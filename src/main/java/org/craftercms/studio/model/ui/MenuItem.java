package org.craftercms.studio.model.ui;

/**
 * Represents an item that is displayed in a UI menu.
 *
 * @author avasquez
 */
public class MenuItem {

    private String id;
    private String label;
    private String icon;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }
    
}
