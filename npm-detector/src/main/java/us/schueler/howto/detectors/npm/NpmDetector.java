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
        PackageJson packagejson;
        try {
            packagejson = objectMapper.readValue(jsonFile, PackageJson.class);
        } catch (IOException e) {
            if (howto.isDebug()) {
                System.err.println("Error reading json file: " + e);
                e.printStackTrace(System.err);
            }
            return new ArrayList<>();
        }
        Map<String, String> scripts = packagejson.scripts;
        if (scripts == null) {
            return new ArrayList<>();
        }
        //automatic npm install command
        actions.add(createNpmInstall(jsonFile));
        final File packageLock = new File(howto.getBaseDir(), "package-lock.json");
        if (packageLock.exists()) {
            //automatic npm ci
            actions.add(createNpmCi(packageLock));
        }

        scripts.forEach((key, val) -> {
            CommandAction action = new CommandAction();
            action.setType("npm");
            action.setName(key);
            action.setTitle(key);
            action.setDescription("Runs npm script \"" + key + "\"\n\n> " + val);
            action.setInvocationString("npm run " + key);
            action.setSourceFile(jsonFile);
            actions.add(action);
        });

        return actions;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    static class PackageJson {
        public Map<String, String> scripts;
    }

    private CommandAction createNpmInstall(File jsonFile) {
        CommandAction action = new CommandAction();
        action.setType("npm");
        action.setName("npm-install");
        action.setTitle("NPM Install");
        action.setDescription("Runs npm install");
        action.setInvocationString("npm install");
        action.setSourceFile(jsonFile);
        return action;
    }

    private CommandAction createNpmCi(File jsonFile) {
        CommandAction action = new CommandAction();
        action.setType("npm");
        action.setName("npm-ci");
        action.setTitle("NPM CI");
        action.setDescription("Runs npm ci");
        action.setInvocationString("npm ci");
        action.setSourceFile(jsonFile);
        return action;
    }

    public final String getName() {
        return "npm";
    }

}
