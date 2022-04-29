package us.schueler.howto.detectors.gradle

import groovy.transform.CompileStatic
import us.schueler.howto.Howto
import us.schueler.howto.detectors.CommandAction
import us.schueler.howto.detectors.Detector
import us.schueler.howto.model.DiscoveredAction

import java.util.regex.Matcher
import java.util.regex.Pattern

@CompileStatic
class GradleDetector implements Detector {
    final String name = 'gradle'

    @Override
    List<DiscoveredAction> getActions(Howto howto) {
        if (!hasGradle(howto.baseDir)) {
            return []
        }
        Map<String, String> gradleActions = listGradleActions(howto.baseDir)
        List<DiscoveredAction> actions = new ArrayList<>()
        gradleActions.each {
            actions << new CommandAction(
                    type: 'gradle',
                    name: it.key,
                    description: it.value,
                    invocationString: "./gradlew ${it.key}"
            )
        }
        actions
    }

    Map<String, String> listGradleActions(File baseDir) {
        def gradlew = new File(baseDir, 'gradlew')
        if (gradlew.exists()) {
            String output = captureOutput(baseDir, ['./gradlew', 'tasks'])
            return parseOutputTasks(output)
        }
        [:]
    }

    String captureOutput(File baseDir, List<String> command) {
        def builder = new ProcessBuilder(command).directory(baseDir)
        def Process proc = builder.start()
        StringBuffer out = new StringBuffer()
        proc.waitForProcessOutput(out, null)
        int ret = proc.waitFor()
        if (ret == 0) {
            return out.toString()
        }
        return null
    }

    boolean hasGradle(File baseDir) {
        return new File(baseDir, 'settings.gradle').exists()
    }

    static Pattern TASK_PATTERN = Pattern.compile(/^(\w+) - (.+)$/)

    static Map<String, String> parseOutputTasks(String s) {
        Map<String, String> found = [:]
        for (String line : s.readLines()) {
            Matcher m = TASK_PATTERN.matcher(line)
            if (m.matches()) {
                if (m.groupCount() == 2) {
                    found[m.group(1)] = m.group(2)
                }
            }
        }
        found
    }
}
