package org.ncgr.coge;

import java.util.List;
import java.util.Map;

/**
 * Encapsulate a Experiment record.
 *
 * @author Sam Hokin
 */
public class Experiment extends CoGeObject {

    String link;
    String version;
    int genomeId;
    String source;
    Map<String,String> types;
    boolean restricted;
    List<Metadata> additionalMetadata; // type_group:string, type:string, text:string, link:string

    /**
     * Construct given id, name, description but not experiments.
     */
    protected Experiment(int id, String name, String description) {
        super(id, name, description);
    }

    /**
     * Construct from an instantiated superclass.
     */
    protected Experiment(CoGeObject object) {
        super(object);
    }

    void setLink(String link) {
        this.link = link;
    }

    void setVersion(String version) {
        this.version = version;
    }

    void setGenomeId(int genomeId) {
        this.genomeId = genomeId;
    }

    void setSource(String source) {
        this.source = source;
    }

    void setTypes(Map<String,String> types) {
        this.types = types;
    }
    
    void setRestricted(boolean restricted) {
        this.restricted = restricted;
    }

    void setAdditionalMetadata(List<Metadata> additionalMetadata) {
        this.additionalMetadata = additionalMetadata;
    }

    /**
     * The venerable toString() method.
     */
    public String toString() {
        return "Experiment.toString() not yet written.";
    }

}
        
        

