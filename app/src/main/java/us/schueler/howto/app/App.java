package us.schueler.howto.app;

import picocli.CommandLine;
import us.schueler.howto.Howto;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@CommandLine.Command(name = "howto", versionProvider = VersionProvider.class, mixinStandardHelpOptions = true, description = "Discover how to do things and do them")
public class App {
    public static void main(String[] args) {
        List<String> allowed = Arrays.asList("-V", "--version", "-h", "--help", "help", "ls", "list", "run", "do", "to", "exec");
        if (args.length > 0 && !allowed.contains(args[0])) {
            //assume a run command is intended
            ArrayList<String> run = new ArrayList<>(Collections.singletonList("to"));
            run.addAll(Arrays.asList(args));
            args = run.toArray(new String[]{});
        } else if (args.length == 0) {
            args = new String[]{"help"};
        }

        System.exit(new CommandLine(new App()).setExpandAtFiles(false).execute(args));
    }

    @CommandLine.Command(name = "to", aliases = {"list", "ls", "help"}, description = "List available actions")
    public int help(@CommandLine.Option(names = {"-d", "--dir"}, description = "Base dir") File baseDir,
                    @CommandLine.Option(names = {"-x", "--exclude"}, description = "Exclude scanner type") List<String> excludeTypes,
                    @CommandLine.Option(names = {"-v", "--verbose"}, description = "Verbose") boolean verbose,
                    @CommandLine.Option(names = {"-a", "--all"}, description = "All") boolean all,
                    @CommandLine.Option(names = {"--debug"}, description = "Debug") boolean debug,
                    @CommandLine.Parameters(paramLabel = "args", description = "args passed to the action") List<String> args) {
        return run(baseDir, excludeTypes, args != null && args.size() > 0 || verbose, all, debug, "help", args);
    }

    @CommandLine.Command(name = "do", aliases = {"run", "exec"}, description = "Run an action")
    public int run(@CommandLine.Option(names = {"-d", "--dir"}, description = "Base dir") File baseDir,
                   @CommandLine.Option(names = {"-x", "--exclude"}, description = "Exclude scanner type") List<String> excludeTypes,
                   @CommandLine.Option(names = {"-v", "--verbose"}, description = "Verbose") boolean verbose,
                   @CommandLine.Option(names = {"-a", "--all"}, description = "All") boolean all,
                   @CommandLine.Option(names = {"--debug"}, description = "Debug") boolean debug,
                   @CommandLine.Parameters(paramLabel = "action") String action,
                   @CommandLine.Parameters(paramLabel = "args", description = "args passed to the action") List<String> args) {
        Howto howto = Howto.create(baseDir != null ? baseDir : new File("").getAbsoluteFile());
        howto.setVerbose(verbose);
        howto.setAll(all);
        howto.setDebug(debug);
        howto.setExcludeTypes(excludeTypes);

        int val = howto.invoke(action, args);
        if (val < 0) {
            //find all actions
            if (!all) {
                howto.setVerbose(true);
                howto.setAll(true);
                val = howto.invoke(action, args);
            }

        }

        if (val < 0) {
            System.out.println("Action not found: " + action);
            return 1;
        }

        return val;
    }

}
