package us.schueler.howto

import groovy.transform.CompileStatic
import us.schueler.howto.detectors.Detector
import us.schueler.howto.model.DiscoveredAction

@CompileStatic
class Howto {
    File baseDir
    List<Detector> detectors = []
    boolean verbose

    Howto(File baseDir) {
        this.baseDir = baseDir
    }
    static ServiceLoader<Detector> detectorLoader = ServiceLoader.load(Detector)

    static Howto create(File file) {
        Howto howto = new Howto(file)
        for (Detector detector : detectorLoader) {
            howto.detectors.add detector
        }
        howto
    }

    List<Detector> detect() {
        return detectors
    }

    void invoke(String name, List<String> args) {
        DiscoveredAction action = findAction(name)
        if (!action) {
            println "Action not found $name. Try: how help"
            return
        }
        action.invoke(this, args)
    }


    DiscoveredAction findAction(String name) {
        String action = name
        for (Detector detector : detectors) {
            def local = detector.getActions(this).find {
                it.name == action
            }
            if (local) {
                return local
            }
        }
        null
    }
}
