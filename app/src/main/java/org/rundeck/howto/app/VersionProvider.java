package org.rundeck.howto.app;

import picocli.CommandLine;
import org.rundeck.howto.Version;

public class VersionProvider implements CommandLine.IVersionProvider {
    @Override
    public String[] getVersion() {
        return new String[]{Version.VERSION};
    }

}
