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
    
    // static String BASE_URL = "https://genomevolution.org/coge/api/v1";
    static String BASE_URL = "https://geco.iplantcollaborative.org/coge-qa/coge/api/v1/";
        
    public static void main(String[] args) {

        if (args.length!=2) {
            System.err.println("Usage: CoGeTester <user> <token>");
            System.exit(1);
        }

        String user = args[0];
        String token = args[1];

        // Successfully created client CoGe Java API
        // key: Go5PXacQcprIA5vFADpkVsDqyAka 
        // secret: XIqxKkiyHvobK3T5L3z_nFCNK10a

        // need something like this to get token!
        //
        // CLIENT=R281UFhhY1FjcHJJQTV2RkFEcGtWc0RxeUFrYTpYSXF4S2tpeUh2b2JLM1Q1TDN6X25GQ05LMTBh
        //
        // -k insecure
        // -d data
        // -H extra header
        //
        // curl -k -d "grant_type=client_credentials" -H "Authorization: Basic $CLIENT, Content-Type: application/x-www-form-urlencoded" https://agave.iplantc.org/token
        //
        // Returns:
        //
        // {"scope":"am_application_scope default","token_type":"bearer","expires_in":14017,"access_token":"ed69edabfa3887e5cfdb9fb9bd5e59a"}
        
        CoGe coge = new CoGe(BASE_URL, user, token);

        try {

            String name = "Taxidea taxus";
            
            String description = "cellular organisms; Eukaryota; Opisthokonta; Metazoa; Eumetazoa; Bilateria; Deuterostomia; Chordata; Craniata; Vertebrata; Gnathostomata; Teleostomi; Euteleostomi; Sarcopterygii; Dipnotetrapodomorpha; Tetrapoda; Amniota; Mammalia; Theria; Eutheria; Boreoeutheria; Laurasiatheria; Carnivora; Caniformia; Mustelidae; Taxidiinae; Taxidea";
            
            System.out.println("Attempting to add organism:\t"+name);
            System.out.println("\t\tDescription:\t"+description);
            System.out.println("");
            
            CoGeResponse response = coge.addOrganism(name, description);

            System.out.println(response.toString());
            
        } catch (Exception ex) {

            System.err.println(ex.toString());
            System.exit(1);

        }

    }

}
        
        

