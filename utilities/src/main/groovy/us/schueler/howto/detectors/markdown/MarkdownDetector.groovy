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

    static enum ParseState {
        NONE,
        H1,
        H2,
        H2_CONTENT
    }

    static class DocVisitor extends AbstractVisitor {
        List<DiscoveredAction> actions = []
        CommandAction action = new CommandAction(type: 'markdown')
        Pattern h1search = null
        Consumer<String> textCapture = null
        ParseState state = ParseState.NONE

        void visitH1(Heading heading) {
            String h1literal = null
            textCapture = { String s -> h1literal = s }

            visitChildren(heading)

            boolean matchedHeader = h1search && h1literal && h1search.matcher(h1literal).matches() || !h1search

            if (matchedHeader) {
                state = ParseState.H1
            } else {
                state = ParseState.NONE
            }
            textCapture = null
        }

        void visitH2(Heading heading) {
            String h1literal = null
            textCapture = { String s -> h1literal = s }

            switch (state) {
                case ParseState.H1:
                case ParseState.H2_CONTENT:
                    state = ParseState.H2
            }
            visitChildren(heading)

            switch (state) {
                case ParseState.H2:
                    state = ParseState.H2_CONTENT
                    actionTitle(h1literal)
            }
            textCapture = null
        }

        @Override
        void visit(Heading heading) {
            pushAction()
            if (heading.level == 1) {
                visitH1(heading)
            } else if (heading.level == 2) {
                visitH2(heading)
            } else {
                visitChildren(heading)
            }
        }

        @Override
        void visit(Paragraph paragraph) {
            visitChildren(paragraph)
        }

        @Override
        void visit(Text text) {
            if (textCapture) {
                textCapture.accept(text.literal)
            } else if (state == ParseState.H2_CONTENT) {
                addDescription(text.literal + "\n")
            }
            super.visit(text)
        }

        @Override
        void visit(FencedCodeBlock fencedCodeBlock) {
            if (state == ParseState.H2_CONTENT) {
                actionInvocation(fencedCodeBlock.literal)
            }
            visitChildren(fencedCodeBlock)
        }

        @Override
        void visit(IndentedCodeBlock indentedCodeBlock) {
            if (state == ParseState.H2_CONTENT) {
                actionInvocation(indentedCodeBlock.literal)
            }
            visitChildren(indentedCodeBlock)
        }

        @Override
        void visit(Code code) {
            if (state == ParseState.H2_CONTENT) {
                actionInvocation(code.literal)
            }
            visitChildren(code)
        }

        private void pushAction() {
            if (state == ParseState.H2_CONTENT) {
                if ((action.invocationString || action.description) && action.name) {
                    actions.add(action)
                }
                state == ParseState.H1
            }
            action = new CommandAction(type: 'markdown')
        }

        void finish() {
            pushAction()
        }

        private void reset() {
            state = ParseState.NONE
            action = new CommandAction(type: 'markdown')
        }

        void actionInvocation(String string) {
            action.invocationString = string
        }

        void addDescription(String string) {
            if (action.description) {
                action.description += string
            } else {
                action.description = string
            }
        }

        void actionTitle(String string) {
            action.title = string
            action.name = string.toLowerCase().replaceAll('\\s+', '-')
        }
    }

    @Override
    List<DiscoveredAction> getActions(Howto howto) {
        Pattern h1search = null
        File file = findMdFile(howto.baseDir, HOWTO_FILE_NAMES)
        if (!file) {
            //look for Howto section in readme
            h1search = HOWTO_H1_PATTERN
            file = findMdFile(howto.baseDir, README_FILE_NAMES)
            if (!file) {
                return []
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
