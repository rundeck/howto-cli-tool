package us.schueler.cando.utilities

import groovy.transform.CompileStatic
import us.schueler.cando.utilities.detectors.Detector
import us.schueler.cando.utilities.model.DiscoveredAction

@CompileStatic
class Cando {
    File baseDir
    List<Detector> detectors = []
    boolean verbose

    Cando(File baseDir) {
        this.baseDir = baseDir
    }
    static ServiceLoader<Detector> detectorLoader = ServiceLoader.load(Detector)

    static Cando create(File file) {
        Cando cando = new Cando(file)
        for (Detector detector : detectorLoader) {
            cando.detectors.add detector
        }
        cando
    }

    List<Detector> detect() {
        return detectors
    }

    void invoke(String name, List<String> args) {
        DiscoveredAction action = findAction(name)
        if (!action) {
            println "Action not found $name. Try: cando help"
            return
        }
        action.invoke(this, args)
    }


    DiscoveredAction findAction(String name) {
        String type = '*'
        String action = name
        if (name.contains(':')) {
            def split = name.split(':', 2)
            if (split.length == 2) {
                type = split[0]
                action = split[1]
            }
        }
        for (Detector detector : detectors) {
            def local = detector.getActions(this).find {
                it.name == action && (type in [it.type, '*'])
            }
            if (local) {
                return local
            }
        }
    }
}
