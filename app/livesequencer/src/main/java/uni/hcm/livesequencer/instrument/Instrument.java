package uni.hcm.livesequencer.instrument;

import uni.hcm.livesequencer.sequencer.Sequencer;

/**
 * Models an instrument to be played with a {@link Sequencer}.
 */
public abstract class Instrument {

    private static int counter = 1;
    private final int id;

    /**
     * Models an instrument to be played with a {@link Sequencer}.
     */
    Instrument() {
        id = counter++;
    }

    /**
     * @return the unique id of this instrument
     */
    public int getId() {
        return id;
    }
}