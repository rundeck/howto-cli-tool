package us.schueler.howto.detectors;

import us.schueler.howto.Howto;
import us.schueler.howto.model.DiscoveredAction;

import java.util.List;

public interface Detector {
    public abstract String getName();

    public abstract List<DiscoveredAction> getActions(Howto howto);
}
