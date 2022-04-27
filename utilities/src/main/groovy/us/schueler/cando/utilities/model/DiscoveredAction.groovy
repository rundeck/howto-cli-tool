package us.schueler.cando.utilities.model

import us.schueler.cando.utilities.Cando

interface DiscoveredAction {
    /**
     * Type of discovered actions
     * @return
     */
    String getType()
    /**
     * Get action invocation name
     * @return
     */
    String getName()
    /**
     * Get action display title
     * @return
     */
    String getTitle()
    /**
     * Get action description
     * @return
     */
    String getDescription()
    /**
     *
     * @return file source of discovered command, if any
     */
    File getSourceFile()
    /**
     * Get action description
     * @return
     */
    String getInvocationString()

    /**
     * Invoke the action
     * @param args
     */
    void invoke(Cando cando, List<String> args)
}