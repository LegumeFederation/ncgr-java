package org.ncgr.coge;

import java.util.List;
import java.util.ArrayList;

/**
 * The abstract object which contains data and methods common to all objects.
 *
 * @author Sam Hokin
 */
public class CoGeObject {

    int id;
    String name;
    String description;

    /**
     * Construct given id, name and description.
     */
    protected CoGeObject(int id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }

    /**
     * A copy constructor so we can instantiate child classes from a CoGeObject.
     */
    protected CoGeObject(CoGeObject object) {
        this.id = object.id;
        this.name = object.name;
        this.description = object.description;
    }

    /**
     * Return the id.
     */
    public int getId() {
        return id;
    }

    /**
     * Return the name.
     */
    public String getName() {
        return name;
    }

    /**
     * Return the description.
     */
    public String getDescription() {
        return description;
    }

}
