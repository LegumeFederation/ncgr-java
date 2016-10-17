package org.ncgr.blast;

import java.util.TreeMap;
import java.util.TreeSet;

/**
 * A container class that stores SequenceHit instances for the same combined sequence.
 * The method equals() matches against sequence only; compareTo() is designed to sort by score, then number of hits, then sequence alpha.
 * The score is simply the sum of SequenceHit.score values.
 *
 * @author Sam Hokin
 */
public class SequenceHits implements Comparable {

    public String sequence;                   // the sequence associated with the SequenceHit objects
    public TreeSet<SequenceHit> sequenceHits; // the SequenceHit instances contained within
    public int score;                         // the full score = sum of SequenceHit.score values.
    public TreeSet<String> uniqueHits;        // a set of strings representing unique hits of the form seqID:start-end

    /**
     * Create a new SequenceHits instance from a SequenceHit
     */
    public SequenceHits(SequenceHit sequenceHit) {
        this.sequence = sequenceHit.sequence;
        this.score = sequenceHit.score;
        this.sequenceHits = new TreeSet<SequenceHit>();
        this.sequenceHits.add(sequenceHit);
        this.uniqueHits = new TreeSet<String>();
        this.uniqueHits.add(sequenceHit.getQueryLoc());
        this.uniqueHits.add(sequenceHit.getHitLoc());
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
        int thisHits = this.sequenceHits.size();
        int thatHits = that.sequenceHits.size();
        if (this.score!=that.score) {
            return this.score-that.score;
        } else if (thisHits!=thatHits) {
            return thisHits - thatHits;
        } else {
            return this.sequence.compareTo(that.sequence);
        }
    }

    /**
     * Add a SequenceHit to the set and adjust the score, and add to uniqueHits set
     */
    public void addSequenceHit(SequenceHit sequenceHit) {
        sequenceHits.add(sequenceHit);
        int oldSize = uniqueHits.size();
        uniqueHits.add(sequenceHit.getQueryLoc());
        uniqueHits.add(sequenceHit.getHitLoc());
        int newSize = uniqueHits.size();
        if (newSize!=oldSize) score += (newSize-oldSize)*sequenceHit.score;
    }

}
