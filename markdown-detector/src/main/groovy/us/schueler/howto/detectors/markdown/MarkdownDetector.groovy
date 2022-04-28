package us.schueler.howto.detectors.markdown

import groovy.transform.CompileStatic
import org.commonmark.ext.autolink.AutolinkExtension
import org.commonmark.node.AbstractVisitor
import org.commonmark.node.Code
import org.commonmark.node.FencedCodeBlock
import org.commonmark.node.Heading
import org.commonmark.node.IndentedCodeBlock
import org.commonmark.node.Node
import org.commonmark.node.Text
import org.commonmark.parser.Parser
import us.schueler.howto.Howto
import us.schueler.howto.detectors.CommandAction
import us.schueler.howto.detectors.Detector
import us.schueler.howto.model.DiscoveredAction

import java.util.function.Consumer

/**
 * Detects "howto.md" or "howto.markdown" file,
 * parses out H2 sections to define howto actions
 */
@CompileStatic
class MarkdownDetector implements Detector {
    final String name = 'howto'

    private static Parser getParser() {
        return Parser.builder()
                .extensions(Collections.singletonList(AutolinkExtension.create()))
                .build();
    }
    static List<String> PATTERNS = [
            'howto.md',
            'howto.markdown'
    ]

    static File findMdFile(File file) {
        for (String pat : PATTERNS) {
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
        boolean h1Context = false
        boolean h2Context = false
        StringBuffer sb = new StringBuffer()
        Consumer<String> textCapture = null

        @Override
        void visit(Heading heading) {
            if (heading.level == 1 && !h1Context) {
                reset()
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
            textCapture = null
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
        List<DiscoveredAction> actions = new ArrayList<>()
        File file = findMdFile(howto.baseDir)
        if (!file) {
            return []
        }
        try (InputStream is = new FileInputStream(file)) {
            Node document = parser.parseReader(new InputStreamReader(is));
            //find h1 nodes

            def visitor = new DocVisitor()
            document.accept(visitor)
            visitor.finish()
            if (visitor.actions) {
                actions.addAll(visitor.actions)
            }
        }
        actions
    }
}
