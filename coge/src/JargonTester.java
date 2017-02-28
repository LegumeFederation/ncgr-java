import org.ncgr.irods.IRODSParameters;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

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
 * Test the Jargon API. Loads iRODS connection properties from properties file "irods.properties".
 */
public class JargonTester {

    static String PROPERTIES_FILE = "irods.properties";

    public static void main(String[] args) {

        if (args.length!=1) {
            System.out.println("Usage: JargonTester <iplant directory>");
            System.out.println("Example: JargonTester /iplant/home/shared/Legume_Federation/Cajanus_cajan");
            System.exit(1);
        }

        String directory = args[0];

        IRODSFileSystem iRODSFileSystem = null;

        try {
            
            IRODSParameters params = new IRODSParameters(PROPERTIES_FILE);
            
            iRODSFileSystem = IRODSFileSystem.instance();
            System.out.println("IRODSFileSystem instantiated.");

            IRODSAccount irodsAccount = new IRODSAccount(params.getHost(), params.getPort(), params.getUser(), params.getPassword(),
                                                         params.getHomeDirectory(), params.getUserZone(), params.getDefaultStorageResource());
            System.out.println(irodsAccount.toURI(false));
            System.out.println("\tisAnonymousAccount\t"+irodsAccount.isAnonymousAccount());
            System.out.println("\tisDefaultObfuscate\t"+irodsAccount.isDefaultObfuscate());

            DataTransferOperations dataTransferOperations = iRODSFileSystem.getIRODSAccessObjectFactory().getDataTransferOperations(irodsAccount);

            IRODSFileFactory irodsFileFactory = iRODSFileSystem.getIRODSFileFactory(irodsAccount);

            System.out.println(directory);
            IRODSFile irodsFile = irodsFileFactory.instanceIRODSFile(directory);
            File[] files = irodsFile.listFiles();
            for (int i=0; i<files.length; i++) {
                String fileType = null;
                if (files[i].isFile()) fileType = "file";
                if (files[i].isDirectory()) fileType = "directory";
                // discern directory purpose from LIS standard directory names
                boolean isGenomeDir = files[i].isDirectory() && files[i].getName().endsWith("gnm1");
                boolean isAnnotationDir = files[i].isDirectory() && files[i].getName().endsWith("ann1");
                boolean isDiversityDir = files[i].isDirectory() && files[i].getName().endsWith("div1");
                boolean isSyntenyDir = files[i].isDirectory() && files[i].getName().endsWith("synt1");
                System.out.print("\t"+fileType+"\t"+files[i].getName()); 
                if (isGenomeDir) System.out.print("\tGENOME DIRECTORY");
                if (isAnnotationDir) System.out.print("\tANNOTATION DIRECTORY");
                if (isDiversityDir) System.out.print("\tDIVERSITY DIRECTORY");
                if (isSyntenyDir) System.out.print("\tSYNTENY DIRECTORY");
                System.out.println("");
                // drill deeper
                if (files[i].isDirectory()) {
                    IRODSFile subFile = irodsFileFactory.instanceIRODSFile(directory+"/"+files[i].getName());
                    File[] subFiles = subFile.listFiles();
                    for (int j=0; j<subFiles.length; j++) {
                        String subFileType = null;
                        if (subFiles[j].isFile()) subFileType = "file";
                        if (subFiles[j].isDirectory()) subFileType = "directory";

                        // discern file purpose from suffix
                        boolean isFasta = subFiles[j].isFile() && subFiles[j].getName().endsWith("fa.gz");
                        boolean isHardMaskedFasta = subFiles[j].isFile() && subFiles[j].getName().endsWith("hardmasked.fa.gz");
                        boolean isSoftMaskedFasta = subFiles[j].isFile() && subFiles[j].getName().endsWith("softmasked.fa.gz");
                        boolean isPlainFasta = isFasta && !isHardMaskedFasta && !isSoftMaskedFasta;
                        
                        boolean isCDSFasta = isFasta && subFiles[j].getName().endsWith("cds.fa.gz");
                        boolean isCDSPrimaryTranscriptOnlyFasta = isFasta && subFiles[j].getName().endsWith("cds_primaryTranscriptOnly.fa.gz");
                        
                        boolean isProteinFasta = isFasta && subFiles[j].getName().endsWith("protein.fa.gz");
                        boolean isProteinPrimaryTranscriptOnlyFasta = isFasta && subFiles[j].getName().endsWith("protein_primaryTranscriptOnly.fa.gz");

                        boolean isTranscriptFasta = isFasta && subFiles[j].getName().endsWith("transcript.fa.gz");
                        boolean isTranscriptPrimaryTranscriptOnlyFasta = isFasta && subFiles[j].getName().endsWith("transcript_primaryTranscriptOnly.fa.gz");

                        boolean isGFF = subFiles[j].isFile() && subFiles[j].getName().endsWith("gff3.gz");
                        boolean isGeneGFF = subFiles[j].isFile() && subFiles[j].getName().endsWith("gene.gff3.gz");
                        boolean isGeneExonsGFF = isGFF && subFiles[j].getName().endsWith("gene_exons.gff3.gz");
                        
                        boolean isReadme = subFiles[j].isFile() && subFiles[j].getName().startsWith("README");

                        // output
                        System.out.print("\t\t"+subFileType+"\t\t"+subFiles[j].getName());
                        
                        if (isFasta) System.out.print("\tFASTA");
                        if (isHardMaskedFasta) System.out.print("\tHARDMASKED");
                        if (isSoftMaskedFasta) System.out.print("\tSOFTMASKED");
                        
                        if (isCDSFasta) System.out.print("\tCDS");
                        if (isCDSPrimaryTranscriptOnlyFasta) System.out.print("\tCDS Primary Transcripts Only");
                        
                        if (isProteinFasta) System.out.print("\tPROTEIN");
                        if (isProteinPrimaryTranscriptOnlyFasta) System.out.print("\tPROTEIN Primary Transcripts Only");

                        if (isTranscriptFasta) System.out.print("\tTRANSCRIPTS");
                        if (isTranscriptPrimaryTranscriptOnlyFasta) System.out.print("\tTRANSCRIPTS Primary Transcripts Only");

                        if (isGFF) System.out.print("\tGFF");
                        if (isGeneGFF) System.out.print("\tGENES");
                        if (isGeneExonsGFF) System.out.print("\tGENES+EXONS");

                        if (isReadme) {
                            System.out.print("\tREADME");
                            IRODSFile sourceFile = irodsFileFactory.instanceIRODSFile(directory+"/"+files[i].getName()+"/"+subFiles[j].getName());
                            File localFile = new File(subFiles[j].getName());
                            try {
                                dataTransferOperations.getOperation(sourceFile, localFile, null, null);
                                System.out.print("\tCOPIED TO LOCAL DIRECTORY.");
                            } catch (OverwriteException oe) {
                                System.out.print("\tFILE ALREADY IN LOCAL DIRECTORY.");
                            }
                        }
                        
                        System.out.println("");
                    }
                }
            }
            
        } catch (AuthenticationException e) {
            System.out.println("Your username/password combination is invalid.");
        } catch (Exception e) {
            System.err.println(e);
        }

        if (iRODSFileSystem!=null) {
            try {
                iRODSFileSystem.close();
                System.out.println("IRODSFileSystem closed.");
            } catch (JargonException e) {
                System.err.println(e);
            }
        }

    }

}
