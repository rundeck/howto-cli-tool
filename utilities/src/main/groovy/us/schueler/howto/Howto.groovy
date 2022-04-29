package us.schueler.howto

import groovy.transform.CompileStatic
import groovy.transform.Memoized
import us.schueler.howto.detectors.Detector
import us.schueler.howto.detectors.help.HelpDetector
import us.schueler.howto.detectors.markdown.MarkdownDetector
import us.schueler.howto.model.DiscoveredAction

@CompileStatic
class Howto {
    static ServiceLoader<Detector> detectorLoader = ServiceLoader.load(Detector)
    File baseDir
    List<Detector> detectors = detectorLoader.toList()

    boolean verbose
    boolean all

    Howto(File baseDir) {
        this.baseDir = baseDir
    }

    static Howto create(File file) {
        new Howto(file)
    }

    List<DiscoveredAction> getDetectedActions() {
        return getDetectedActions(this.all)
    }

    //cache results
    @Memoized
    List<DiscoveredAction> getDetectedActions(boolean all) {
        List<DiscoveredAction> detectedActions = []
        detectedActions.addAll(new HelpDetector().getActions(this))
        def mdactions = new MarkdownDetector().getActions(this)
        if (mdactions) {
            detectedActions.addAll(mdactions)
        }
        if (!mdactions || all) {
            for (Detector detector : detectors) {
                detectedActions.addAll(detector.getActions(this))
            }
        }
        detectedActions
    }

    int invoke(String name, List<String> args) {
        DiscoveredAction action = detectedActions.find { it.name == name }
        if (!action) {
            return -1
        }
        action.invoke(this, args)
    }


}
