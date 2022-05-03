package us.schueler.howto.detectors.markdown;

import org.commonmark.ext.autolink.AutolinkExtension;
import org.commonmark.node.*;
import org.commonmark.parser.Parser;
import us.schueler.howto.Howto;
import us.schueler.howto.detectors.CommandAction;
import us.schueler.howto.detectors.Detector;
import us.schueler.howto.model.DiscoveredAction;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.regex.Pattern;

/**
 * Detects "howto.md" or "howto.markdown" file,
 * parses out H2 sections to define howto actions
 */
public class MarkdownDetector implements Detector {

    public static final Pattern HOWTO_H1_PATTERN = Pattern.compile("(?i)(.+)?how[ -]?to(\\W.+)?");

    private static final List<String> HOWTO_FILE_NAMES = new ArrayList<>(Arrays.asList("howto.md", "howto.markdown", "howto"));
    private static final List<String> README_FILE_NAMES = new ArrayList<>(Arrays.asList("readme.md", "readme.markdown", "readme"));
    private static Parser getParser() {
        return Parser.builder().extensions(Collections.singletonList(AutolinkExtension.create())).build();
    }

    public static File findMdFile(File file, List<String> patterns) {
        for (String pat : patterns) {
            File[] found = file.listFiles((File d, String s) -> s.equalsIgnoreCase(pat));
            if (found != null && found.length == 1) {
                return found[0];
            }

        }


        return null;
    }

    @Override
    public List<DiscoveredAction> getActions(Howto howto) {
        Pattern h1search = null;
        File file = findMdFile(howto.getBaseDir(), HOWTO_FILE_NAMES);
        if (null == file) {
            //look for Howto section in readme
            h1search = HOWTO_H1_PATTERN;
            file = findMdFile(howto.getBaseDir(), README_FILE_NAMES);
            if (file == null) {
                return new ArrayList<>();
            }

        }

        try {
            return parseActions(file, h1search);
        } catch (IOException e) {
            return new ArrayList<>();
        }
    }

    public static List<DiscoveredAction> parseActions(File file, Pattern h1search) throws IOException {
        List<DiscoveredAction> actions = new ArrayList<>();
        try (FileInputStream is = new FileInputStream(file)) {
            parseActions(is, h1search, actions);
        }

        return actions;
    }

    public static void parseActions(InputStream is, Pattern h1search, List<DiscoveredAction> actions) throws IOException {
        Node document = getParser().parseReader(new InputStreamReader(is));

        DocVisitor visitor = new DocVisitor();
        visitor.setH1search(h1search);
        document.accept(visitor);
        visitor.finish();
        if (visitor.getActions() != null) {
            actions.addAll(visitor.getActions());
        }

    }

    public final String getName() {
        return "howto";
    }


    public enum ParseState {
        NONE, H1, H2, H2_CONTENT
    }
    static class StringHolder implements Consumer<String>{
        String value = null;

        @Override
        public void accept(String s) {
            this.value=s;
        }
    }
    public static class DocVisitor extends AbstractVisitor {
        public void visitH1(Heading heading) {
            textCapture = new StringHolder();

            visitChildren(heading);

            boolean matchedHeader = h1search == null || textCapture.value != null && h1search.matcher(textCapture.value).matches();

            if (matchedHeader) {
                state = ParseState.H1;
            } else {
                state = ParseState.NONE;
            }

            textCapture = null;
        }

        public void visitH2(Heading heading) {
            textCapture = new StringHolder();

            switch (state) {
                case H1:
                case H2_CONTENT:
                    state = ParseState.H2;
            }
            visitChildren(heading);

            if (state == ParseState.H2) {
                state = ParseState.H2_CONTENT;
                actionTitle(textCapture.value);
            }
            textCapture = null;
        }

        @Override
        public void visit(Heading heading) {
            pushAction();
            if (heading.getLevel() == 1) {
                visitH1(heading);
            } else if (heading.getLevel() == 2) {
                visitH2(heading);
            } else {
                visitChildren(heading);
            }

        }

        @Override
        public void visit(Paragraph paragraph) {
            visitChildren(paragraph);
        }

        @Override
        public void visit(Text text) {
            if (textCapture!=null) {
                textCapture.accept(text.getLiteral());
            } else if (state.equals(ParseState.H2_CONTENT)) {
                addDescription(text.getLiteral() + "\n");
            }

            super.visit(text);
        }

        @Override
        public void visit(FencedCodeBlock fencedCodeBlock) {
            if (state.equals(ParseState.H2_CONTENT)) {
                actionInvocation(fencedCodeBlock.getLiteral());
            }

            visitChildren(fencedCodeBlock);
        }

        @Override
        public void visit(IndentedCodeBlock indentedCodeBlock) {
            if (state.equals(ParseState.H2_CONTENT)) {
                actionInvocation(indentedCodeBlock.getLiteral());
            }

            visitChildren(indentedCodeBlock);
        }

        @Override
        public void visit(Code code) {
            if (state.equals(ParseState.H2_CONTENT)) {
                actionInvocation(code.getLiteral());
            }

            visitChildren(code);
        }

        private void pushAction() {
            if (state.equals(ParseState.H2_CONTENT)) {
                if ((action.getInvocationString()!=null || action.getDescription()!=null) && action.getName()!=null) {
                    actions.add(action);
                }

                state = ParseState.H1;
            }

            action = new CommandAction();
            action.setType("markdown");
        }

        public void finish() {
            pushAction();
        }

        private void reset() {
            state = ParseState.NONE;
            action = new CommandAction();

            action.setType("markdown");
        }

        public void actionInvocation(String string) {
            action.setInvocationString(string);
        }

        public void addDescription(String string) {
            if (action.getDescription()!=null) {
                action.setDescription(getAction().getDescription() + string);
            } else {
                action.setDescription(string);
            }

        }

        public void actionTitle(String string) {
            action.setTitle(string);
            action.setName(string.toLowerCase().replaceAll("\\s+", "-"));
        }

        public List<DiscoveredAction> getActions() {
            return actions;
        }

        public void setActions(List<DiscoveredAction> actions) {
            this.actions = actions;
        }

        public CommandAction getAction() {
            return action;
        }

        public void setAction(CommandAction action) {
            this.action = action;
        }

        public Pattern getH1search() {
            return h1search;
        }

        public void setH1search(Pattern h1search) {
            this.h1search = h1search;
        }

        public Consumer<String> getTextCapture() {
            return textCapture;
        }

        public ParseState getState() {
            return state;
        }

        public void setState(ParseState state) {
            this.state = state;
        }

        private List<DiscoveredAction> actions = new ArrayList<DiscoveredAction>();


        private CommandAction action;
        private Pattern h1search = null;
        private StringHolder textCapture = null;
        private ParseState state = ParseState.NONE;
    }

}
