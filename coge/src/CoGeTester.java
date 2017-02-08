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

        if (args.length!=1) {
            System.err.println("Usage: CoGeTester <searchTerm>");
            System.exit(1);
        }

        String searchTerm = args[0];
        
        CoGe coge = new CoGe("https://genomevolution.org/coge/api/v1/");

        try {
            System.out.println("");
            System.out.println("Searching on:"+searchTerm);
            System.out.println("");
            List<Organism> organisms = coge.searchOrganism(searchTerm);
            for (Organism o : organisms) {
                System.out.println("id\t"+o.id);
                System.out.println("name\t"+o.name);
                System.out.println("description\t"+o.description);
                System.out.print("genomes");
                for (Integer genome : o.genomes) {
                    System.out.print("\t"+genome);
                }
                System.out.println("");
                System.out.println("");
            }
        } catch (Exception ex) {
            System.err.println(ex.toString());
            System.exit(1);
        }

    }

}
        
        

