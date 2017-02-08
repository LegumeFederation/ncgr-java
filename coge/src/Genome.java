package org.ncgr.coge;

import java.util.List;
import java.util.ArrayList;

/**
 * Encapsulate a Genome record.
 *
 * @author Sam Hokin
 */
public class Genome {

    int id;
    String name;
    String description;

    String link;
    String version;
    Organism organism;
    String sequenceType;
    boolean restricted;
    int chromosomeCount;
    List<Metadata> additionalMetadata;
    List<Integer> experiments;

    /**
     * Construct given id, name, description but not genomes.
     */
    public Genome(int id, String name, String description) {
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
        
        

