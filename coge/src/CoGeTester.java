package org.ncgr.coge;

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

        if (args.length!=1) {
            System.err.println("Usage: CoGeTester <datastorePath>");
            System.exit(1);
        }

        String datastorePath = args[0];

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

        
        CoGe coge = new CoGe("https://genomevolution.org/coge/api/v1", "shokin", "ed69edabfa3887e5cfdb9fb9bd5e59a");

        try {

            System.out.println("");
            System.out.println("Searching for data store path: "+datastorePath);
            System.out.println("");
            DataStoreList dsl = coge.listDataStore(datastorePath);
            if (dsl!=null) {
                if (dsl.path!=null) {
                    System.out.println("path:"+dsl.path);
                    System.out.println("");
                }
                if (dsl.items!=null) {
                    for (Map<String,String> item : dsl.items) {
                        for (String key : item.keySet()) {
                            System.out.println(key+":"+item.get(key));
                        }
                        System.out.println("");
                    }
                } else {
                    System.out.println("DataStoreList is not null but items IS null!");
                }
            }
            
        } catch (Exception ex) {
            System.err.println(ex.toString());
            System.exit(1);
        }

    }

}
        
        

