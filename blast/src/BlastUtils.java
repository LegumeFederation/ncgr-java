package org.ncgr.blast;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.TreeMap;
import java.util.TreeSet;


import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.biojava.nbio.core.sequence.DNASequence;
import org.biojava.nbio.core.sequence.io.FastaReaderHelper;
import org.biojava.nbio.core.sequence.io.FastaWriterHelper;

/**
 * A set of static utility methods for running Blast and returning BlastOutput.
 *
 * @author Sam Hokin
 */
public class BlastUtils {

    /**
     * Run blastn with some fixed parameters, taking two sequences and word size as input.
     *
     * @param subjectFilename the name of the FASTA file containing the subject sequence(s)
     * @param queryFilename the name of the FASTA file containing the query sequence(s)
     * @param parameters a Map of parameter names (without the dash) and values, both represented as Strings, e.g. "word_size":"8"; outfmt, out, subject and query will be ignored.
     */
    public static BlastOutput runBlastn(String subjectFilename, String queryFilename, Map<String,String> parameters) throws IOException, InterruptedException, JAXBException {
        Runtime rt = Runtime.getRuntime();
        String filename = "/tmp/blastutils_"+System.currentTimeMillis();
        String command = "blastn -outfmt 5 -subject "+subjectFilename+" -query "+queryFilename;
        for (String parameter : parameters.keySet()) {
            if (!parameter.contains("-") &&
                !parameter.equals("outfmt") &&
                !parameter.equals("out") &&
                !parameter.equals("subject") &&
                !parameter.equals("query")) {
                String value = parameters.get(parameter);
                command += " -"+parameter+" "+value;
            }
            // indicate the query range that we're searching in the file name
            if (parameter.equals("query_loc")) {
                String value = parameters.get(parameter);
                filename += "_"+value;
            }
        }
        filename += ".xml";
        command += " -out "+filename;
        Process pr = rt.exec(command);
        pr.waitFor();
        if (pr.exitValue()!=0) {
            System.err.println("Aborting: blastn returned exit value "+pr.exitValue());
            System.exit(pr.exitValue());
        }
        return getBlastOutput(filename);
    }

    /**
     * Return a BlastOutput from a given XML file
     *
     * @param filename the name of the XML file containing blast output
     * @return a BlastOutput instance
     */
    public static BlastOutput getBlastOutput(String filename) throws JAXBException {
        JAXBContext jaxbContext = JAXBContext.newInstance(BlastOutput.class);
        Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
        BlastOutput blastOutput = (BlastOutput) jaxbUnmarshaller.unmarshal(new File(filename));
        return blastOutput;
    }

    /**
     * Return a BlastOutput from an XML file given by a URL
     *
     * @param url the URL of the XML file
     * @return a BlastOutput instance
     */
    public static BlastOutput getBlastOutput(URL url) throws JAXBException {
        JAXBContext jaxbContext = JAXBContext.newInstance(BlastOutput.class);
        Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
        BlastOutput blastOutput = (BlastOutput) jaxbUnmarshaller.unmarshal(url);
        return blastOutput;
    }
    

    /**
     * Run blast between all the sequences in the provided file, returning a TreeSet of SequenceHits summarizing the results.
     * Uses temp storage to create the many FASTA files used in the BLAST command line.
     *
     * @param  multiFastaFilename the name of a multi-fasta file containing all the sequences to search for common motifs
     * @return a TreeSet containing resulting SequenceHits sorted by the SequenceHits comparator
     */
    public static TreeSet<SequenceHits> blastSequenceHits(String multiFastaFilename) throws Exception {

        // the smallest word_size for the blast runs
        String WORD_SIZE = "8";

        // the blastn parameters without the dash
        Map<String,String> parameters = new HashMap<String,String>();
        parameters.put("strand", "plus");
        parameters.put("ungapped", "");
        parameters.put("perc_identity", "100");
        parameters.put("word_size", WORD_SIZE);
    
        // we'll add the found hits to this map of SequenceHits
        TreeMap<String,SequenceHits> seqHitsMap = new TreeMap<String,SequenceHits>();

        FastaReaderHelper frh = new FastaReaderHelper();
        FastaWriterHelper fwh = new FastaWriterHelper();

        // pull out the individual sequences with BioJava help
        File multiFasta = new File(multiFastaFilename);
        LinkedHashMap<String,DNASequence> sequenceMap = null;
        try {
            sequenceMap = frh.readFastaDNASequence(multiFasta);
        } catch (IOException ex) {
            ex.printStackTrace();
            System.exit(1);
        }

        // loop through each sequence as query against the remaining as subject
        for (DNASequence querySequence : sequenceMap.values()) {

            String queryID = querySequence.getOriginalHeader();
            File queryFile = null;
            File subjectFile = null;

            // write out the query fasta
            queryFile = File.createTempFile("query", ".fasta");
            fwh.writeSequence(queryFile, querySequence);
            String queryFilePath = queryFile.getAbsolutePath();
            
            // create the subject multi-fasta = all sequences but the query sequence
            LinkedHashMap<String,DNASequence> subjectMap = new LinkedHashMap<String,DNASequence>(sequenceMap);
            subjectMap.remove(queryID);
            
            // write out the subject file
            subjectFile = File.createTempFile("subject", ".fasta");
            fwh.writeNucleotideSequence(subjectFile, subjectMap.values());
            String subjectFilePath = subjectFile.getAbsolutePath();
            
            // now run BLAST with given parameters
            BlastOutput blastOutput = runBlastn(subjectFilePath, queryFilePath, parameters);
            BlastOutputIterations iterations = blastOutput.getBlastOutputIterations();
            if (iterations!=null) {
                List<Iteration> iterationList = iterations.getIteration();
                if (iterationList!=null) {
                    for (Iteration iteration : iterationList) {
                        if (iteration.getIterationMessage()==null) {
                            List<Hit> hitList = iteration.getIterationHits().getHit();
                            for (Hit hit : hitList) {
                                String hitID = hit.getHitDef();
                                HitHsps hsps = hit.getHitHsps();
                                if (hsps!=null) {
                                    List<Hsp> hspList = hsps.getHsp();
                                    if (hspList!=null) {
                                        for (Hsp hsp : hspList) {
                                            String qSeq = hsp.getHspQseq(); // qSeq = motif
                                            if (seqHitsMap.containsKey(qSeq)) {
                                                SequenceHits seqHits = seqHitsMap.get(qSeq);
                                                seqHits.addSequenceHit(new SequenceHit(queryID, hitID, hsp));
                                            } else {
                                                SequenceHits seqHits = new SequenceHits(new SequenceHit(queryID, hitID, hsp));
                                                seqHitsMap.put(qSeq, seqHits);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // return output TreeSet, which is sorted by score
        return new TreeSet<SequenceHits>(seqHitsMap.values());

    }

    /**
     * Return a double score for an input DNA sequence equal to the log of the inverse of the probability of each letter being produced randomly.
     * The probabilities for each are set at the top. Longer sequences naturally get much larger scores, so this should be used to 
     * compare sequences of the same length.
     *
     * @param  sequence a string sequence of DNA letters
     * @return an integer score
     */
    public static double scoreDNASequence(String sequence) {

        char[] letters = { 'A',  'T',  'C',  'G',  'W',  'K',  'R',  'M',  'Y',  'S',  'N'  };
        double[] probs = { 0.35, 0.35, 0.15, 0.15, 1.00, 0.50, 0.50, 0.50, 0.50, 0.30, 1.00 };
        
        char[] chars = sequence.toCharArray();
        double totalProb = 1.00;
        for (int i=0; i<chars.length; i++) {
            for (int j=0; j<letters.length; j++) {
                if (chars[i]==letters[j]) {
                    totalProb *= probs[j];
                    j = chars.length;
                }
            }
        }

        return -Math.log10(totalProb);
        
    }

    /**
     * Return a combined sequence from two input sequences, where mismatches are represented by "N". Assumes both are of same length.
     *
     * @param seq1 a string sequence of DNA letters
     * @param seq2 a string sequence of DNA letters
     * @return combined a string sequence representing seq1 and seq2 with mismatches represented by N 
     */
    public static String combineSequences(String seq1, String seq2) {
        if (seq1.length()!=seq2.length()) {
            return null;
        }
        char[] seq1Chars = seq1.toCharArray();
        char[] seq2Chars = seq2.toCharArray();
        String combined = "";
        for (int i=0; i<seq1Chars.length; i++) {
            if (seq1Chars[i]==seq2Chars[i]) {
                combined += seq1Chars[i];
            } else {
                combined += 'N';
            }
        }
        return combined;
    }
    

    /**
     * Read a Blast-generated XML file (-outfmt 5) and spit out the contents.
     *
     * @param filepath the full path of the blast XML file
     */
    public static void readBlastXML(String filepath) throws JAXBException {

            File file = new File(filepath);
            JAXBContext jaxbContext = JAXBContext.newInstance(BlastOutput.class);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            
            BlastOutput blastOutput = (BlastOutput) jaxbUnmarshaller.unmarshal(file);
            System.out.println("======== BlastOutput ========-");
            System.out.println("db="+blastOutput.getBlastOutputDb());
            System.out.println("program="+blastOutput.getBlastOutputProgram());
            System.out.println("queryDef="+blastOutput.getBlastOutputQueryDef());
            System.out.println("queryID="+blastOutput.getBlastOutputQueryID());
            System.out.println("queryLen="+blastOutput.getBlastOutputQueryLen());
            System.out.println("querySeq="+blastOutput.getBlastOutputQuerySeq());
            System.out.println("outputReference="+blastOutput.getBlastOutputReference());
            System.out.println("outputVersion="+blastOutput.getBlastOutputVersion());

            BlastOutputMbstat mbstat = blastOutput.getBlastOutputMbstat();
            System.out.println("======== Statistics ========");
            if (mbstat==null) {
                System.out.println("getBlastOutputMbstat() returned null.");
            } else {
                Statistics stats = mbstat.getStatistics();
                if (stats==null) {
                    System.out.println("getStatistics() returned null.");
                } else {
                    System.out.println("dblen="+stats.getStatisticsDbLen());
                    System.out.println("dbnum="+stats.getStatisticsDbNum());
                    System.out.println("effspace="+stats.getStatisticsEffSpace());
                    System.out.println("entropy="+stats.getStatisticsEntropy());
                    System.out.println("hsplen="+stats.getStatisticsHspLen());
                    System.out.println("kappa="+stats.getStatisticsKappa());
                    System.out.println("lambda="+stats.getStatisticsLambda());
                }
            }

            BlastOutputParam param = blastOutput.getBlastOutputParam();
            System.out.println("======== Parameters ========");
            if (param==null) {
                System.out.println("getBlastOutputParam() returned null.");
            } else {
                Parameters params = param.getParameters();
                if (params==null) {
                    System.out.println("getParameters() returned null.");
                } else {
                    System.out.println("entrezQuery="+params.getParametersEntrezQuery());
                    System.out.println("expect="+params.getParametersExpect());
                    System.out.println("filter="+params.getParametersFilter());
                    System.out.println("gapExtend="+params.getParametersGapExtend());
                    System.out.println("gapOpen="+params.getParametersGapOpen());
                    System.out.println("include="+params.getParametersInclude());
                    System.out.println("matrix="+params.getParametersMatrix());
                    System.out.println("pattern="+params.getParametersPattern());
                    System.out.println("scMatch="+params.getParametersScMatch());
                    System.out.println("scMismatch="+params.getParametersScMismatch());
                }
            }

            BlastOutputIterations iterations = blastOutput.getBlastOutputIterations();
            System.out.println("======== Iterations ========");
            if (iterations==null) {
                System.out.println("getBlastOutputIterations() returned null.");
            } else {
                List<Iteration> iterationList = iterations.getIteration();
                if (iterationList==null) {
                    System.out.println("getIteration() returned null.");
                } else {
                    for (Iteration iteration : iterationList) {
                        
                        System.out.println("num="+iteration.getIterationIterNum());
                        System.out.println("message="+iteration.getIterationMessage());
                        System.out.println("queryDef="+iteration.getIterationQueryDef());
                        System.out.println("queryID="+iteration.getIterationQueryID());
                        System.out.println("queryLen="+iteration.getIterationQueryLen());
                        
                        IterationStat stat = iteration.getIterationStat();
                        Statistics stats = stat.getStatistics();
                        System.out.println("======== Stats ========");
                        if (stats==null) {
                            System.out.println("getStatistics() returned null.");
                        } else {
                            System.out.println("dblen="+stats.getStatisticsDbLen());
                            System.out.println("dbnum="+stats.getStatisticsDbNum());
                            System.out.println("effspace="+stats.getStatisticsEffSpace());
                            System.out.println("entropy="+stats.getStatisticsEntropy());
                            System.out.println("hsplen="+stats.getStatisticsHspLen());
                            System.out.println("kappa="+stats.getStatisticsKappa());
                            System.out.println("lambda="+stats.getStatisticsLambda());
                        }
                        
                        IterationHits hits = iteration.getIterationHits();
                        if (hits==null) {
                            System.out.println("getIterationHits() returned null.");
                        } else {
                            List<Hit> hitList = hits.getHit();
                            if (hitList==null) {
                                System.out.println("getHit() returned null.");
                            } else {
                                for (Hit hit : hitList) {
                                    System.out.println("======== HIT "+hit.getHitNum()+" ========");
                                    System.out.println("accession="+hit.getHitAccession());
                                    System.out.println("def="+hit.getHitDef());
                                    System.out.println("id="+hit.getHitId());
                                    System.out.println("len="+hit.getHitLen());
                                    HitHsps hsps = hit.getHitHsps();
                                    if (hsps==null) {
                                        System.out.println("getHitHsps() returned null.");
                                    } else {
                                        List<Hsp> hspList = hsps.getHsp();
                                        if (hspList==null) {
                                            System.out.println("getHsp() returned null.");
                                        } else {
                                            for (Hsp hsp : hspList) {
                                                System.out.println("-------- HSP "+hsp.getHspNum()+" --------");
                                                System.out.println("alignLen="+hsp.getHspAlignLen());
                                                System.out.println("bitScore="+hsp.getHspBitScore());
                                                System.out.println("density="+hsp.getHspDensity());
                                                System.out.println("evalue="+hsp.getHspEvalue());
                                                System.out.println("gaps="+hsp.getHspGaps());
                                                System.out.println("hitFrame="+hsp.getHspHitFrame());
                                                System.out.println("hseq\t"+hsp.getHspHseq()+"\t"+hsp.getHspHitFrom()+"-"+hsp.getHspHitTo());
                                                System.out.println("midline\t"+hsp.getHspMidline());
                                                System.out.println("qseq\t"+hsp.getHspQseq()+"\t"+hsp.getHspQueryFrom()+"-"+hsp.getHspQueryTo());
                                                System.out.println("identity="+hsp.getHspIdentity());
                                                System.out.println("patternFrom="+hsp.getHspPatternFrom());
                                                System.out.println("patternTo="+hsp.getHspPatternTo());
                                                System.out.println("positive="+hsp.getHspPositive());
                                                System.out.println("queryFrame="+hsp.getHspQueryFrame());
                                                System.out.println("score="+hsp.getHspScore());
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

    }

    
}
