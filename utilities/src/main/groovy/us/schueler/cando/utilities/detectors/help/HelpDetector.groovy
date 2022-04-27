package us.schueler.cando.utilities.detectors.help

import groovy.transform.CompileStatic
import us.schueler.cando.utilities.Cando
import us.schueler.cando.utilities.detectors.Detector
import us.schueler.cando.utilities.model.DiscoveredAction

@CompileStatic
class HelpDetector implements Detector {
    final String name = 'help'

    @Override
    List<DiscoveredAction> getActions(Cando cando) {
        List<DiscoveredAction> actions = new ArrayList<>()
        actions.add new HelpAction(
                type: 'help',
                name: 'help',
                title: 'Help',
                description: 'List available cando actions'
        )
        actions
    }
}
