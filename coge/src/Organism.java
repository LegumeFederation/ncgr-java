package org.ncgr.coge;

import java.util.List;
import java.util.ArrayList;

/**
 * Encapsulate an Organism record.
 *
 * @author Sam Hokin
 */
public class Organism {

    int id;
    String name;
    String description;
    List<Integer> genomes;

    /**
     * Construct given id, name, description but not genomes.
     */
    public Organism(int id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }

    /**
     * Construct given id, name, description and genomes.
     */
    public Organism(int id, String name, String description, List<Integer> genomes) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.genomes = genomes;
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
        
        

