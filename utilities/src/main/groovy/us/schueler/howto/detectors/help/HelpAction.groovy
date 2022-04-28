package us.schueler.howto.detectors.help

import groovy.transform.CompileStatic
import us.schueler.howto.Howto
import us.schueler.howto.detectors.Detector
import picocli.CommandLine.Help.Ansi

@CompileStatic
class HelpAction extends us.schueler.howto.detectors.BaseAction {
    String type = 'help'
    String name = 'help'
    String title = 'Help'
    String description = 'List available Howto actions.'

    @Override
    void invoke(Howto howto, List<String> args) {
        printPlain(howto)
    }

    private void printPlain(Howto howto) {
        int count = 0
        println Ansi.AUTO.string("""Here's what you can do:

@|green how run|@ @|white [action]|@ [args]

""")
        Map<String, Map<String, String>> cmds = [:]
        howto.detectors.each { Detector detector ->
            detector.getActions(howto).each {
                if (it.name == 'help' && it.type == 'help') {
                    return
                }
                cmds.put(it.name, [description: it.description, invocationString: it.invocationString?.trim()])
                count++
            }
        }
        if (count > 0) {
            println "Actions:\n"
        } else {
            println Ansi.AUTO.string("@|faint No actions were found.|@")
        }
        int max = cmds.keySet().collect { it.size() }.max()
        cmds.each { String name, Map<String, String> data ->
            if (howto.verbose) {
                println Ansi.AUTO.string("@|white ${name}|@: \n")
                println indentString('    ', data.description.trim())
            } else {
                String named = name.size() < max ? (name + (" " * (max - name.size()))) : name
                def shortDesc = data.description.split('[\n\r]', 2)[0]
                println Ansi.AUTO.string("@|white ${named}|@: ${shortDesc}")
            }
            if (howto.verbose && data.invocationString) {
                println '\n' + indentString('        ', data.invocationString) + '\n'
            }
        }
        if (!howto.verbose) {
            println Ansi.AUTO.string("@|faint " + ("_" * 43) + " |@")
            println Ansi.AUTO.string("@|cyan tip:|@ use @|green how help -v|@ for more information")
        }
    }

    private String indentString(String indent, String string) {
        indent + string.replaceAll('\n', '\n' + indent)
    }

    private void printMarkdown(Howto howto, int count) {
        println """# Welcome to Howto
"""
        howto.detectors.each { Detector detector ->
            detector.getActions(howto).each {
                println "## ${detector.name}: ${it.title}\n"
                println "${it.description}\n"
                if (it.invocationString) {
                    println "    ${it.invocationString}\n"
                }
                println "alias"
                println ":   how run ${it.type}:${it.name}\n"
                count++
            }
        }
    }
}
