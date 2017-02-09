package org.ncgr.coge;

import java.io.IOException;
import java.util.Iterator;
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
            System.err.println("Usage: CoGeTester <featureTerm>");
            System.exit(1);
        }

        String featureTerm = args[0];
        
        CoGe coge = new CoGe("https://genomevolution.org/coge/api/v1");

        try {

            System.out.println("");
            System.out.println("Searching for feature with:"+featureTerm);
            List<Feature> features = coge.searchFeature(featureTerm);
            for (Feature f : features) {
                Feature feature = coge.fetchFeature(f.id);
                System.out.println("");
                System.out.println("id:\t"+feature.id);
                System.out.println("type:\t"+feature.type);
                System.out.println("chr:\t"+feature.chromosome);
                if (feature.genome!=null) System.out.println("genome:\t"+feature.genome.toString());
                System.out.println("start:\t"+feature.start);
                System.out.println("stop:\t"+feature.stop);
                System.out.println("strand:\t"+feature.strand);
                System.out.println("seq:\t"+feature.sequence);
            }
            
        } catch (Exception ex) {
            System.err.println(ex.toString());
            System.exit(1);
        }

    }

}
        
        

