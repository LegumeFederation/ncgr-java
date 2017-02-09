package org.ncgr.coge;

import java.io.IOException;
import java.io.InputStream;

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

    ////////// Organism //////////

    /**
     * Organism search
     * GET [base_url/organisms/search/term]
     *
     * @param searchTerm a text string to search on
     */
    public List<Organism> searchOrganism(String searchTerm) throws IOException, JSONException {
        List<Organism> organisms = new ArrayList<Organism>();
        for (CoGeObject object : search("organisms", searchTerm)) {
            organisms.add(new Organism(object));
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
        JSONObject json = fetch("organisms", id);
        return getOrganismFromJSON(json);
    }

    /**
     * Populate an Organism from a JSONObject.
     */
    Organism getOrganismFromJSON(JSONObject json) throws IOException, JSONException {
        int id = json.getInt("id");
        String name = json.getString("name");
        String description = json.getString("description");
        Organism organism = new Organism(id, name, description);
        if (json.has("genomes")) {
            List<Integer> genomes = new ArrayList<Integer>();
            JSONArray genomeStrings = json.getJSONArray("genomes");
            for (int i=0; i<genomeStrings.length(); i++) {
                genomes.add(Integer.parseInt(genomeStrings.get(i).toString()));
            }
            organism.setGenomes(genomes);
        }
        return organism;
    }

    ////////// Genome //////////

    /**
     * Genome search
     * GET [base_url/genomes/search/term]
     *
     * @param searchTerm a text string to search on
     */
    public List<Genome> searchGenome(String searchTerm) throws IOException, JSONException {
        List<Genome> genomes = new ArrayList<Genome>();
        for (CoGeObject object : search("genomes", searchTerm)) {
            Genome g = new Genome(object);
            if (g.name!=null || g.description!=null) genomes.add(g);
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
        JSONObject json = fetch("genomes", id);
        return getGenomeFromJSON(json);
    }

    /**
     * Populate a Genome from a JSONObject.
     */
    Genome getGenomeFromJSON(JSONObject json) throws IOException, JSONException {
        int id = json.getInt("id");
        String name = json.getString("name");
        String description = json.getString("description");
        Genome genome = new Genome(id, name, description);
        if (json.has("link")) genome.setLink(json.getString("link"));
        if (json.has("version")) genome.setVersion(json.getString("version"));
        if (json.has("organism")) {
            JSONObject org = json.getJSONObject("organism");
            genome.setOrganism(getOrganismFromJSON(org));
        }
        if (json.has("sequence_type")) {
            JSONObject sto = json.getJSONObject("sequence_type");
            genome.setSequenceType(new SequenceType(sto.getString("name"), sto.getString("description")));
        }
        if (json.has("restricted")) genome.setRestricted(json.getBoolean("restricted"));
        if (json.has("chromosome_count")) genome.setChromosomeCount(json.getInt("chromosome_count"));
        if (json.has("additional_metadata")) {
            List<Metadata> metadata = new ArrayList<Metadata>();
            JSONArray metarray = json.getJSONArray("additional_metadata");
            for (int i=0; i<metarray.length(); i++) {
                JSONObject meta = metarray.getJSONObject(i);
                metadata.add(new Metadata(meta.getString("type_group"), meta.getString("type"), meta.getString("text"), meta.getString("link")));
            }
            genome.setAdditionalMetadata(metadata);
        }
        if (json.has("experiments")) {
            List<Integer> experiments = new ArrayList<Integer>();
            JSONArray exparray = json.getJSONArray("experiments");
            for (int i=0; i<exparray.length(); i++) {
                experiments.add(exparray.getInt(i));
            }
            genome.setExperiments(experiments);
        }
        return genome;
    }

    /**
     * Genome fetch sequence - grab the full sequence response.
     * GET [​base_url/genomes/​id/sequence]
     *
     * @param id the genome id
     */
    public String fetchGenomeSequence(int id) throws IOException, JSONException {
        String url = baseUrl+"/genomes/"+id+"/sequence";
        JSONResource jr = resty.json(url);
        InputStream stream = jr.stream();
        StringBuffer buffer = new StringBuffer();
        int i = 0;
        while ((i=stream.read())!=-1) buffer.append((char)i);
        return buffer.toString();
    }

    /**
     * Genome fetch sequence - grab a subsequence.
     * GET [​base_url/genomes/​id/sequence/​chr?start=x&stop=y]
     *
     * @param id the genome id
     * @param chr the chromosome name
     * @param start the start index
     * @param stop the stop index
     */
    public String fetchGenomeSequence(int id, String chr, int start, int stop) throws IOException, JSONException {
        String url = baseUrl+"/genomes/"+id+"/sequence/"+chr+"?start="+start+"&stop="+stop;
        JSONResource jr = resty.json(url);
        InputStream stream = jr.stream();
        StringBuffer buffer = new StringBuffer();
        int i = 0;
        while ((i=stream.read())!=-1) buffer.append((char)i);
        return buffer.toString();
    }

    ////////// Feature //////////

    /**
     * Feature search
     * GET [base_url/features/search/term]
     *
     * @param searchTerm a text string to search on
     */
    public List<Feature> searchFeature(String searchTerm) throws IOException, JSONException {
        List<Feature> features = new ArrayList<Feature>();
        String url = baseUrl+"/features/search/"+searchTerm.replaceAll(" ","%20"); // should use a special-purpose method for this
        JSONResource jr  = resty.json(url);
        JSONObject jo = jr.object();
        Iterator<String> joit = jo.keys();
        while (joit.hasNext()) {
            String jkey = joit.next();
            if (jkey.equals("features")) {
                JSONArray ja = jo.getJSONArray(jkey);
                for (int i=0; i<ja.length(); i++) {
                    JSONObject json = ja.getJSONObject(i);
                    Feature f = getFeatureFromJSON(json);
                    features.add(f);
                }
            }
        }
        return features;
    }

    /**
     * Feature Fetch
     * GET [base_url/features/id]
     */
    public Feature fetchFeature(int id)  throws IOException, JSONException {
        String url = baseUrl+"/features/"+id;
        JSONResource jr = resty.json(url);
        JSONObject json = jr.object();
        return getFeatureFromJSON(json);
    }

    /**
     * Populate a Feature from a JSONObject.
     */
    Feature getFeatureFromJSON(JSONObject json) throws IOException, JSONException {
        int id = json.getInt("id"); // all returned objects should have an id
        Feature f = new Feature(id, json.getString("type"));
        if (json.has("chromosome")) f.setChromosome(json.getString("chromosome"));
        if (json.has("genome")) {
            JSONObject go = json.getJSONObject("genome");
            int gid = go.getInt("id");
            Genome g = fetchGenome(gid);
            f.setGenome(g);
        }
        if (json.has("start")) f.setStart(json.getInt("start"));
        if (json.has("stop")) f.setStop(json.getInt("stop"));
        if (json.has("strand")) f.setStrand(json.getInt("strand"));
        if (json.has("sequence")) f.setSequence(json.getString("sequence"));
        return f;
    }

    /**
     * Feature fetch sequence - grab the feature's sequence.
     * GET [​base_url/features/​id/sequence]
     *
     * @param id the feature id
     */
    /* NOT WORKING
    public String fetchFeatureSequence(int id) throws IOException, JSONException {
        String url = baseUrl+"/features/"+id+"/sequence";
        JSONResource jr = resty.json(url);
        InputStream stream = jr.stream();
        StringBuffer buffer = new StringBuffer();
        int i = 0;
        while ((i=stream.read())!=-1) buffer.append((char)i);
        String seq = buffer.toString();
        if (seq.contains("error")) {
            return getErrorMessage(seq);
        } else {
            return seq;
        }
    }
    */

    ////////// Generic //////////

    /**
     * Generic search method
     */
    protected List<CoGeObject> search(String orgKey, String searchTerm) throws IOException, JSONException {
        String url = baseUrl+"/"+orgKey+"/search/"+searchTerm.replaceAll(" ","%20"); // should use a special-purpose method for this
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
                    int id = jjo.getInt("id"); // all returned objects should have an id
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
        String url = baseUrl+"/"+orgKey+"/"+id;
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
        return errorType+":"+errorReason;
    }

    /**
     * Return a nicely readable error message from an error JSON string.
     */
    static String getErrorMessage(String str) throws JSONException {
        JSONObject json = new JSONObject(str);
        JSONObject error = json.getJSONObject("error");
        Iterator<String> keys = error.keys();
        String errorType = keys.next();
        String errorReason = error.getString(errorType);
        return errorType+":"+errorReason;
    }

    
}
