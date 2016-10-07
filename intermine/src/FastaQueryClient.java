import java.io.IOException;
import java.io.PrintStream;
import java.util.Iterator;
import java.util.List;

import org.intermine.metadata.Model;
import org.intermine.pathquery.Constraints;
import org.intermine.pathquery.OrderDirection;
import org.intermine.pathquery.PathQuery;
import org.intermine.webservice.client.core.ServiceFactory;
import org.intermine.webservice.client.services.QueryService;

/**
 * This is a Java program to run a query from BeanMine.
 * It was automatically generated at Wed Sep 28 17:18:26 MDT 2016
 *
 * @author shokin@ncgr.org
 *
 */
public class FastaQueryClient {
    
    private static final String ROOT = "http://datil:8081/beanmine/service";

    /**
     * Perform the query and print the rows of results.
     * @param args command line arguments
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        ServiceFactory factory = new ServiceFactory(ROOT);
        Model model = factory.getModel();
        PathQuery query = new PathQuery(model);

        // Select the output columns:
        query.addViews("GeneFlankingRegion.primaryIdentifier", "GeneFlankingRegion.sequence.residues");

        // Add orderby
        query.addOrderBy("GeneFlankingRegion.primaryIdentifier", OrderDirection.ASC);

        // Filter the results with the following constraints:
        query.addConstraint(Constraints.eq("GeneFlankingRegion.direction", "upstream"));

        QueryService service = factory.getQueryService();
        Iterator<List<Object>> rows = service.getRowListIterator(query);
        while (rows.hasNext()) {
            Object[] row = rows.next().toArray();
            System.out.println(">"+row[0].toString());
            System.out.println(row[1].toString());
        }

    }

}

