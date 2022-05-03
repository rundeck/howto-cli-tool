package us.schueler.howto;

import us.schueler.howto.model.DiscoveredAction;

import java.io.File;
import java.util.List;

public interface HowtoApp {
    boolean isVerbose();

    boolean isAll();

    File getBaseDir();

    public int invoke(final String name, List<String> args);

    List<DiscoveredAction> getDetectedActions();

    public List<DiscoveredAction> getDetectedActions(boolean all);
}
