package org.ncgr.coge;

import java.io.IOException;

import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;

import us.monoid.json.JSONArray;
import us.monoid.json.JSONException;
import us.monoid.json.JSONObject;
import us.monoid.web.Resty;
import us.monoid.web.JSONResource;

/**
 * Core class to make REST calls againt a CoGe web service.
 *
 * @author Sam Hokin
 */
public class CoGe {

    String baseUrl;
    Resty resty;

    /**
     * Construct given a base URL, like https://genomevolution.org/coge/api/v1/
     */
    public CoGe(String baseUrl) {
        this.baseUrl = baseUrl;
        this.resty = new Resty();
    }


    /**
     * Organism search
     * GET [base_url/organisms/search/term]
     */
    public List<Organism> searchOrganism(String searchTerm) throws IOException, JSONException {

        List<Organism> organisms = new ArrayList<Organism>();
        String url = baseUrl+"organisms/search/"+searchTerm.replaceAll(" ","%20"); // should use a special-purpose method for this
        JSONResource jr  = resty.json(url);
        JSONObject jo = jr.object();

        Iterator<String> joit = jo.keys();
        while (joit.hasNext()) {
            String jkey = joit.next();
            if (jkey.equals("organisms")) {
                JSONArray ja = jo.getJSONArray(jkey);
                for (int i=0; i<ja.length(); i++) {
                    JSONObject jjo = ja.getJSONObject(i);
                    int id = Integer.parseInt(jjo.getString("id"));
                    String name = jjo.getString("name");
                    String description = jjo.getString("description");
                    organisms.add(new Organism(id, name, description));
                }
            }
        }

        return organisms;

    }

}
        
        

