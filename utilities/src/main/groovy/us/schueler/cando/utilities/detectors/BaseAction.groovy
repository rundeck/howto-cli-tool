package us.schueler.cando.utilities.detectors

import groovy.transform.CompileStatic
import us.schueler.cando.utilities.model.DiscoveredAction

@CompileStatic
abstract class BaseAction implements DiscoveredAction {
    String type
    String name
    String title
    String description
    String invocationString
    File sourceFile
}
