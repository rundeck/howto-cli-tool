package us.schueler.cando.detectors.npm

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.ObjectMapper
import groovy.transform.CompileStatic
import us.schueler.cando.utilities.Cando
import us.schueler.cando.utilities.detectors.CommandAction
import us.schueler.cando.utilities.detectors.Detector
import us.schueler.cando.utilities.model.DiscoveredAction

/**
 * Detect NPM script definitions in package.json
 */
@CompileStatic
class NpmDetector implements Detector {
    final String name = 'npm'


    @JsonIgnoreProperties(ignoreUnknown = true)
    static class Package {
        Map<String, String> scripts = new HashMap<>()
    }

    @Override
    List<DiscoveredAction> getActions(Cando cando) {
        File jsonFile = new File(cando.baseDir, "package.json")
        if (!jsonFile.exists()) {
            return []
        }

        List<DiscoveredAction> actions = new ArrayList<>()
        ObjectMapper objectMapper = new ObjectMapper()
        Package packagejson = objectMapper.readValue(jsonFile, Package)
        packagejson.scripts.each { k, v ->
            actions.add(new CommandAction(
                    type: 'npm',
                    name: k,
                    title: k,
                    description: """NPM script: ${k}

* action: `${v}`""" ,
                    invocationString: 'npm run ' + k,
                    sourceFile: jsonFile
            ))
        }

        return actions
    }
}
