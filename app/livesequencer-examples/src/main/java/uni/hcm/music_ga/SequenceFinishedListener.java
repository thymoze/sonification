package uni.hcm.music_ga;

/**
 * This interface is used to give the Csound generator feedback about a generated sequence.
 */
public interface SequenceFinishedListener {
    /**
     * This event is thrown when a sequence is finished.
     *
     * @return the rating by the user of the just finished sequence
     */
    double onSequenceFinished();
}
