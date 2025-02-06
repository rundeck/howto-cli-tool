package org.rundeck.howto.detectors.help;

import org.rundeck.howto.Howto;
import org.rundeck.howto.detectors.Detector;
import org.rundeck.howto.model.DiscoveredAction;

import java.util.ArrayList;
import java.util.List;

public class HelpDetector implements Detector {
    @Override
    public List<DiscoveredAction> getActions(Howto howto) {
        List<DiscoveredAction> actions = new ArrayList<DiscoveredAction>();
        actions.add(new HelpAction());
        return actions;
    }

    public final String getName() {
        return name;
    }

    private final String name = "help";
}
