package us.schueler.howto.detectors.help;

import us.schueler.howto.Howto;
import us.schueler.howto.detectors.Detector;
import us.schueler.howto.model.DiscoveredAction;

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
