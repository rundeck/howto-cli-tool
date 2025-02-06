package org.rundeck.howto.detectors.gradle;

import org.rundeck.howto.Howto;
import org.rundeck.howto.detectors.CommandAction;
import org.rundeck.howto.detectors.Detector;
import org.rundeck.howto.model.DiscoveredAction;

import java.io.*;
import java.util.*;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GradleDetector implements Detector {
    public static Pattern TASK_PATTERN = Pattern.compile("^(\\w+) - (.+)$");

    public String getName() {
        return "gradle";
    }

    @Override
    public List<DiscoveredAction> getActions(Howto howto) {
        File gradleBase = findGradleBase(howto.getBaseDir());
        if (gradleBase == null) {
            return new ArrayList<>();
        }

        Map<String, String> gradleActions;
        try {
            gradleActions = listGradleActions(gradleBase, howto.getBaseDir().getAbsoluteFile());
        } catch (IOException | InterruptedException ignored) {
            return new ArrayList<>();
        }
        final List<DiscoveredAction> actions = new ArrayList<>();
        final List<String> strings = getActionStrings(gradleBase, howto.getBaseDir().getAbsoluteFile());
        gradleActions.forEach((key, val) -> {
            CommandAction action = new CommandAction();
            action.setType("gradle");
            action.setName(key);
            action.setDescription(val);
            List<String> newStrings = new ArrayList<>(strings);
            newStrings.add(key);
            action.setInvocationString(String.join(" ", newStrings));
            actions.add(action);
        });
        return actions;
    }

    public Map<String, String> listGradleActions(File gradleBaseDir, File projDir) throws IOException, InterruptedException {
        File gradlew = new File(gradleBaseDir, "gradlew");
        if (!gradlew.exists()) {
            return new HashMap<>();
        }

        List<String> actionStrings = getActionStrings(gradleBaseDir, projDir);
        actionStrings.add("tasks");
        LineReader reader = new LineReader();
        captureOutput(projDir, actionStrings, reader);
        return reader.found;
    }

    private List<String> getActionStrings(File gradleBaseDir, File projDir) {
        String gradlew = "./gradlew";
        if (!projDir.equals(gradleBaseDir)) {
            gradlew = projDir.toPath().relativize(new File(gradleBaseDir, "gradlew").toPath()).toString();
        }

        return new ArrayList<>(Collections.singletonList(gradlew));
    }

    public int captureOutput(File baseDir, List<String> command, Consumer<String> sink) throws IOException, InterruptedException {
        ProcessBuilder builder = new ProcessBuilder(command).directory(baseDir);
        Process proc = builder.start();
        waitForProcessOutput(proc, sink, null);
        return proc.waitFor();
    }

    static class ReadLines implements Runnable {
        private final Consumer<String> sink;
        private final InputStream stream;

        public ReadLines(Consumer<String> sink, InputStream stream) {
            this.sink = sink;
            this.stream = stream;
        }

        @Override
        public void run() {
            try {
                BufferedReader bri = new BufferedReader(new InputStreamReader(stream));

                String line;
// bri may be empty or incomplete.
                while ((line = bri.readLine()) != null) {
                    if (sink != null) sink.accept(line);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        }
    }

    private void waitForProcessOutput(Process proc, Consumer<String> sink, Consumer<String> sinkErr) {
        boolean interrupted = false;
        try {
            Thread outProc = new Thread(new ReadLines(sink, proc.getInputStream()));
            outProc.start();
            Thread errProc = new Thread(new ReadLines(sinkErr, proc.getErrorStream()));
            errProc.start();
            try {
                outProc.join();
            } catch (InterruptedException e) {
                interrupted = true;
            }
            try {
                errProc.join();
            } catch (InterruptedException e) {
                interrupted = true;
            }
        } finally {
            if (interrupted) Thread.currentThread().interrupt();
        }
    }

    public File findGradleBase(File baseDir) {
        File dir = baseDir.getAbsoluteFile();
        int count = 0;

        while (count < 10 && dir != null) {
            if (new File(dir, "settings.gradle").exists()) {
                break;
            }

            dir = dir.getParentFile();

            count++;
        }

        if (dir != null && new File(dir, "settings.gradle").exists()) {
            return dir;
        }

        return null;
    }

    static class LineReader implements Consumer<String> {
        Map<String, String> found = new LinkedHashMap<>();

        @Override
        public void accept(String line) {
            Matcher m = TASK_PATTERN.matcher(line);
            if (m.matches()) {
                if (m.groupCount() == 2) {
                    found.put(m.group(1), m.group(2));
                }
            }
        }
    }
}
