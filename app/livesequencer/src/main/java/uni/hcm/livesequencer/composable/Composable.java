package uni.hcm.livesequencer.composable;

import java.util.List;
import java.util.Set;

import uni.hcm.livesequencer.Sequence;
import uni.hcm.livesequencer.instrument.Instrument;

/**
 * Interface to classes being able to compose sequences. Also needs to return the set of all instruments being used in the compositions.
 */
public interface Composable {
    /**
     * Compose a set of sequences. One sequence represents one instrument for the period of one sequence length.
     *
     * @param lengthSeconds the desired length of the composed sequence in seconds
     * @return the composed list of sequences for all instruments.
     */
    List<Sequence> composeSequence(final double lengthSeconds);

    /**
     * Return all instruments being used during {@link #composeSequence}
     *
     * @return the instrument set being used in compositions.
     */
    Set<? extends Instrument> getInstrumentSet();
}