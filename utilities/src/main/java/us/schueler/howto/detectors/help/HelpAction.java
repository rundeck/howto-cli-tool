package us.schueler.howto.detectors.help;

import picocli.CommandLine;
import us.schueler.howto.HowtoApp;
import us.schueler.howto.detectors.BaseAction;
import us.schueler.howto.model.DiscoveredAction;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public class HelpAction extends BaseAction {

    public int invoke(HowtoApp howto, List<String> args) {
        printPlain(howto, args);
        return 0;
    }

    private void printPlain(final HowtoApp howto, List<String> args) {

        List<String> tips = new ArrayList<>();
        Map<String, DiscoveredAction> cmds = collectActionsAllOrEqual(howto, howto.isAll(), args);

        if (cmds.size() < 1 && args != null && args.size() > 0 && !howto.isAll()) {
            //retry using all detectors
            cmds = collectActionsAllOrEqual(howto, true, args);
        }


        if (args == null || args.size() < 1) {
            System.out.println(CommandLine.Help.Ansi.AUTO.string("Found Actions:"));
            System.out.println(CommandLine.Help.Ansi.AUTO.string("@|faint " + (repeatString("_", 40)) + " |@"));
            tips.add(CommandLine.Help.Ansi.AUTO.string("see more: @|green how to|@ @|white [action]|@"));
            tips.add(CommandLine.Help.Ansi.AUTO.string("do the action: @|green how do|@ @|white [action]|@ [args]"));
        } else {
            tips.add(CommandLine.Help.Ansi.AUTO.string("do the thing with: @|green how do|@ @|white [action]|@ [args]"));
            tips.add(CommandLine.Help.Ansi.AUTO.string("or just: @|green how|@ @|white [action]|@ [args]"));
        }

        if (cmds.size() < 1) {
            System.out.println(CommandLine.Help.Ansi.AUTO.string("@|faint No actions were found.|@"));
            if (args != null && args.size() == 1) {
                //find similar
                String test = args.get(0);
                Map<String, DiscoveredAction> similar = collectActionsMatching(howto, true, (action) -> action.getName().contains(test));
                if(!similar.isEmpty()){
                    System.out.println(CommandLine.Help.Ansi.AUTO.string("@|faint Did you mean?|@"));
                    similar.forEach((n,a)-> System.out.println(CommandLine.Help.Ansi.AUTO.string("@|faint - "+n+"|@")));

                }
            }
        }

        final int max = cmds.keySet().stream().map(String::length).max(Integer::compareTo).orElse(-1);
        cmds.forEach((final String name, final DiscoveredAction data) -> {
            String description = data.getDescription();
            if (howto.isVerbose()) {
                System.out.println(CommandLine.Help.Ansi.AUTO.string("@|white " + name + "|@: \n"));
                if (description != null) {
                    System.out.println(indentString("    ", description.trim()));
                    System.out.println();
                }
            } else {
                final String named = (name + (repeatString(" ", (max > 0 ? ((max + 1) - name.length()) : 2))));
                String shortDesc = description != null ? description.split("[\n\r]", 2)[0] : "";
                if (howto.isAll()) {
                    shortDesc = ("[" + data.getType() + "] " + shortDesc);
                }

                System.out.println(CommandLine.Help.Ansi.AUTO.string("@|white " + named + "|@: " + shortDesc));

            }

            if (howto.isVerbose() && data.getInvocationString() != null) {
                System.out.println(CommandLine.Help.Ansi.AUTO.string("@|magenta " + indentString("    ", data.getInvocationString() + "|@")));
                System.out.println();
            }

        });
        if (!howto.isVerbose()) {
            tips.add(CommandLine.Help.Ansi.AUTO.string("use @|green how to -v|@ for more information"));
        }


        if (tips.size() > 0) {
            System.out.println(CommandLine.Help.Ansi.AUTO.string("@|faint " + (repeatString("_", 40)) + " |@"));
            tips.forEach((s) -> System.out.println(CommandLine.Help.Ansi.AUTO.string("@|cyan tip:|@ ") + s));
        }

    }

    private String repeatString(String s, int i) {
        StringBuilder sb = new StringBuilder();
        for (int x = 0; x < i; x++) {
            sb.append(s);
        }
        return sb.toString();
    }

    private Map<String, DiscoveredAction> collectActionsAllOrEqual(HowtoApp howto, boolean all, final List<String> args) {
        return collectActionsMatching(howto, all, (action) -> args == null || args.size() == 0 || args.contains(action.getName()));
    }

    private Map<String, DiscoveredAction> collectActionsMatching(HowtoApp howto, boolean all, final Predicate<DiscoveredAction> filter) {
        final Map<String, DiscoveredAction> cmds = new LinkedHashMap<>();
        howto.getDetectedActions(all).forEach((DiscoveredAction action) -> {
            if (action.getName().equals("help") && action.getType().equals("help")) {
                return;
            }

            if (filter.test(action)) {
                cmds.put(action.getName(), action);
            }

        });
        return cmds;
    }

    private String indentString(@SuppressWarnings("SameParameterValue") String indent, String string) {
        return indent + string.replaceAll("\n", "\n" + indent);
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    private String type = "help";
    private String name = "help";
    private String title = "Help";
    private String description = "List available Howto actions.";
}
