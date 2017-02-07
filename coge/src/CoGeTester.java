package org.ncgr.coge;

import java.util.List;

import us.monoid.json.JSONArray;
import us.monoid.json.JSONObject;
import us.monoid.web.Resty;
import us.monoid.web.JSONResource;

/**
 * Test the CoGe class.
 */
public class CoGeTester {

    public static void main(String[] args) {

        String searchTerm = args[0];
        
        CoGe coge = new CoGe("https://genomevolution.org/coge/api/v1/");

        try {
            System.out.println("");
            System.out.println("Searching on:"+searchTerm);
            System.out.println("");
            List<Organism> organisms = coge.searchOrganism(searchTerm);
            for (Organism o : organisms) {
                System.out.println("id:"+o.id);
                System.out.println("name:"+o.name);
                System.out.println("description:"+o.description);
                System.out.println("");
            }
        } catch (Exception ex) {
            System.err.println(ex.toString());
            System.exit(1);
        }

    }

}
        
        

