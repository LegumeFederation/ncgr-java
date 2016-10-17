package org.ncgr.blast;

import java.util.TreeMap;
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
            // max score for weights
            int maxScore = 0;
            for (SequenceHits seqHits : seqHitsSet.descendingSet()) {
                if (seqHits.score>maxScore) maxScore = seqHits.score;
            }
            int num = 0;
            for (SequenceHits seqHits : seqHitsSet.descendingSet()) {
                // // TEXT VERSION
                // System.out.println(seqHits.uniqueHits.size()+"x"+seqHits.sequence.length()+"["+seqHits.score+"]"+seqHits.sequence);
                // for (String hit : seqHits.uniqueHits) {
                //     System.out.println("\t"+hit);
                // }
                // // WEIGHTED FASTA-PER-MOTIF VERSION
                // double weight = (double)seqHits.score/(double)maxScore;
                // System.out.println(">WEIGHTS "+weight);
                // System.out.println(">"+seqHits.uniqueHits.size()+"x"+seqHits.sequence.length()+"["+seqHits.score+"]"+seqHits.sequence);
                // System.out.println(seqHits.sequence);
                // FASTA-PER-HIT VERSION
                // for (String hit : seqHits.uniqueHits) {
                //     int count = (int) seqHits.uniqueHits.get(hit);
                //     System.out.println(">["+(++num)+"]["+seqHits.score+"]"+hit.replace(' ','-')+"["+count+"]");
                //     System.out.println(seqHits.sequence);
                // }
                // reject this sequence if it hits against any ID in more than one place
                boolean reject = false;
                TreeSet<String> idSet = new TreeSet<String>();
                for (String hit : seqHits.uniqueHits) {
                    String[] pieces = hit.split(":");
                    String id = pieces[0];
                    if (idSet.contains(id)) {
                        reject = true;
                    }
                    idSet.add(id);
                }
                if (!reject) {
                    System.out.println(seqHits.sequence+"["+seqHits.score+"]\t"+reject);
                    for (String hit : seqHits.uniqueHits) {
                        String[] pieces = hit.split(":");
                        String id = pieces[0];
                        System.out.println("\t"+hit);
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(1);
        }

    }
    
}
