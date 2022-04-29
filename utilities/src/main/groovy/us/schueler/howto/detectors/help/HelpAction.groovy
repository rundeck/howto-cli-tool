package us.schueler.howto.detectors.help

import groovy.transform.CompileStatic
import us.schueler.howto.Howto
import us.schueler.howto.detectors.BaseAction
import us.schueler.howto.detectors.Detector
import picocli.CommandLine.Help.Ansi
import us.schueler.howto.model.DiscoveredAction

@CompileStatic
class HelpAction extends BaseAction {
    String type = 'help'
    String name = 'help'
    String title = 'Help'
    String description = 'List available Howto actions.'

    @Override
    int invoke(Howto howto, List<String> args) {
        printPlain(howto, args)
        0
    }

    private void printPlain(Howto howto, List<String> args) {
        int count = 0
        List<String> tips = []
        Map<String, Map<String, String>> cmds = [:]
        howto.detectedActions.each { DiscoveredAction action ->
            if (action.name == 'help' && action.type == 'help') {
                return
            }
            if (!args || action.name in args) {
                cmds.put(action.name, [description: action.description, invocationString: action.invocationString?.trim(), type: action.type])
                count++
            }
        }

        if (args?.size() < 1) {
            println Ansi.AUTO.string("How To")
            println Ansi.AUTO.string("@|faint " + ("_" * 40) + " |@")
            tips << Ansi.AUTO.string("see more: @|green how to|@ @|white [action]|@ [args]")
            tips << Ansi.AUTO.string("do the action: @|green how do|@ @|white [action]|@ [args]")
        } else {
            tips << Ansi.AUTO.string("do the thing with: @|green how do|@ @|white [action]|@ [args]")
            tips << Ansi.AUTO.string("or just: @|green how|@ @|white [action]|@ [args]")
        }
        if (count < 1) {
            println Ansi.AUTO.string("@|faint No actions were found.|@")
        }
        int max = cmds.keySet().collect { it.size() }?.max()
        cmds.each { String name, Map<String, String> data ->
            if (howto.verbose) {
                println Ansi.AUTO.string("@|white ${name}|@: \n")
                if (data.description) {
                    println indentString('    ', data.description.trim())
                    println ''
                }
            } else {
                String named = (name + (" " * ((max + 1) - name.size())))
                def shortDesc = data.description ? data.description.split('[\n\r]', 2)[0] : ''
                if (howto.all) {
                    shortDesc = "[${data.type}] " + shortDesc
                }
                println Ansi.AUTO.string("@|white ${named}|@: ${shortDesc}")
            }
            if (howto.verbose && data.invocationString) {
                println Ansi.AUTO.string('@|magenta ' + indentString('    ', data.invocationString) + '|@')
                println ''
            }
        }
        if (!howto.verbose) {
            tips << Ansi.AUTO.string("use @|green how to -v|@ for more information")
        }

        if (tips) {
            println Ansi.AUTO.string("@|faint " + ("_" * 40) + " |@")
            tips.forEach(s -> println(Ansi.AUTO.string("@|cyan tip:|@ ") + s))
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
