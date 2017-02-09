package org.ncgr.coge;

import java.util.List;

/**
 * Encapsulate an Organism record.
 *
 * @author Sam Hokin
 */
public class Organism extends CoGeObject {

    List<Integer> genomes;

    /**
     * Construct given id, name, description and genomes.
     */
    protected Organism(int id, String name, String description) {
        super(id, name, description);
    }

    /**
     * Construct from an instantiated CoGeObject.
     */
    protected Organism(CoGeObject object) {
        super(object);
    }

    /**
     * Set the genomes list.
     */
    protected void setGenomes(List<Integer> genomes) {
        this.genomes = genomes;
    }

}
        
        

