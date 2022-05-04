package us.schueler.howto.detectors.npm;

import com.fasterxml.jackson.databind.ObjectMapper;
import us.schueler.howto.Howto;
import us.schueler.howto.detectors.CommandAction;
import us.schueler.howto.detectors.Detector;
import us.schueler.howto.model.DiscoveredAction;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Detect NPM script definitions in package.json
 */
public class NpmDetector implements Detector {
    @Override
    public List<DiscoveredAction> getActions(Howto howto) {
        final File jsonFile = new File(howto.getBaseDir(), "package.json");
        if (!jsonFile.exists()) {
            return new ArrayList<>();
        }

        final List<DiscoveredAction> actions = new ArrayList<>();
        ObjectMapper objectMapper = new ObjectMapper();
        Map packagejson;
        try {
            packagejson = objectMapper.readValue(jsonFile, Map.class);
        } catch (IOException e) {
            if (howto.isDebug()) {
                System.err.println("Error reading json file: " + e);
                e.printStackTrace(System.err);
            }
            return new ArrayList<>();
        }
        Map<String, String> scripts = null;
        if (packagejson.get("scripts") instanceof Map) {
            scripts = (Map<String, String>) packagejson.get("scripts");

        }
        if (scripts == null) {
            return new ArrayList<>();
        }
        scripts.forEach((key, val) -> {
            CommandAction action = new CommandAction();
            action.setType("npm");
            action.setName(key);
            action.setTitle(key);
            action.setDescription("Runs npm script \"" + key + "\"\n\n> " + val);
            action.setInvocationString("npm run" + key);
            action.setSourceFile(jsonFile);
            actions.add(action);
        });

        return actions;
    }

    public final String getName() {
        return "npm";
    }

}
