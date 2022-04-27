package us.schueler.cando.utilities.detectors.help

import groovy.transform.CompileStatic
import us.schueler.cando.utilities.Cando
import us.schueler.cando.utilities.detectors.BaseAction
import us.schueler.cando.utilities.detectors.Detector
import picocli.CommandLine.Help.Ansi

@CompileStatic
class HelpAction extends BaseAction {
    String type = 'help'
    String name = 'help'
    String title = 'Help'
    String description = 'List available cando actions.'

    @Override
    void invoke(Cando cando, List<String> args) {
        printPlain(cando)
    }

    private void printPlain(Cando cando) {
        int count = 0
        println Ansi.AUTO.string("""Here's what you can do:

@|green cando run|@ @|white [action]|@ [args]

""")
        Map<String, Map<String, String>> cmds = [:]
        cando.detectors.each { Detector detector ->
            detector.getActions(cando).each {
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
            if (cando.verbose) {
                println Ansi.AUTO.string("@|white ${name}|@: \n")
                println indentString('    ', data.description.trim())
            } else {
                String named = name.size() < max ? (name + (" " * (max - name.size()))) : name
                def shortDesc = data.description.split('[\n\r]', 2)[0]
                println Ansi.AUTO.string("@|white ${named}|@: ${shortDesc}")
            }
            if (cando.verbose && data.invocationString) {
                println '\n' + indentString('        ', data.invocationString) + '\n'
            }
        }
        if (!cando.verbose) {
            println Ansi.AUTO.string("@|faint " + ("_" * 43) + " |@")
            println Ansi.AUTO.string("@|cyan tip:|@ use @|green cando help -v|@ for more information")
        }
    }

    private String indentString(String indent, String string) {
        indent + string.replaceAll('\n', '\n' + indent)
    }

    private void printMarkdown(Cando cando, int count) {
        println """# Welcome to cando
"""
        cando.detectors.each { Detector detector ->
            detector.getActions(cando).each {
                println "## ${detector.name}: ${it.title}\n"
                println "${it.description}\n"
                if (it.invocationString) {
                    println "    ${it.invocationString}\n"
                }
                println "alias"
                println ":   cando run ${it.type}:${it.name}\n"
                count++
            }
        }
    }
}
