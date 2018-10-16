package uni.hcm.livesequencer.note;

import uni.hcm.livesequencer.sequencer.Sequencer;

/**
 * This class models a note to be used with a {@link Sequencer}.
 */
public abstract class Note {

    private double offsetBeats;
    private double lengthBeats;
    private double additionalOffsetSeconds;

    /**
     * Instantiates a new Note.
     *
     * @param offsetBeats the time in beats to wait until this note is played
     * @param lengthBeats the length in beats this note is being played
     */
    Note(final double offsetBeats, final double lengthBeats) {
        setOffsetBeats(offsetBeats);
        setLengthBeats(lengthBeats);
    }

    /**
     * Generate the payload that contains everything of this note.
     *
     * @param offsetSeconds the entire time in seconds to wait until this note is played
     * @param lengthSeconds the length in seconds this note is being played
     * @return payload that contains everything needed to play this note
     */
    public abstract String generatePayload(final double offsetSeconds, final double lengthSeconds);

    /**
     * @return the number of beats to wait until this note is being played
     */
    public double getOffsetBeats() {
        return offsetBeats;
    }

    /**
     * @param offsetBeats the number of beats to wait until this note is being played
     */
    public void setOffsetBeats(final double offsetBeats) {
        this.offsetBeats = offsetBeats;
    }

    /**
     * @return the number of beats this note is being played
     */
    public double getLengthBeats() {
        return lengthBeats;
    }

    /**
     * @param lengthBeats the number of beats this note is being played
     */
    public void setLengthBeats(final double lengthBeats) {
        this.lengthBeats = lengthBeats;
    }

    /**
     * Gets additional deferral time in seconds to wait until this note is being played.
     *
     * @return the additional deferral time of playing this note in seconds
     */
    public double getAdditionalOffsetSeconds() {
        return additionalOffsetSeconds;
    }

    /**
     * Add an additional deferral time in seconds to wait until this note is being played.
     *
     * @param additionalOffsetSeconds the additional deferral time of playing this note in seconds
     */
    public void addAdditionalOffsetSeconds(final double additionalOffsetSeconds) {
        this.additionalOffsetSeconds += additionalOffsetSeconds;
    }
}