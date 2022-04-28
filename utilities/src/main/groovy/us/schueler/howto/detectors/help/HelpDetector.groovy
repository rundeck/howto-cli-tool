package us.schueler.howto.detectors.help

import groovy.transform.CompileStatic
import us.schueler.howto.Howto

@CompileStatic
class HelpDetector implements us.schueler.howto.detectors.Detector {
    final String name = 'help'

    @Override
    List<us.schueler.howto.model.DiscoveredAction> getActions(Howto howto) {
        List<us.schueler.howto.model.DiscoveredAction> actions = new ArrayList<>()
        actions.add new HelpAction(
        )
        actions
    }
}
