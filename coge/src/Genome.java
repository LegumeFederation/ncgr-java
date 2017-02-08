package org.ncgr.coge;

import java.util.List;
import java.util.ArrayList;

/**
 * Encapsulate a Genome record.
 *
 * @author Sam Hokin
 */
public class Genome extends CoGeObject {

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
    protected Genome(int id, String name, String description) {
        super(id, name, description);
    }

    /**
     * Construct from an instantiated superclass.
     */
    protected Genome(CoGeObject object) {
        super(object);
    }

    void setLink(String link) {
        this.link = link;
    }

    void setVersion(String version) {
        this.version = version;
    }

    void setOrganism(Organism organism) {
        this.organism = organism;
    }

    void setSequenceType(String sequenceType) {
        this.sequenceType = sequenceType;
    }

    void setRestricted(boolean restricted) {
        this.restricted = restricted;
    }

    void setChromosomeCount(int chromosomeCount) {
        this.chromosomeCount = chromosomeCount;
    }

    void setAdditionalMetadata(List<Metadata> additionalMetadata) {
        this.additionalMetadata = additionalMetadata;
    }

    void setExperiments(List<Integer> experiments) {
        this.experiments = experiments;
    }

}
        
        

