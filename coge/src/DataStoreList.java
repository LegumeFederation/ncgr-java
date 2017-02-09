package org.coge.api;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;

/**
 * Encapsulate a Data Store list of string:string maps.
 *
 * @author Sam Hokin
 */
public class DataStoreList {

    String path;
    List<Map<String,String>> items;

    /**
     * Constructor just initializes items.
     */
    protected DataStoreList() {
        items = new ArrayList<Map<String,String>>();
    }

    public List<Map<String,String>> getItems() {
        return items;
    }
    void addItem(Map<String,String> item) {
        items.add(item);
    }

    public String getPath() {
        return path;
    }
    void setPath(String path) {
        this.path = path;
    }
    

    // "items":[
    //          {"name":"cicar.CDCFrontier.gnm1/","order":1,"path":"/iplant/home/shared/Legume_Federation/Cicer_arietinum/cicar.CDCFrontier.gnm1","type":"directory"},
    //          {"name":"cicar.CDCFrontier.gnm1.ann1/","order":1,"path":"/iplant/home/shared/Legume_Federation/Cicer_arietinum/cicar.CDCFrontier.gnm1.ann1","type":"directory"},
    //          {"name":"cicar.CDCFrontier.gnm1.synt1/","order":1,"path":"/iplant/home/shared/Legume_Federation/Cicer_arietinum/cicar.CDCFrontier.gnm1.synt1","type":"directory"}
    //          ],
    // "path":"/iplant/home/shared/Legume_Federation/Cicer_arietinum"

}
        
        

