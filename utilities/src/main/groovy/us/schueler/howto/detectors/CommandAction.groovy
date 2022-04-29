package us.schueler.howto.detectors

import groovy.transform.CompileStatic
import picocli.CommandLine
import us.schueler.howto.Howto

@CompileStatic
class CommandAction extends BaseAction {
    @Override
    int invoke(Howto howto, List<String> args) {
        def invocation = getInvocation(args)
        if (!invocation) {
            println "TODO: unable to automatically execute the action on this OS, sorry!"
            println "You should execution manually:\n\n" +
                    "${invocationString}"
        } else if (howto.verbose) {
            println CommandLine.Help.Ansi.AUTO.string("@|faint how do|@ @|white ${name} |@@|faint >|@ ${invocationDisplay}")

            println CommandLine.Help.Ansi.AUTO.string("@|faint " + ("_" * 40) + " |@")
        }
        def Process proc = new ProcessBuilder(invocation).
                directory(howto.baseDir).
                inheritIO().
                start()
        return proc.waitFor()
    }

    private String getInvocationDisplay() {
        if (invocationString.trim().contains("\n")) {
            return '[script]'
        } else {
            return invocationString.trim()
        }
    }

    private List<String> getInvocation(List<String> args) {
        if (System.getProperty("os.name").toLowerCase().contains('windows')) {
            return null
        }
        return getBashInvocation(args)
    }

    private List<String> getBashInvocation(List<String> args) {
        [
                'bash',
                '-c',
                invocationString.trim()
        ] + (args != null && args ? (['--'] + args) : [])
    }
}
