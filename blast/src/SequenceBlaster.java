package org.ncgr.blast;

import java.util.TreeSet;

/**
 * Load a multi-fasta containing sequences, and search them for common motifs using the BlastUtils blastSequenceHits utility.
 *
 * @author Sam Hokin
 */
public class SequenceBlaster {

    public static void main(String[] args) {

        if (args.length!=1) {
            System.err.println("Usage: SequenceBlaster <multi-sequence.fasta>");
            System.exit(1);
        }

        try {
            TreeSet<SequenceHits> seqHitsSet = BlastUtils.blastSequenceHits(args[0]);
            for (SequenceHits seqHits : seqHitsSet.descendingSet()) {
                int hits = seqHits.idSet.size();
                System.out.println(hits+"x"+seqHits.sequence.length()+" ["+seqHits.score+"]\t"+seqHits.sequence);
                for (String id : seqHits.idSet) {
                    System.out.println("\t"+id);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(1);
        }

    }
    
}
