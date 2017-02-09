package org.ncgr.coge;

/**
 * Encapsulate a sequence type. Doesn't extend CoGeObject since does not have an id.
 *
 * @author Sam Hokin
 */
public class SequenceType  {

    String name;
    String description;

    /**
     * Construct given name and description
     */
    protected SequenceType(String name, String description) {
        this.name = name;
        this.description = description;
    }

}
        
        

