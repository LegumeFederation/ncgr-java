package org.ncgr.coge;

import java.io.IOException;
import java.io.InputStream;

import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.LinkedHashMap;

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
    String username;
    String token;
    Resty resty;

    /**
     * Construct given just a base URL, like https://genomevolution.org/coge/api/v1/
     * Used only for calls that don't require authentication.
     */
    public CoGe(String baseUrl) {
        this.baseUrl = baseUrl;
        this.resty = new Resty();
    }

    /**
     * Construct given a base URL, username and token, used like https://genomevolution.org/coge/api/v1/stuff/here/and/here?username=x&token=y
     * Used for calls that require authentication.
     */
    public CoGe(String baseUrl, String username, String token) {
        this.baseUrl = baseUrl;
        this.username = username;
        this.token = token;
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

    ////////// Experiment //////////

    /**
     * Experiment search
     * GET [base_url/experiments/search/term]
     *
     * @param searchTerm a text string to search on
     */
    public List<Experiment> searchExperiment(String searchTerm) throws IOException, JSONException {
        List<Experiment> experiments = new ArrayList<Experiment>();
        for (CoGeObject object : search("experiments", searchTerm)) {
            experiments.add(new Experiment(object));
        }
        return experiments;
    }

    /**
     * Experiment fetch - used to populate the genomes
     * GET [base_url/experiments/id]
     * 
     * @param id the experiment id
     */
    public Experiment fetchExperiment(int id) throws IOException, JSONException {
        JSONObject json = fetch("experiments", id);
        return getExperimentFromJSON(json);
    }

    /**
     * Populate an Experiment from a JSONObject.
     */
    Experiment getExperimentFromJSON(JSONObject json) throws IOException, JSONException {
        int id = json.getInt("id");
        String name = json.getString("name");
        String description = json.getString("description");
        Experiment experiment = new Experiment(id, name, description);
        if (json.has("version")) experiment.setVersion(json.getString("version"));
        if (json.has("genome_id")) experiment.setGenomeId(json.getInt("genome_id"));
        if (json.has("source")) experiment.setSource(json.getString("source"));
        if (json.has("types")) {
            Map<String,String> types = new LinkedHashMap<String,String>();
            JSONArray typarray = json.getJSONArray("types");
            for (int i=0; i<typarray.length(); i++) {
                JSONObject type = typarray.getJSONObject(i);
                types.put(type.getString("name"), type.getString("description"));
            }
            experiment.setTypes(types);
        }
        if (json.has("restricted")) experiment.setRestricted(json.getBoolean("restricted"));
        if (json.has("additional_metadata")) {
            List<Metadata> metadata = new ArrayList<Metadata>();
            JSONArray metarray = json.getJSONArray("additional_metadata");
            for (int i=0; i<metarray.length(); i++) {
                JSONObject meta = metarray.getJSONObject(i);
                metadata.add(new Metadata(meta.getString("type_group"), meta.getString("type"), meta.getString("text"), meta.getString("link")));
            }
            experiment.setAdditionalMetadata(metadata);
        }
        return experiment;
    }

    ////////// Notebook //////////

    /**
     * Notebook search
     * GET [base_url/notebooks/search/term]
     *
     * @param searchTerm a text string to search on
     */
    public List<Notebook> searchNotebook(String searchTerm) throws IOException, JSONException {
        List<Notebook> notebooks = new ArrayList<Notebook>();
        for (CoGeObject object : search("notebooks", searchTerm)) {
            notebooks.add(new Notebook(object));
        }
        return notebooks;
    }

    /**
     * Notebook fetch - used to populate the genomes
     * GET [base_url/notebooks/id]
     * 
     * @param id the notebook id
     */
    public Notebook fetchNotebook(int id) throws IOException, JSONException {
        JSONObject json = fetch("notebooks", id);
        return getNotebookFromJSON(json);
    }

    /**
     * Populate an Notebook from a JSONObject.
     */
    Notebook getNotebookFromJSON(JSONObject json) throws IOException, JSONException {
        int id = json.getInt("id");
        String name = json.getString("name");
        String description = json.getString("description");
        Notebook notebook = new Notebook(id, name, description);
        if (json.has("type")) notebook.setType(json.getString("type"));
        if (json.has("restricted")) notebook.setRestricted(json.getBoolean("restricted"));
        if (json.has("additional_metadata")) {
            List<Metadata> metadata = new ArrayList<Metadata>();
            JSONArray metarray = json.getJSONArray("additional_metadata");
            for (int i=0; i<metarray.length(); i++) {
                JSONObject meta = metarray.getJSONObject(i);
                metadata.add(new Metadata(meta.getString("type_group"), meta.getString("type"), meta.getString("text"), meta.getString("link")));
            }
            notebook.setAdditionalMetadata(metadata);
        }
        if (json.has("items")) {
            List<Item> items = new ArrayList<Item>();
            JSONArray itemarray = json.getJSONArray("items");
            for (int i=0; i<itemarray.length(); i++) {
                JSONObject item = itemarray.getJSONObject(i);
                items.add(new Item(item.getInt("id"), item.getString("type")));
            }
            notebook.setItems(items);
        }
        return notebook;
    }

    ////////// Group //////////

    /**
     * Group search
     * GET [base_url/groups/search/term]
     *
     * @param searchTerm a text string to search on
     */
    public List<Group> searchGroup(String searchTerm) throws IOException, JSONException {
        List<Group> groups = new ArrayList<Group>();
        String url = baseUrl+"/groups/search/"+searchTerm.replaceAll(" ","%20"); // should use a special-purpose method for this
        List<Group> objects = new ArrayList<Group>();
        JSONResource jr  = resty.json(url);
        JSONObject jo = jr.object();
        Iterator<String> joit = jo.keys();
        while (joit.hasNext()) {
            String jkey = joit.next();
            if (jkey.equals("groups")) {
                JSONArray ja = jo.getJSONArray(jkey);
                for (int i=0; i<ja.length(); i++) {
                    JSONObject jjo = ja.getJSONObject(i);
                    int id = jjo.getInt("id"); // all returned objects should have an id
                    String name = jjo.getString("name");
                    String description = jjo.getString("description");
                    String role = jjo.getString("role");
                    groups.add(new Group(id, name, description, role));
                }
            }
        }
        return groups;
    }

    /**
     * Group fetch - used to populate the genomes
     * GET [base_url/groups/id]
     * 
     * @param id the group id
     */
    public Group fetchGroup(int id) throws IOException, JSONException {
        JSONObject json = fetch("groups", id);
        return getGroupFromJSON(json);
    }

    /**
     * Populate an Group from a JSONObject.
     */
    Group getGroupFromJSON(JSONObject json) throws IOException, JSONException {
        int id = json.getInt("id");
        String name = json.getString("name");
        String description = json.getString("description");
        String role = json.getString("role");
        Group group = new Group(id, name, description, role);
        if (json.has("users")) {
            List<Integer> users = new ArrayList<Integer>();
            JSONArray usearray = json.getJSONArray("users");
            for (int i=0; i<usearray.length(); i++) {
                int user = usearray.getInt(i);
                users.add(user);
            }
            group.setUsers(users);
        }
        return group;
    }

    ////////// DataStoreList //////////

    /**
     * Get a list from the data store corresponding to the given path.
     * GET [base_url/irods/list/path]
     *
     */
    public DataStoreList listDataStore(String path) throws Exception {
        if (username==null || token==null) throw new Exception("Error: username and/or token not supplied. Data Store requests require authentication.");
        String url = baseUrl+"/irods/list/"+path+"?username="+username+"&token="+token;
        JSONResource jr = resty.json(url);
        JSONObject jo = jr.object();
        DataStoreList dsl = new DataStoreList();
        if (jo.has("error")) throw new Exception(getErrorMessage(jo));
        if (jo.has("path")) dsl.setPath(jo.getString("path"));
        if (jo.has("items")) {
            JSONArray ja = jo.getJSONArray("items");
            for (int i=0; i<ja.length(); i++) {
                JSONObject json = ja.getJSONObject(i);
                Map<String,String> item = new LinkedHashMap<String,String>();
                Iterator<String> keys = json.keys();
                while (keys.hasNext()) {
                    String key = keys.next();
                    item.put(key, json.getString(key));
                }
                dsl.addItem(item);
            }
        }
        return dsl;
    }

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
     * Return a nicely readable error message from an JSONObject containing "error".
     */
    static String getErrorMessage(JSONObject json) throws JSONException {
        JSONObject error = json.getJSONObject("error");
        Iterator<String> keys = error.keys();
        String errorType = keys.next();
        String errorReason = error.getString(errorType);
        return errorType+": "+errorReason;
    }

    
}
