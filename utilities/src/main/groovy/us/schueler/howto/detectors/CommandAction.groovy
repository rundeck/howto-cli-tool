package us.schueler.howto.detectors

import groovy.transform.CompileStatic
import us.schueler.howto.Howto

@CompileStatic
class CommandAction extends BaseAction {
    @Override
    void invoke(Howto howto, List<String> args) {
        def invocation = getInvocation(args)
        if (!invocation) {
            println "TODO: unable to automatically execute the action on this OS, sorry!"
            println "You should execution manually:\n\n" +
                    "${invocationString}"
        } else if (howto.verbose) {
            println "how: running: ${invocation}"
        }
        def Process proc = new ProcessBuilder(invocation).
                directory(howto.baseDir).
                inheritIO().
                start()
        int result = proc.waitFor()
        if (result != 0) {
            System.exit(result)
        }
    }

    private List<String> getInvocation(List<String> args) {
        if (System.getProperty("os.name").toLowerCase().contains('windows')) {
            return null
        }
        return [
                'bash',
                '-c',
                invocationString
        ] + (args != null && args ? (['--'] + args) : [])

    }
}
