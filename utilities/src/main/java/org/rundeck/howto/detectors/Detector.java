package org.rundeck.howto.detectors;

import org.rundeck.howto.Howto;
import org.rundeck.howto.model.DiscoveredAction;

import java.util.List;

public interface Detector {
    public abstract String getName();

    public abstract List<DiscoveredAction> getActions(Howto howto);
}
