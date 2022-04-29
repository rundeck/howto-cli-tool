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
        File gradleBase = findGradleBase(howto.baseDir)
        if (!gradleBase) {
            return []
        }
        Map<String, String> gradleActions = listGradleActions(gradleBase, howto.baseDir.absoluteFile)
        List<DiscoveredAction> actions = new ArrayList<>()
        def strings = getActionStrings(gradleBase, howto.baseDir.absoluteFile)
        gradleActions.each {
            actions << new CommandAction(
                    type: 'gradle',
                    name: it.key,
                    description: it.value,
                    invocationString: (strings + [it.key]).join(' ')
            )
        }
        actions
    }

    Map<String, String> listGradleActions(File gradleBaseDir, File projDir) {
        def gradlew = new File(gradleBaseDir, 'gradlew')
        if (!gradlew.exists()) {
            return [:]
        }
        List<String> actionStrings = getActionStrings(gradleBaseDir, projDir)
        String output = captureOutput(projDir, actionStrings + ['tasks'])
        return parseOutputTasks(output)
    }

    private List<String> getActionStrings(File gradleBaseDir, File projDir) {
        String gradlew = './gradlew'
        if (projDir != gradleBaseDir) {
            gradlew = projDir.toPath().relativize(new File(gradleBaseDir, 'gradlew').toPath()).toString()
        }
        [gradlew]
    }

    String captureOutput(File baseDir, List<String> command) {
        def builder = new ProcessBuilder(command).directory(baseDir)
        def Process proc = builder.start()
        StringBuffer out = new StringBuffer()
        proc.waitForProcessOutput(out, System.err)
        int ret = proc.waitFor()
        if (ret == 0) {
            return out.toString()
        }
        return null
    }

    File findGradleBase(File baseDir) {
        File dir = baseDir.getAbsoluteFile()
        int count = 0

        while (count < 10 && dir) {
            if (new File(dir, 'settings.gradle').exists()) {
                break
            }
            dir = dir.parentFile

            count++
        }
        if (dir && new File(dir, 'settings.gradle').exists()) {
            return dir
        }
        return null
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
