import org.ncgr.irods.IRODSParameters;
import org.ncgr.irods.Readme;

import org.coge.api.CoGe;
import org.coge.api.CoGeParameters;
import org.coge.api.CoGeResponse;
import org.coge.api.Organism;

import java.io.File;

import java.util.List;
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
 * Copy data from iRODS to CoGe.
 */
public class IRODSToCoGe {

    static String IRODS_PROPERTIES_FILE = "irods.properties";
    static String COGE_PROPERTIES_FILE = "coge.properties";

    public static void main(String[] args) {

        if (args.length!=2) {
            System.out.println("Usage: IRODSToCoGe <iplant directory> <CoGe organism name>");
            System.out.println("Example: IRODSToCoGe /iplant/home/shared/Legume_Federation/Cajanus_cajan \"Cajanus cajan\"");
            System.exit(1);
        }

        String iRODSDirectory = args[0];
        String cogeOrganismName = args[1];
        
        IRODSFileSystem iRODSFileSystem = null;

        // data we want to extract from iRODS
        // README data
        String identifier = null;
        String genotype = null;
        String source = null;
        String provenance = null;
        // FASTA files
        File unmaskedFasta = null;
        File softMaskedFasta = null;
        File hardMaskedFasta = null;

        try {

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

            // get the iRODS parameters
            IRODSParameters iRODSParams = new IRODSParameters(IRODS_PROPERTIES_FILE);
            
            // instantiate the IRODSFileSystem object
            iRODSFileSystem = IRODSFileSystem.instance();
            System.out.println("");
            System.out.println("IRODSFileSystem instantiated.");

            // instantiate our iRODS account
            IRODSAccount irodsAccount = new IRODSAccount(iRODSParams.getHost(), iRODSParams.getPort(), iRODSParams.getUser(), iRODSParams.getPassword(),
                                                         iRODSParams.getHomeDirectory(), iRODSParams.getUserZone(), iRODSParams.getDefaultStorageResource());
            System.out.println("iRODS Account:\t"+irodsAccount.toURI(false));

            // data transfer ops and iRODS file factory
            DataTransferOperations dataTransferOperations = iRODSFileSystem.getIRODSAccessObjectFactory().getDataTransferOperations(irodsAccount);
            IRODSFileFactory irodsFileFactory = iRODSFileSystem.getIRODSFileFactory(irodsAccount);

            // now get the data from the requested iRODS directory
            IRODSFile irodsFile = irodsFileFactory.instanceIRODSFile(iRODSDirectory);
            printLine(irodsFile);

            File[] files = irodsFile.listFiles();
            for (int i=0; i<files.length; i++) {
                if (files[i].isDirectory()) {
                    // discern directory purpose from LIS standard directory names
                    // we only want genomes and annotation
                    if (isGenomeDir(files[i])) {
                        // drill deeper
                        IRODSFile subFile = irodsFileFactory.instanceIRODSFile(iRODSDirectory+"/"+files[i].getName());
                        File[] subFiles = subFile.listFiles();
                        for (int j=0; j<subFiles.length; j++) {
                            // gather the FASTA files
                            if (isGenomeDir(files[i]) && isUnmaskedFasta(subFiles[j])) unmaskedFasta = subFiles[j];
                            if (isGenomeDir(files[i]) && isSoftMaskedFasta(subFiles[j])) softMaskedFasta = subFiles[j];
                            if (isGenomeDir(files[i]) && isHardMaskedFasta(subFiles[j])) hardMaskedFasta = subFiles[j];
                            // discern file purpose from suffix
                            // download the README.md
                            if (isReadme(subFiles[j])) {
                                IRODSFile sourceFile = irodsFileFactory.instanceIRODSFile(iRODSDirectory+"/"+files[i].getName()+"/"+subFiles[j].getName());
                                File localFile = new File(subFiles[j].getName());
                                try {
                                    dataTransferOperations.getOperation(sourceFile, localFile, null, null);
                                } catch (OverwriteException oe) {
                                    localFile = new File(localFile.getName());
                                }
                                Readme readme = new Readme(localFile);
                                identifier = readme.getIdentifier();
                                genotype = readme.getGenotype();
                                source = readme.getSource();
                                provenance = readme.getProvenance();
                            }
                        }
                    }
                }
            }

            // Hopefully we found everything
            if (identifier!=null && genotype!=null && source!=null && provenance!=null && unmaskedFasta!=null && softMaskedFasta!=null && hardMaskedFasta!=null) {
                System.out.println("");
                System.out.println("This is what we'll send to CoGe:");
                System.out.println("Identifier:\t"+identifier);
                System.out.println("Genotype:\t"+genotype);
                System.out.println("Source:\t\t"+source);
                System.out.println("Provenance:\t"+provenance);
                System.out.println("");
                System.out.println("unmasked FASTA:\t\t"+unmaskedFasta.getAbsolutePath());
                System.out.println("soft-masked FASTA:\t"+softMaskedFasta.getAbsolutePath());
                System.out.println("hard-masked FASTA:\t"+hardMaskedFasta.getAbsolutePath());
                System.out.println("");
            }

            // now for the CoGe stuff
            List<Organism> organisms = coge.searchOrganism(cogeOrganismName);
            if (organisms.size()==0) {
                System.out.println("CoGe organism NOT FOUND.");
            } else if (organisms.size()>1) {
                System.out.println("Multiple CoGe organisms found. Refine your CoGe organism string.");
            } else {
                Organism organism = organisms.get(0);
                System.out.println("Single CoGe organism FOUND.");
                System.out.println("Organism Name:\t"+organism.getName());
                System.out.println("Organism ID:\t"+organism.getId());
                System.out.println("Organism Description:\t"+organism.getDescription());
                System.out.println("");
                System.out.println("Adding unmasked genome to CoGe...");
                CoGeResponse response1 = coge.addGenome(organism, genotype, provenance, identifier, source, "unmasked", false, unmaskedFasta.getAbsolutePath());
                System.out.println(response1.toString());
                System.out.println("");
                System.out.println("Adding soft-masked genome to CoGe...");
                CoGeResponse response2 = coge.addGenome(organism, genotype, provenance, identifier, source, "soft-masked", false, softMaskedFasta.getAbsolutePath());
                System.out.println(response2.toString());
                System.out.println("");
                System.out.println("Adding hard-masked genome to CoGe...");
                CoGeResponse response3 = coge.addGenome(organism, genotype, provenance, identifier, source, "hard-masked", false, hardMaskedFasta.getAbsolutePath());
                System.out.println(response3.toString());
            }                
                

        } catch (AuthenticationException e) {
            System.out.println("Your username/password combination is invalid.");
        } catch (Exception e) {
            System.err.println(e);
        } finally {
            if (iRODSFileSystem!=null) {
                try {
                    iRODSFileSystem.close();
                    System.out.println("");
                    System.out.println("IRODSFileSystem closed.");
                } catch (JargonException e) {
                    System.err.println(e);
                }
            }
        }

    }

    /**
     * Informatativational output
     */
    static void printLine(File file) {
        if (file.isDirectory()) {
            System.out.println("");
            System.out.println("Dir:\t"+file.getAbsolutePath());
        } else  if (file.isFile()) {
            System.out.println("File:\t\t"+file.getAbsolutePath());
        }
    }

    /**
     * Informatativational output
     */
    static void printLine(IRODSFile file) {
        if (file.isDirectory()) {
            System.out.println("");
            System.out.println("Dir:\t"+file.getAbsolutePath());
        } else  if (file.isFile()) {
            System.out.println("File:\t\t"+file.getAbsolutePath());
        }
    }

    static boolean isFasta(File file) {
        return file.getName().endsWith("fa.gz");
    }

    static boolean isHardMaskedFasta(File file) {
        return file.getName().endsWith("hardmasked.fa.gz");
    }

    static boolean isSoftMaskedFasta(File file) {
        return file.getName().endsWith("softmasked.fa.gz");
    }

    static boolean isUnmaskedFasta(File file) {
        return isFasta(file) && !isHardMaskedFasta(file) && !isSoftMaskedFasta(file);
    }
    
    static boolean isCDSFasta(File file) {
        return file.getName().endsWith("cds.fa.gz");
    }

    static boolean isCDSPrimaryTranscriptOnlyFasta(File file) {
        return file.getName().endsWith("cds_primaryTranscriptOnly.fa.gz");
    }

    static boolean isProteinFasta(File file) {
        return file.getName().endsWith("protein.fa.gz");
    }

    static boolean isProteinPrimaryTranscriptOnlyFasta(File file) {
        return file.getName().endsWith("protein_primaryTranscriptOnly.fa.gz");
    }
    
    static boolean isTranscriptFasta(File file) {
        return file.getName().endsWith("transcript.fa.gz");
    }
    
    static boolean isTranscriptPrimaryTranscriptOnlyFasta(File file) {
        return file.getName().endsWith("transcript_primaryTranscriptOnly.fa.gz");
    }

    static boolean isGFF(File file) {
        return file.getName().endsWith("gff3.gz");
    }
    
    static boolean isGeneGFF(File file) {
        return file.getName().endsWith("gene.gff3.gz");
    }

    static boolean isGeneExonsGFF(File file) {
        return file.getName().endsWith("gene_exons.gff3.gz");
    }

    static boolean isReadme(File file) {
        return file.getName().startsWith("README");
    }

    static boolean isGenomeDir(File dir) {
        return dir.getName().endsWith("gnm1");
    }

    static boolean isAnnotationDir(File dir) {
        return dir.getName().endsWith("ann1");
    }

    static boolean isDiversityDir(File dir) {
        return dir.getName().endsWith("div1");
    }
    
    static boolean isSyntenyDir(File dir) {
        return dir.getName().endsWith("synt1");
    }

}
