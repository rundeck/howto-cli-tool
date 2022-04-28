package us.schueler.howto.detectors

import us.schueler.howto.Howto
import us.schueler.howto.model.DiscoveredAction

interface Detector {
    String getName()
    List<DiscoveredAction> getActions(Howto howto)
}