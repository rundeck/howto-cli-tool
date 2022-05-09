package us.schueler.howto;

import us.schueler.howto.detectors.Detector;
import us.schueler.howto.detectors.help.HelpDetector;
import us.schueler.howto.detectors.markdown.MarkdownDetector;
import us.schueler.howto.model.DiscoveredAction;

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
        List<DiscoveredAction> detectedActions = new ArrayList<>(new HelpDetector().getActions(this));
        List<DiscoveredAction> mdactions = new MarkdownDetector().getActions(this);
        if (mdactions != null && mdactions.size() > 0) {
            detectedActions.addAll(mdactions);
        }

        if (mdactions == null || mdactions.size() < 1 || all) {
            for (Detector detector : getDetectors()) {
                if (isDebug()) {
                    System.err.println("Loaded detector: " + detector.getName());
                }
                detectedActions.addAll(detector.getActions(this));
            }

        }
        if (all) {
            allFound = detectedActions;
        }
        if (!all) {
            baseFound = detectedActions;
        }

        return detectedActions;
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
        //find prefixed
        List<DiscoveredAction> prefixed = getDetectedActions().stream().filter(startsWith(name)).collect(Collectors.toList());
        if (prefixed.size() == 1) {
            return prefixed.get(0);
        }
        return null;
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

}
