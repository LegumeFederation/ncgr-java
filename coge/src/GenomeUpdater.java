import org.coge.api.CoGe;
import org.coge.api.CoGeParameters;
import org.coge.api.CoGeResponse;
import org.coge.api.Genome;

/**
 * Update a genome from CoGe, identified by its id.
 */
public class GenomeUpdater {

    static String COGE_PROPERTIES_FILE = "coge.properties";

    public static void main(String[] args) {

        if (args.length!=3) {
            System.out.println("Usage: GenomeUpdater <genome id> <name> <description>");
            System.exit(1);
        }

        try {

            int id = Integer.parseInt(args[0]);
            String name = args[1];
            String description = args[2];

            // get the CoGe auth params and initialize token
            CoGeParameters cogeParams = new CoGeParameters(COGE_PROPERTIES_FILE);

            // initialize Agave/CoGe token
            try {
                cogeParams.initializeToken();
            } catch (Exception e) {
                System.err.println("Error initializing token:");
                System.err.println(e.toString());
                System.exit(1);
            }
            
            if (cogeParams.hasToken()) {
                System.out.println("");
                System.out.println("CoGe baseURL:\t"+cogeParams.getBaseURL());
                System.out.println("CoGe Token:\t"+cogeParams.getToken());
            } else {
                System.err.println("Error: couldn't get CoGe token.");
                System.exit(1);
            }
            
            // instantiate our CoGe workhorse
            CoGe coge = new CoGe(cogeParams.getBaseURL(), cogeParams.getUser(), cogeParams.getToken());

            // stub genome
            Genome genome = new Genome(id, name, description);
            boolean success = coge.updateGenome(genome);
            
            System.out.println("Success: "+success);

        } catch (Exception e) {
            System.err.println(e);
        }

    }

}
