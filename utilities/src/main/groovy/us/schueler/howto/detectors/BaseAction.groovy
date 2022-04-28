package us.schueler.howto.detectors

import groovy.transform.CompileStatic

@CompileStatic
abstract class BaseAction implements us.schueler.howto.model.DiscoveredAction {
    String type
    String name
    String title
    String description
    String invocationString
    File sourceFile
}
