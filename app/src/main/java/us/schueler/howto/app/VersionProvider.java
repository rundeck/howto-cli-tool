package us.schueler.howto.app;

import picocli.CommandLine;
import us.schueler.howto.Version;

public class VersionProvider implements CommandLine.IVersionProvider {
    @Override
    public String[] getVersion() {
        return new String[]{Version.VERSION};
    }

}
