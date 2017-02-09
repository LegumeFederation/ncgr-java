package org.coge.api;

import java.util.List;

/**
 * Encapsulate a Group record.
 *
 * @author Sam Hokin
 */
public class Group extends CoGeObject {

    String role;
    List<Integer> users;

    /**
     * Construct given id, name, description but not groups.
     */
    protected Group(int id, String name, String description, String role) {
        super(id, name, description);
        this.role = role;
    }

    void setUsers(List<Integer> users) {
        this.users = users;
    }
}
        
        

