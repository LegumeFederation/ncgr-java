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
     *
     * @param searchTerm a text string to search on
     */
    public List<Organism> searchOrganism(String searchTerm) throws IOException, JSONException {
        List<Organism> organisms = new ArrayList<Organism>();
        for (CoGeObject object : search("organisms", searchTerm)) {
            Organism o = fetchOrganism(object.id);
            organisms.add(o);
        }
        return organisms;
    }

    /**
     * Organism fetch - used to populate the genomes
     * GET [base_url/organisms/id]
     * 
     * @param id the organism id
     */
    public Organism fetchOrganism(int id) throws IOException, JSONException {
        // generic fetch
        JSONObject jo = fetch("organisms", id);
        String name = jo.getString("name");
        String description = jo.getString("description");
        Organism organism = new Organism(id, name, description);
        // genomes
        List<Integer> genomes = new ArrayList<Integer>();
        JSONArray genomeStrings = jo.getJSONArray("genomes");
        for (int i=0; i<genomeStrings.length(); i++) {
            genomes.add(Integer.parseInt(genomeStrings.get(i).toString()));
        }
        organism.setGenomes(genomes);
        // done
        return organism;
    }

    /**
     * Genome search
     * GET [base_url/genomes/search/term]
     *
     * @param searchTerm a text string to search on
     */
    public List<Genome> searchGenome(String searchTerm) throws IOException, JSONException {
        List<Genome> genomes = new ArrayList<Genome>();
        for (CoGeObject object : search("genomes", searchTerm)) {
            Genome o = fetchGenome(object.id);
            genomes.add(o);
        }
        return genomes;
    }

    /**
     * Genome fetch - used to populate the genomes
     * GET [base_url/genomes/id]
     * 
     * @param id the genome id
     */
    public Genome fetchGenome(int id) throws IOException, JSONException {
        // generic fetch
        JSONObject jo = fetch("genomes", id);
        String name = jo.getString("name");
        String description = jo.getString("description");
        Genome genome = new Genome(id, name, description);
        // genome specific stuff
        genome.setLink(jo.getString("link"));
        genome.setVersion(jo.getString("version"));
        JSONObject org = jo.getJSONObject("organism");
        genome.setOrganism(new Organism(org.getInt("id"), org.getString("name"), org.getString("description")));
        genome.setSequenceType(jo.getString("sequence_type"));
        genome.setRestricted(jo.getBoolean("restricted"));
        genome.setChromosomeCount(jo.getInt("chromosome_count"));
        List<Metadata> metadata = new ArrayList<Metadata>();
        JSONArray metarray = jo.getJSONArray("additional_metadata");
        for (int i=0; i<metarray.length(); i++) {
            JSONObject meta = metarray.getJSONObject(i);
            metadata.add(new Metadata(meta.getString("type_group"), meta.getString("type"), meta.getString("text"), meta.getString("link")));
        }
        genome.setAdditionalMetadata(metadata);
        List<Integer> experiments = new ArrayList<Integer>();
        JSONArray exparray = jo.getJSONArray("experiments");
        for (int i=0; i<exparray.length(); i++) {
            experiments.add(exparray.getInt(i));
        }
        genome.setExperiments(experiments);
        // done
        return genome;
    }

    

    /**
     * Generic search method
     */
    protected List<CoGeObject> search(String orgKey, String searchTerm) throws IOException, JSONException {
        String url = baseUrl+orgKey+"/search/"+searchTerm.replaceAll(" ","%20"); // should use a special-purpose method for this
        List<CoGeObject> objects = new ArrayList<CoGeObject>();
        JSONResource jr  = resty.json(url);
        JSONObject jo = jr.object();
        Iterator<String> joit = jo.keys();
        while (joit.hasNext()) {
            String jkey = joit.next();
            if (jkey.equals(orgKey)) {
                JSONArray ja = jo.getJSONArray(jkey);
                for (int i=0; i<ja.length(); i++) {
                    JSONObject jjo = ja.getJSONObject(i);
                    int id = Integer.parseInt(jjo.getString("id"));
                    String name = jjo.getString("name");
                    String description = jjo.getString("description");
                    objects.add(new CoGeObject(id, name, description));
                }
            }
        }
        return objects;
    }

    /**
     * Generic fetch method
     */
    protected JSONObject fetch(String orgKey, int id) throws IOException, JSONException {
        String url = baseUrl+orgKey+"/"+id;
        JSONResource jr = resty.json(url);
        return jr.object();
    }

    /**
     * Print out an arbitrary JSON response, for testing purposes.
     *
     * @param url the full API url
     */
    void printResponse(String url) throws IOException, JSONException {
        JSONResource jr  = resty.json(url);
        JSONObject jo = jr.object();
        System.out.println(jo.toString());
    }

    /**
     * Return a nicely readable error message from an IOException containing the error JSON.
     */
    static String getErrorMessage(IOException ex) throws JSONException {
        String[] parts = ex.getMessage().split("\n");
        JSONObject json = new JSONObject(parts[1]);
        JSONObject error = json.getJSONObject("error");
        Iterator<String> keys = error.keys();
        String errorType = keys.next();
        String errorReason = error.getString(errorType);
        return errorType+" error:"+errorReason;
    }

    
}
