package org.coge.api;

import java.util.List;

/**
 * Encapsulate a Genome record.
 *
 * @author Sam Hokin
 */
public class Genome extends CoGeObject {

    String link;
    String version;
    Organism organism;
    SequenceType sequenceType;
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

    void setSequenceType(SequenceType sequenceType) {
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

    /**
     * The venerable toString() method.
     */
    public String toString() {
        return "id="+id+"; name="+name+"; description="+description +
            "; version="+version+"; organism="+organism.name+"; sequenceType="+sequenceType.name +
            "; restricted="+restricted+"; chromosomeCount="+chromosomeCount;
    }

}
        
        

