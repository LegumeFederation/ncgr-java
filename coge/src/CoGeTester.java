import org.coge.api.*;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import us.monoid.json.JSONArray;
import us.monoid.json.JSONObject;
import us.monoid.web.Resty;
import us.monoid.web.JSONResource;

/**
 * Test the CoGe class.
 */
public class CoGeTester {
    
    public static void main(String[] args) {

        if (args.length!=2) {
            System.err.println("Usage: CoGeTester <token> <featureTerm>");
            System.exit(1);
        }

        String token = args[0];
        String featureTerm = args[1];

        // Successfully created client CoGe Java API
        // key: Go5PXacQcprIA5vFADpkVsDqyAka 
        // secret: XIqxKkiyHvobK3T5L3z_nFCNK10a

        // need something like this to get token!
        //
        // -k insecure
        // -d data
        // -H extra header
        //
        // CLIENT=R281UFhhY1FjcHJJQTV2RkFEcGtWc0RxeUFrYTpYSXF4S2tpeUh2b2JLM1Q1TDN6X25GQ05LMTBh
        //
        // curl -k -d "grant_type=client_credentials" -H "Authorization: Basic $CLIENT, Content-Type: application/x-www-form-urlencoded" https://agave.iplantc.org/token
        //
        // {"scope":"am_application_scope default","token_type":"bearer","expires_in":14017,"access_token":"ed69edabfa3887e5cfdb9fb9bd5e59a"}

        
        CoGe coge = new CoGe("https://genomevolution.org/coge/api/v1", "shokin", token);

        try {

            System.out.println("");
            System.out.println("Searching for feature: "+featureTerm);
            System.out.println("");
            List<Feature> features = coge.searchFeature(featureTerm);
            
            // int id;
            // String type;
            
            // String chromosome;
            // Genome genome;
            // int start;
            // int stop;
            // int strand;
            // String sequence;

            for (Feature feature : features) {
                System.out.println("id:\t"+feature.getId());
                System.out.println("type:\t"+feature.getType());
                System.out.println("chr:\t"+feature.getChromosome());
                System.out.println("genome:\t"+feature.getGenome().toString());
                System.out.println("start:\t"+feature.getStart());
                System.out.println("stop:\t"+feature.getStop());
                System.out.println("strand:\t"+feature.getStrand());
                System.out.println("sequence:");
                System.out.println(coge.fetchFeatureSequence(feature.getId()));
                System.out.println("");
            }                
            
        } catch (Exception ex) {
            System.err.println(ex.toString());
            System.exit(1);
        }

    }

}
        
        

