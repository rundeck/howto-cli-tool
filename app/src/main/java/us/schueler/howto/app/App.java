package us.schueler.howto.app;

import picocli.CommandLine;
import us.schueler.howto.Howto;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

@CommandLine.Command(
        name = "howto",
        versionProvider = VersionProvider.class,
        mixinStandardHelpOptions = true,
        description = "Discover how to do things and do them"
)
public class App implements Callable<Integer> {
    public static void main(String[] args) {
        System.exit(new CommandLine(new App()).setExpandAtFiles(false).execute(args));
    }


    @CommandLine.Option(names = {"-d", "--dir"}, description = "Base dir", scope = CommandLine.ScopeType.INHERIT)
    File baseDir;
    @CommandLine.Option(names = {"-v", "--verbose"}, description = "Verbose", scope = CommandLine.ScopeType.INHERIT)
    boolean verbose;
    @CommandLine.Option(names = {"-x", "--exclude"}, description = "Exclude scanner type", scope = CommandLine.ScopeType.INHERIT)
    java.util.List<String> excludeTypes;
    @CommandLine.Option(names = {"-a", "--all"}, description = "All", scope = CommandLine.ScopeType.INHERIT)
    boolean all;
    @CommandLine.Option(names = {"--debug"}, description = "Debug", scope = CommandLine.ScopeType.INHERIT)
    boolean debug;
    @CommandLine.Parameters(paramLabel = "action args", description = "action and args passed to the action", scope = CommandLine.ScopeType.INHERIT)
    java.util.List<String> args;

    @Override
    public Integer call() {
        String action = shiftArgs();
        return run(action, args);
    }

    private String shiftArgs() {
        String action = args != null && !args.isEmpty() ? args.get(0) : "help";
        args = args != null && args.size() > 1 ? args.subList(1, args.size()) : Collections.emptyList();
        return action;
    }

    @CommandLine.Command(name = "to", aliases = {"list", "ls", "help"}, description = "List available actions")
    public int helpCommand() {
        return run("help", args);
    }

    @CommandLine.Command(name = "run", aliases = {"exec", "do"}, description = "Run an action")
    public int execCommand() {
        String action = shiftArgs();
        return run(action, args);
    }

    public int run(String action, java.util.List<String> args) {
        Howto howto = Howto.create(baseDir != null ? baseDir : new File("").getAbsoluteFile());
        howto.setVerbose(args!=null && args.size()>0 || verbose);
        howto.setAll(all);
        howto.setDebug(debug);
        howto.setExcludeTypes(excludeTypes);

        int val = howto.invoke(action, args);
        if (val < 0) {
            //find all actions
            if (all) {
                howto.setVerbose(true);
                howto.setAll(true);
                val = howto.invoke(action, args);
            }

        }

        if (val < 0) {
            System.out.println("Action not found: " + action);
            run("help", Collections.singletonList(action));
            return 1;
        }

        return val;
    }

}
