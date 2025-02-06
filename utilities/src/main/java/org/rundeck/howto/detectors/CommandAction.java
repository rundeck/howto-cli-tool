package org.rundeck.howto.detectors;

import picocli.CommandLine;
import org.rundeck.howto.HowtoApp;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CommandAction extends BaseAction {
    @Override
    public int invoke(HowtoApp howto, List<String> args) {
        List<String> invocation = getInvocation(args);
        if (invocation == null || invocation.isEmpty()) {
            System.out.println("TODO: unable to automatically execute the action on this OS, sorry!");
            System.out.println("You should execution manually:\n\n" + getInvocationString());
            return 1;
        } else if (howto.isVerbose()) {
            System.out.println(CommandLine.Help.Ansi.AUTO.string("@|faint how do|@ @|white " + getName() + " |@@|faint >|@ " + getInvocationDisplay()));
            System.out.println(CommandLine.Help.Ansi.AUTO.string("@|faint ____________________________________________ |@"));
        }

        try {
            ProcessBuilder builder = new ProcessBuilder(invocation).directory(howto.getBaseDir()).inheritIO();
            builder.environment().put("DIR", howto.getBaseDir().getAbsolutePath());
            Process proc = builder.start();
            return proc.waitFor();
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private String getInvocationDisplay() {
        if (getInvocationString().trim().contains("\n")) {
            return "[script]";
        } else {
            return getInvocationString().trim();
        }

    }

    private List<String> getInvocation(List<String> args) {
        if (System.getProperty("os.name").toLowerCase().contains("windows")) {
            return null;
        }

        return getBashInvocation(args);
    }

    private List<String> getBashInvocation(List<String> args) {
        List<String> bash = new ArrayList<>(Arrays.asList("bash", "-c", getInvocationString().trim()));
        if (args != null && args.size() > 0) {
            bash.add("--");
            bash.addAll(args);
        }
        return bash;
    }
}
