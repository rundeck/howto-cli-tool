package us.schueler.howto.app

import groovy.transform.CompileStatic
import picocli.CommandLine

import static us.schueler.howto.Version.VERSION

@CompileStatic
class VersionProvider implements CommandLine.IVersionProvider {
    @Override
    String[] getVersion() throws Exception {
        return [VERSION].toArray(new String[1])
    }
}
