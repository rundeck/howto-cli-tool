package us.schueler.cando.utilities.detectors.help

import groovy.transform.CompileStatic
import us.schueler.cando.utilities.Cando
import us.schueler.cando.utilities.detectors.BaseAction
import us.schueler.cando.utilities.detectors.Detector

@CompileStatic
class HelpAction extends BaseAction {
    @Override
    void invoke(Cando cando, List<String> args) {
        int count = 1
        printPlain(cando, count)
//        printMarkdown(cando, count)
    }

    private void printPlain(Cando cando, int count) {
        println """Here's what you can do:

cando run [action] [args]

actions:
"""
        cando.detectors.each { Detector detector ->
            detector.getActions(cando).each {
                if (it.name == 'help' && it.type == 'help' && !cando.verbose) {
                    return
                }
                if (cando.verbose) {
                    println "${it.type}:${it.name}: \n"
                    println "    ${it.description.replaceAll('\\s+$', '')}\n"
                } else {
                    println "${it.name}: ${it.description.replaceAll('\\\\s+\$', '')}"
                }
                if (cando.verbose && it.invocationString) {
                    println "        ${it.invocationString}\n"
                }
                count++
            }
        }
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
