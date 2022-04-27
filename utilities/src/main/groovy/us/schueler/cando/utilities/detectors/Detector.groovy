package us.schueler.cando.utilities.detectors

import us.schueler.cando.utilities.Cando
import us.schueler.cando.utilities.model.DiscoveredAction

interface Detector {
    String getName()
    List<DiscoveredAction> getActions(Cando cando)
}