package org.rundeck.howto.model;

import org.rundeck.howto.HowtoApp;

import java.io.File;
import java.util.List;

public interface DiscoveredAction {
    /**
     * Type of discovered actions
     *
     * @return
     */
    public  String getType();

    /**
     * Get action invocation name
     *
     * @return
     */
    public  String getName();

    /**
     * Get action display title
     *
     * @return
     */
    public  String getTitle();

    /**
     * Get action description
     *
     * @return
     */
    public  String getDescription();

    /**
     * @return file source of discovered command, if any
     */
    public  File getSourceFile();

    /**
     * Get action description
     *
     * @return
     */
    public  String getInvocationString();

    /**
     * Invoke the action
     *
     * @param args
     */
    public  int invoke(HowtoApp howto, List<String> args);
}
