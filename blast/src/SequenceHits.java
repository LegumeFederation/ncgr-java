package org.ncgr.blast;

import java.util.TreeSet;

/**
 * A simple class that stores a sequence along with a set of the query IDs that contain that particular sequence.
 * equals and compareTo are designed to sort by (number of hits)*length, number of hits, sequence length then sequence alpha.
 * It also contains a score given by BlastUtils.scoreDNASequence so that it need only be computed once.
 *
 * @author Sam Hokin
 */
public class SequenceHits implements Comparable {

    public String sequence = "";
    public TreeSet<String> idSet;
    public int baseScore; // the sequence-only score, calculated at construction
    public int score;     // the full score including the contribution from the number of hits, updated in addId()

    /**
     * Create a new SequenceHits instance from an input sequence and ID
     */
    public SequenceHits(String sequence, String id) {
        this.sequence = sequence;
        baseScore = (int) Math.round(100.0*BlastUtils.scoreDNASequence(sequence));
        score = baseScore;
        idSet = new TreeSet<String>();
        idSet.add(id);
    }
        
    /**
     * Two are equal if they have the same sequence (regardless of hits)
     */
    public boolean equals(Object o) {
        SequenceHits that = (SequenceHits) o;
        return this.sequence.equals(that.sequence);
    }

    /**
     * Order by score, then hits, then alphabetic
     */
    public int compareTo(Object o) {
        SequenceHits that = (SequenceHits) o;
        int thisHits = this.idSet.size();
        int thatHits = that.idSet.size();
        if (this.score!=that.score) {
            return this.score-that.score;
        } else if (thisHits!=thatHits) {
            return thisHits - thatHits;
        } else {
            return this.sequence.compareTo(that.sequence);
        }
    }

    /**
     * Add a sequence ID to the ID set and update the score
     */
    public void addId(String id) {
        idSet.add(id);
        score = baseScore*idSet.size();
    }

}
