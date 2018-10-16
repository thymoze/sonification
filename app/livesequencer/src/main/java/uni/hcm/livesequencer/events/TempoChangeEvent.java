package uni.hcm.livesequencer.events;

import android.support.annotation.NonNull;

import uni.hcm.livesequencer.Sequence;

/**
 * Models an event that changes tempo during a {@link Sequence}.
 */
public class TempoChangeEvent implements Comparable<TempoChangeEvent> {

    private double offsetSeconds;
    private double bpm;

    /**
     * Instantiates a new Tempo change event.
     *
     * @param offsetSeconds the offset seconds
     * @param bpm           the bpm
     */
    public TempoChangeEvent(final double offsetSeconds, final double bpm) {
        setOffsetSeconds(offsetSeconds);
        setBpm(bpm);
    }

    /**
     * Gets the time in seconds after the beginning of the sequence this tempo is valid.
     *
     * @return the offset in seconds to wait from beginning of the sequence until this tempo is valid
     */
    public double getOffsetSeconds() {
        return offsetSeconds;
    }

    /**
     * Sets the time in seconds after the beginning of the sequence this tempo is valid.
     *
     * @param offsetSeconds the offset in seconds to wait from beginning of the sequence until this tempo is valid
     */
    public void setOffsetSeconds(double offsetSeconds) {
        this.offsetSeconds = offsetSeconds;
    }

    /**
     * Gets the new speed of the sequence in beats per minute.
     *
     * @return the new speed in beats per minute
     */
    public double getBpm() {
        return bpm;
    }

    /**
     * Sets the new speed of the sequence in beats per minute.
     *
     * @param bpm the new speed in beats per minute
     */
    public void setBpm(double bpm) {
        this.bpm = bpm;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(final @NonNull TempoChangeEvent o) {
        final double deltaSeconds = getOffsetSeconds() - o.getOffsetSeconds();
        if (deltaSeconds < 0) {
            return -1;
        } else if (deltaSeconds > 0) {
            return 1;
        } else
            return 0;
    }
}