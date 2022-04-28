package us.schueler.howto.detectors.npm

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.ObjectMapper
import groovy.transform.CompileStatic
import us.schueler.howto.Howto
import us.schueler.howto.detectors.CommandAction
import us.schueler.howto.detectors.Detector
import us.schueler.howto.model.DiscoveredAction

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
    List<DiscoveredAction> getActions(Howto howto) {
        File jsonFile = new File(howto.baseDir, "package.json")
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
                    description: """Runs npm script "${k}"

> ${v}""" ,
                    invocationString: 'npm run ' + k,
                    sourceFile: jsonFile
            ))
        }

        return actions
    }
}
