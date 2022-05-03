package us.schueler.howto.detectors.npm;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import us.schueler.howto.Howto;
import us.schueler.howto.detectors.CommandAction;
import us.schueler.howto.detectors.Detector;
import us.schueler.howto.model.DiscoveredAction;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
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


        final List<DiscoveredAction> actions = new ArrayList<DiscoveredAction>();
        ObjectMapper objectMapper = new ObjectMapper();
        Package packagejson = null;
        try {
            packagejson = objectMapper.readValue(jsonFile, Package.class);
        } catch (IOException e) {
            return new ArrayList<>();
        }
        packagejson.getScripts().forEach((key, val) -> {
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

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Package {
        public Map<String, String> getScripts() {
            return scripts;
        }

        public void setScripts(Map<String, String> scripts) {
            this.scripts = scripts;
        }

        private Map<String, String> scripts = new HashMap<String, String>();
    }
}
