import java.io.File;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.packinstr.TransferOptions;
import org.irods.jargon.core.pub.DataTransferOperations;
import org.irods.jargon.core.pub.IRODSFileSystem;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.pub.io.IRODSFileFactory;
import org.irods.jargon.core.transfer.DefaultTransferControlBlock;

import org.irods.jargon.core.exception.AuthenticationException;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.exception.OverwriteException;
/**
 * Test the Jargon API.
 */
public class JargonTester {

    static String PROPERTIES_FILE = "irods.properties";

    static String HOST = "data.iplantcollaborative.org";
    static int PORT = 1247;
    static String USER = "shokin";
    static String PASSWORD = "BrettFavre4";
    static String HOME_DIRECTORY = "";
    static String USER_ZONE = "iplant";
    static String DEFAULT_STORAGE_RESOURCE = "";

    public static void main(String[] args) {

        if (args.length!=1) {
            System.out.println("Usage: JargonTester <iplant directory>");
            System.out.println("Example: JargonTester /iplant/home/shared/Legume_Federation/Cajanus_cajan");
            System.exit(1);
        }

        String directory = args[0];

        IRODSFileSystem irodsFileSystem = null;

        try {
            
            irodsFileSystem = IRODSFileSystem.instance();
            System.out.println("IRODSFileSystem instantiated.");

            IRODSAccount irodsAccount = new IRODSAccount(HOST, PORT, USER, PASSWORD, HOME_DIRECTORY, USER_ZONE, DEFAULT_STORAGE_RESOURCE);
            System.out.println(irodsAccount.toURI(false));
            System.out.println("\tisAnonymousAccount\t"+irodsAccount.isAnonymousAccount());
            System.out.println("\tisDefaultObfuscate\t"+irodsAccount.isDefaultObfuscate());

            DataTransferOperations dataTransferOperations = irodsFileSystem.getIRODSAccessObjectFactory().getDataTransferOperations(irodsAccount);

            IRODSFileFactory irodsFileFactory = irodsFileSystem.getIRODSFileFactory(irodsAccount);

            System.out.println(directory);
            IRODSFile irodsFile = irodsFileFactory.instanceIRODSFile(directory);
            File[] files = irodsFile.listFiles();
            for (int i=0; i<files.length; i++) {
                String fileType = null;
                if (files[i].isFile()) fileType = "file";
                if (files[i].isDirectory()) fileType = "directory";
                System.out.println("\t"+fileType+"\t"+files[i].getName());
                // drill deeper
                if (files[i].isDirectory()) {
                    IRODSFile subFile = irodsFileFactory.instanceIRODSFile(directory+"/"+files[i].getName());
                    File[] subFiles = subFile.listFiles();
                    for (int j=0; j<subFiles.length; j++) {
                        String subFileType = null;
                        if (subFiles[j].isFile()) subFileType = "file";
                        if (subFiles[j].isDirectory()) subFileType = "directory";
                        System.out.print("\t\t"+subFileType+"\t\t"+subFiles[j].getName());
                        if (subFiles[j].getName().startsWith("README")) {
                            IRODSFile sourceFile = irodsFileFactory.instanceIRODSFile(directory+"/"+files[i].getName()+"/"+subFiles[j].getName());
                            File localFile = new File(subFiles[j].getName());
                            try {
                                dataTransferOperations.getOperation(sourceFile, localFile, null, null);
                                System.out.println("\tCOPIED TO LOCAL DIRECTORY.");
                            } catch (OverwriteException oe) {
                                System.out.println("\tFILE ALREADY IN LOCAL DIRECTORY.");
                            }
                        } else {
                            System.out.println("");
                        }
                    }
                }
            }
            
        } catch (AuthenticationException e) {
            System.out.println("Your username/password combination is invalid.");
        } catch (JargonException e) {
            System.err.println(e);
        }

        if (irodsFileSystem!=null) {
            try {
                irodsFileSystem.close();
                System.out.println("IRODSFileSystem closed.");
            } catch (JargonException e) {
                System.err.println(e);
            }
        }

    }

}
