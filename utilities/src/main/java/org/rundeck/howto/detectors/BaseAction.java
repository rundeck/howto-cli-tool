package org.rundeck.howto.detectors;

import org.rundeck.howto.model.DiscoveredAction;

import java.io.File;

public abstract class BaseAction implements DiscoveredAction {
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getInvocationString() {
        return invocationString;
    }

    public void setInvocationString(String invocationString) {
        this.invocationString = invocationString;
    }

    public File getSourceFile() {
        return sourceFile;
    }

    public void setSourceFile(File sourceFile) {
        this.sourceFile = sourceFile;
    }

    private String type;
    private String name;
    private String title;
    private String description;
    private String invocationString;
    private File sourceFile;
}
