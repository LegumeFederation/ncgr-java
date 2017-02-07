package org.ncgr.coge;

/**
 * Encapsulate an Organism record.
 *
 * @author Sam Hokin
 */
public class Organism {

    int id;
    String name;
    String description;

    /**
     * Construct given the values.
     */
    public Organism(int id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
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
        
        

