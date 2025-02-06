package org.rundeck.howto;

import org.rundeck.howto.detectors.Detector;
import org.rundeck.howto.detectors.help.HelpDetector;
import org.rundeck.howto.detectors.markdown.MarkdownDetector;
import org.rundeck.howto.model.DiscoveredAction;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class Howto implements HowtoApp {
    private static final ServiceLoader<Detector> detectorLoader = ServiceLoader.load(Detector.class);

    private final File baseDir;
    private boolean verbose;
    private boolean all;
    private boolean debug;
    private List<String> excludeTypes = new ArrayList<>();

    public Howto(File baseDir) {
        this.baseDir = baseDir;
    }

    public static Howto create(File file) {
        return new Howto(file);
    }

    public List<DiscoveredAction> getDetectedActions() {
        return getDetectedActions(this.all);
    }

    private List<DiscoveredAction> allFound;
    private List<DiscoveredAction> baseFound;

    public List<DiscoveredAction> getDetectedActions(boolean all) {
        if (all && null != allFound) {
            return allFound;
        }
        if (!all && null != baseFound) {
            return baseFound;
        }
        List<DiscoveredAction> actions = new ArrayList<>(new HelpDetector().getActions(this));
        List<DiscoveredAction> detectedActions = new ArrayList<>();
        MarkdownDetector markdownDetector = new MarkdownDetector();
        if (!isExcludedDetector(markdownDetector)) {
            List<DiscoveredAction> mdactions = markdownDetector.getActions(this);
            if (mdactions != null && mdactions.size() > 0) {
                detectedActions.addAll(mdactions);
            }
        }

        if (detectedActions.size() < 1 || all) {
            for (Detector detector : getDetectors()) {
                if (isExcludedDetector(detector)) {
                    if (isDebug()) {
                        System.err.println("Excluded detector: " + detector.getName());
                    }
                    continue;
                }
                if (isDebug()) {
                    System.err.println("Loaded detector: " + detector.getName());
                }
                detectedActions.addAll(detector.getActions(this));
            }

        }
        actions.addAll(detectedActions);
        if (all) {
            allFound = detectedActions;
        }
        if (!all) {
            baseFound = detectedActions;
        }

        return actions;
    }

    private boolean isExcludedDetector(Detector detector) {
        return null != excludeTypes && excludeTypes.contains(detector.getName());
    }

    public int invoke(final String name, List<String> args) {
        DiscoveredAction action = findInvocationAction(name);
        if (action == null) {
            return -1;
        }

        return action.invoke(this, args);
    }

    /**
     * @param name action name
     * @return possible suggested actions for the input action name
     */
    public List<DiscoveredAction> suggestions(final String name) {
        return getDetectedActions().stream().filter(nameContains(name)).collect(Collectors.toList());
    }

    private DiscoveredAction findInvocationAction(String name) {
        Optional<DiscoveredAction> match = getDetectedActions().stream().filter(equalsPrefix(name)).findFirst();
        if (match.isPresent()) {
            return match.get();
        }
        setVerbose(true);
        //find prefixed
        List<DiscoveredAction> prefixed = getDetectedActions().stream().filter(startsWith(name)).collect(Collectors.toList());
        if (prefixed.size() == 1) {
            return prefixed.get(0);
        }

        //find hyphen-abbreviated.  some-action can be abbreviated s-a
        if (!name.matches("[a-z]-[a-z]")) {
            return null;
        }

        List<DiscoveredAction> abbrev = getDetectedActions().stream().filter((it) -> abbreviatedHyphenated(name, it.getName())).collect(Collectors.toList());
        if (abbrev.size() == 1) {
            return abbrev.get(0);
        }
        return null;
    }

    static boolean abbreviationCamelcase(String input, String name) {
        char[] split = input.toCharArray();
        String[] parts = name.split("-");
        if (split.length <= parts.length && split.length > 0) {
            for (int i = 0; i < split.length; i++) {
                if (Character.toLowerCase(parts[i].charAt(0)) != Character.toLowerCase(split[i])) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    public static boolean abbreviatedHyphenated(String input, String name) {
        String[] inputParts = input.split("-");
        String[] nameParts = name.split("-");
        if (inputParts.length <= nameParts.length && inputParts.length > 0) {
            for (int i = 0; i < inputParts.length; i++) {
                if (inputParts[i].length() < 1) {
                    return false;
                }
                if (!nameParts[i].toLowerCase().startsWith(inputParts[i].toLowerCase())) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    private Predicate<DiscoveredAction> equalsPrefix(String name) {
        return (it) -> it.getName().equals(name);
    }

    private Predicate<DiscoveredAction> nameContains(String name) {
        return (it) -> it.getName().contains(name);
    }

    private Predicate<DiscoveredAction> startsWith(String name) {
        return (it) -> it.getName().startsWith(name);
    }

    public File getBaseDir() {
        return baseDir;
    }


    public List<Detector> getDetectors() {
        List<Detector> list = new ArrayList<>();
        for (Detector detector : detectorLoader) {
            list.add(detector);
        }
        return list;
    }


    public boolean isVerbose() {
        return verbose;
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    public boolean getAll() {
        return all;
    }

    public boolean isAll() {
        return all;
    }

    public void setAll(boolean all) {
        this.all = all;
    }

    public boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public List<String> getExcludeTypes() {
        return excludeTypes;
    }

    public void setExcludeTypes(List<String> excludeTypes) {
        this.excludeTypes = excludeTypes;
    }

}
