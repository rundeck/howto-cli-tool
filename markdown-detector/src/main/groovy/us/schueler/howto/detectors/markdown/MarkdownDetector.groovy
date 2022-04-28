package us.schueler.howto.detectors.markdown

import groovy.transform.CompileStatic
import org.commonmark.ext.autolink.AutolinkExtension
import org.commonmark.node.*
import org.commonmark.parser.Parser
import us.schueler.howto.Howto
import us.schueler.howto.detectors.CommandAction
import us.schueler.howto.detectors.Detector
import us.schueler.howto.model.DiscoveredAction

import java.util.function.Consumer
import java.util.regex.Pattern
/**
 * Detects "howto.md" or "howto.markdown" file,
 * parses out H2 sections to define howto actions
 */
@CompileStatic
class MarkdownDetector implements Detector {
    public static final Pattern HOWTO_H1_PATTERN = Pattern.compile(/(?i)(.+)?how[ -]?to(\W.+)?/)
    final String name = 'howto'

    private static Parser getParser() {
        return Parser.builder()
                .extensions(Collections.singletonList(AutolinkExtension.create()))
                .build();
    }
    static List<String> HOWTO_FILE_NAMES = [
            'howto.md',
            'howto.markdown',
            'howto'
    ]
    static List<String> README_FILE_NAMES = [
            'readme.md',
            'readme.markdown',
            'readme'
    ]

    static File findMdFile(File file, List<String> patterns) {
        for (String pat : patterns) {
            def found = file.listFiles({ File d, String s -> s.equalsIgnoreCase(pat) } as FilenameFilter)
            if (found.length == 1) {
                return found[0]
            }
        }

        return null
    }

    static class DocVisitor extends AbstractVisitor {
        List<DiscoveredAction> actions = []
        CommandAction state = new CommandAction(type: 'markdown')
        Pattern h1search = null
        boolean h1Context = false
        boolean h2Context = false
        Consumer<String> textCapture = null

        @Override
        void visit(Heading heading) {
            String h1literal = null

            if (heading.level == 1 && !h1Context) {
                reset()
                if (h1search) {
                    textCapture = { String s -> h1literal = s }
                }
                h1Context = true
            } else if (heading.level == 2 && h1Context) {
                pushAction()
                h2Context = true
                textCapture = { String s -> actionTitle(s) }
            } else if (h2Context) {
                pushAction()
                reset()
            }
            visitChildren(heading)
            if (heading.level == 1 && h1search && h1literal) {

                def matcher = h1search.matcher(h1literal)
                if (!matcher.matches()) {
                    reset()
                }
            } else if (heading.level == 1 && h1search && !h1literal) {
                reset()
            }
            textCapture = null
        }

        @Override
        void visit(Paragraph paragraph) {
            visitChildren(paragraph)
        }

        @Override
        void visit(Text text) {
            if (textCapture) {
                textCapture.accept(text.literal)
            } else if (h2Context) {
                addDescription(text.literal + "\n")
            }
            super.visit(text)
        }

        @Override
        void visit(FencedCodeBlock fencedCodeBlock) {
            if (h2Context) {
                actionInvocation(fencedCodeBlock.literal)
            }
            visitChildren(fencedCodeBlock)
        }

        @Override
        void visit(IndentedCodeBlock indentedCodeBlock) {
            if (h2Context) {
                actionInvocation(indentedCodeBlock.literal)
            }
            visitChildren(indentedCodeBlock)
        }

        @Override
        void visit(Code code) {
            if (h2Context) {
                actionInvocation(code.literal)
            }
            visitChildren(code)
        }

        private void pushAction() {
            if (state.invocationString && state.name) {
                actions.add(state)
            }
            state = new CommandAction(type: 'markdown')
        }

        void finish() {
            if (h2Context) {
                pushAction()
            }
        }

        private void reset() {
            h1Context = false
            h2Context = false
            state = new CommandAction(type: 'markdown')
        }

        void actionInvocation(String string) {
            state.invocationString = string
        }

        void addDescription(String string) {
            if (state.description) {
                state.description += string
            } else {
                state.description = string
            }
        }

        void actionTitle(String string) {
            state.title = string
            state.name = string.toLowerCase().replaceAll('\\s+', '-')
        }
    }

    @Override
    List<DiscoveredAction> getActions(Howto howto) {
        Pattern h1search = null
        File file = findMdFile(howto.baseDir, HOWTO_FILE_NAMES)
        if (!file) {
            //look for Howto section in readme
            file = findMdFile(howto.baseDir, README_FILE_NAMES)
            if (file) {
                h1search = HOWTO_H1_PATTERN
            }
        }
        parseActions(file, h1search)
    }

    static List<DiscoveredAction> parseActions(File file, Pattern h1search) {
        List<DiscoveredAction> actions = new ArrayList<>()
        try (InputStream is = new FileInputStream(file)) {
            parseActions(is, h1search, actions)
        }
        actions
    }

    static void parseActions(InputStream is, Pattern h1search, List<DiscoveredAction> actions) {
        Node document = parser.parseReader(new InputStreamReader(is))

        def visitor = new DocVisitor()
        visitor.h1search = h1search
        document.accept(visitor)
        visitor.finish()
        if (visitor.actions) {
            actions.addAll(visitor.actions)
        }
    }
}
