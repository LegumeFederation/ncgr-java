package org.ncgr.coge;

/**
 * Holds a piece of generic metadata associated with many objects.
 *
 * @author Sam Hokin
 */
public class Metadata {

    String typeGroup;
    String type;
    String text;
    String link;

    /**
     * Standard constructor
     */
    Metadata(String typeGroup, String type, String text, String link) {
        this.typeGroup = typeGroup;
        this.type = type;
        this.text = text;
        this.link = link;
    }

    public String getTypeGroup() {
        return typeGroup;
    }

    public String getType() {
        return type;
    }

    public String getText() {
        return text;
    }

    public String getLink() {
        return link;
    }

}
