package org.ncgr.blast;

import java.io.File;
import java.util.List;
import java.util.ArrayList;
import java.util.TreeSet;
import java.text.DecimalFormat;

import org.biojava.nbio.alignment.Alignments;
import org.biojava.nbio.alignment.FractionalIdentityScorer;
import org.biojava.nbio.alignment.FractionalIdentityInProfileScorer;
import org.biojava.nbio.alignment.SimpleGapPenalty;
import org.biojava.nbio.alignment.routines.AnchoredPairwiseSequenceAligner;
import org.biojava.nbio.alignment.template.GapPenalty;
import org.biojava.nbio.core.alignment.matrices.SubstitutionMatrixHelper;
import org.biojava.nbio.core.alignment.template.AlignedSequence;
import org.biojava.nbio.core.alignment.template.Profile;
import org.biojava.nbio.core.alignment.template.SequencePair;
import org.biojava.nbio.core.alignment.template.SubstitutionMatrix;
import org.biojava.nbio.core.sequence.DNASequence;
import org.biojava.nbio.core.sequence.compound.NucleotideCompound;
import org.biojava.nbio.core.sequence.io.FastaWriterHelper;
import org.biojava.nbio.core.sequence.template.Sequence;
import org.biojava.nbio.core.util.ConcurrencyTools;

/**
 * Load a multi-fasta containing sequences, and search them for common motifs using the BlastUtils blastSequenceHits utility.
 *
 * @author Sam Hokin
 */
public class SequenceBlaster {

    // defaults
    public static double MAX_DISTANCE = 0.5; // maximum distance of a motif from top-scoring motif to be used in sequence logo
    public static int GOP = 10;              // gap open penalty for alignments
    public static int GEP = 1;               // gap extension penalty for alignments

    static DecimalFormat dec = new DecimalFormat("0.0000");
    static DecimalFormat rnd = new DecimalFormat("+00;-00");
    
    public static void main(String[] args) {

        if (args.length<1) {
            System.err.println("Usage: SequenceBlaster <multi-sequence.fasta> [maxDistance] [gop] [gep]");
            System.exit(0);
        }

        // defaults
        double maxDistance = MAX_DISTANCE;
        int gop = GOP;
        int gep = GEP;

        if (args.length>1) maxDistance = Double.parseDouble(args[1]);
        if (args.length>3) gop = Integer.parseInt(args[3]);
        if (args.length>4) gep = Integer.parseInt(args[4]);

        GapPenalty gapPenalty = new SimpleGapPenalty(gop, gep);
        SubstitutionMatrix<NucleotideCompound> subMatrix = SubstitutionMatrixHelper.getNuc4_4();

        try {
            
            // BLAST the FASTA!
            long blastStart = System.currentTimeMillis();
            TreeSet<SequenceHits> seqHitsSet = BlastUtils.blastSequenceHits(args[0]);
            long blastEnd = System.currentTimeMillis();

            // collect top numKept singular motifs for further analysis
            long pairwiseStart = System.currentTimeMillis();
            boolean first = true;
            int count = 0;
            DNASequence topMotif = null;
            List<DNASequence> logoMotifs = new ArrayList<DNASequence>();
            for (SequenceHits seqHits : seqHitsSet.descendingSet()) {

                // keep this motif iff it hits against each ID in only one place
                boolean keep = true;
                TreeSet<String> idSet = new TreeSet<String>();
                for (String hit : seqHits.uniqueHits) {
                    String[] pieces = hit.split(":");
                    String id = pieces[0];
                    if (idSet.contains(id)) keep = false;
                    idSet.add(id);
                }
                // sometimes you get two nearly identical input sequences; they'll give score = -1
                if (keep && seqHits.score>0) {
                    
                    count++;
                    
                    if (first) {
                        // save the top motif for pairwise alignments
                        topMotif = new DNASequence(seqHits.sequence);
                        logoMotifs.add(topMotif);
                        System.out.print(seqHits.sequence+"\t["+seqHits.score+"]["+seqHits.uniqueHits.size()+"]");
                        System.out.println("\tscore\tdistance\tsimilarity");
                        first = false;
                    } else {
                        // do a pairwise alignment with topMotif and add to logo list if close enough
                        // pairwise alignment choices: AnchoredPairwiseSequenceAligner, GuanUberbacher, NeedlemanWunsch, SmithWaterman
                        DNASequence thisMotif = new DNASequence(seqHits.sequence);
                        AnchoredPairwiseSequenceAligner<DNASequence,NucleotideCompound> aligner =
                            new AnchoredPairwiseSequenceAligner<DNASequence,NucleotideCompound>(thisMotif, topMotif, gapPenalty, subMatrix);
                        double score = aligner.getScore();
                        double distance = aligner.getDistance();
                        double similarity = aligner.getSimilarity();
                        System.out.print(seqHits.sequence+"\t["+seqHits.score+"]["+seqHits.uniqueHits.size()+"]");
                        System.out.print("\t"+rnd.format(score)+"\t"+dec.format(distance)+"\t"+dec.format(similarity));
                        if (distance<maxDistance) {
                            logoMotifs.add(thisMotif);
                            System.out.println("\t*"+count);
                        } else {
                            System.out.println();
                        }
                    }
                    
                    // // WEIGHTED FASTA-PER-MOTIF VERSION
                    // double weight = (double)seqHits.score/(double)maxScore;
                    // System.out.println(">WEIGHTS "+weight);
                    // System.out.println(">"+seqHits.uniqueHits.size()+"x"+seqHits.sequence.length()+"["+seqHits.score+"]"+seqHits.sequence);
                    // System.out.println(seqHits.sequence);
                    
                    // // FASTA-PER-HIT VERSION
                    // for (String hit : seqHits.uniqueHits) {
                    //     System.out.println(">["+seqHits.score+"]["+seqHits.sequence.length()+"]"+hit.replace(' ','-'));
                    //     System.out.println(seqHits.sequence);
                    // }
                    
                    // // TEXT VERSION
                    // System.out.println(seqHits.sequence+"["+seqHits.score+"]");
                    // for (String hit : seqHits.uniqueHits) {
                    //     String[] pieces = hit.split(":");
                    //     String id = pieces[0];
                    //     System.out.println("\t"+hit);
                    // }
                    
                    // MINIMAL TEXT VERSION
                    // System.out.println(seqHits.sequence+"\t["+seqHits.score+"]["+seqHits.uniqueHits.size()+"]");
                    
                }
                
            }

            long pairwiseEnd = System.currentTimeMillis();
            System.out.println();
            System.out.println("------- "+logoMotifs.size()+" motifs in sequence logo -----");

            long multiStart = 0;
            long multiEnd = 0;

            if (logoMotifs.size()>1) {

                // do a multiple alignment of the kept motifs and write to a FASTA for sequence logo generation (which may be uninformative)
                multiStart = System.currentTimeMillis();
                Object[] settings = new Object[3];
                settings[0] = gapPenalty;
                settings[1] = Alignments.PairwiseSequenceScorerType.GLOBAL_IDENTITIES;
                settings[2] = Alignments.ProfileProfileAlignerType.GLOBAL;
                Profile<DNASequence,NucleotideCompound> profile = Alignments.getMultipleSequenceAlignment(logoMotifs, settings);
                multiEnd = System.currentTimeMillis();
                List<DNASequence> dseqs = new ArrayList<DNASequence>();
                for (AlignedSequence aseq : profile) {
                    DNASequence dseq = new DNASequence(aseq.getSequenceAsString());
                    dseq.setOriginalHeader(aseq.getOriginalSequence().getSequenceAsString());
                    dseqs.add(dseq);
                    System.out.println(dseq.getSequenceAsString());
                }
                FastaWriterHelper.writeNucleotideSequence(new File("/tmp/alignment.fasta"), dseqs);

            }

            // timing output
            System.out.println();
            System.out.println("BLAST runs took "+(blastEnd-blastStart)+" ms.");
            System.out.println("Pairwise alignments with top motif took "+(pairwiseEnd-pairwiseStart)+" ms.");
            if (multiStart>0) System.out.println("Multiple sequence alignment took "+(multiEnd-multiStart)+" ms.");

        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(1);
        } finally {
            ConcurrencyTools.shutdown();  
        }            

    }
    
}
