package org.coge.api;

import java.util.List;
import java.util.Map;

/**
 * Encapsulate a Notebook record.
 *
 * @author Sam Hokin
 */
public class Notebook extends CoGeObject {

    String type;
    boolean restricted;
    List<Metadata> additionalMetadata; // type_group:string, type:string, text:string, link:string
    List<Item> items;

    /**
     * Construct given id, name, description but not notebooks.
     */
    protected Notebook(int id, String name, String description) {
        super(id, name, description);
    }

    /**
     * Construct from an instantiated superclass.
     */
    protected Notebook(CoGeObject object) {
        super(object);
    }

    void setType(String type) {
        this.type = type;
    }

    void setRestricted(boolean restricted) {
        this.restricted = restricted;
    }

    void setAdditionalMetadata(List<Metadata> additionalMetadata) {
        this.additionalMetadata = additionalMetadata;
    }

    void setItems(List<Item> items) {
        this.items = items;
    }

    /**
     * The venerable toString() method.
     */
    public String toString() {
        return "Notebook.toString() not yet written.";
    }

}
        
        

