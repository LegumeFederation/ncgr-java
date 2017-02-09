package org.coge.api;

/**
 * Encapsulate an item.
 *
 * @author Sam Hokin
 */
public class Item {

    int id;
    String type;
    String role;

    /**
     * Construct with an id and type
     */
    protected Item(int id, String type) {
        this.id = id;
        this.type = type;
    }

    /**
     * Construct with an id, type and role
     */
    protected Item(int id, String type, String role) {
        this.id = id;
        this.type = type;
        this.role = role;
    }

}
