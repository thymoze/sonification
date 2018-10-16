package uni.hcm.livesequencer.helper;

import java.util.Iterator;
import java.util.TreeSet;

import uni.hcm.livesequencer.events.TempoChangeEvent;
import uni.hcm.livesequencer.note.Note;

/**
 * A beats to seconds converter in order to get all needed values to play a note according to the provided {@link TempoChangeEvent TempoChangeEvents}.
 */
public class BeatsToSecondsConverter {

    private final TreeSet<TempoChangeEvent> tempoChangeEvents;
    private final Note note;

    /**
     * This helper class converts beats of a {@link Note} to seconds.
     *
     * @param tempoChangeEvents the tempo change events according to which the {@code note} should be converted
     * @param note              the note with the required values to convert
     */
    public BeatsToSecondsConverter(final TreeSet<TempoChangeEvent> tempoChangeEvents, final Note note) {
        this.tempoChangeEvents = tempoChangeEvents;
        this.note = note;
    }

    /**
     * Converts a time span in beats to seconds.
     *
     * @param offsetSeconds     the offset time in seconds, this means the length of the time period until the time span to convert starts
     * @param lengthBeats       the length in beats
     * @param tempoChangeEvents the tempo change events according to which the time span should be converted
     * @return the length of the time span in seconds
     */
    public static double convertBeatsTimeSpanToSeconds(final double offsetSeconds, double lengthBeats, final TreeSet<TempoChangeEvent> tempoChangeEvents) {
        double timeSpanSeconds = 0d;

        for (final Iterator<TempoChangeEvent> i = tempoChangeEvents.iterator(); i.hasNext(); ) {
            TempoChangeEvent thisTempo = i.next();
            if (i.hasNext()) {
                TempoChangeEvent nextTempo = tempoChangeEvents.higher(thisTempo);

                if (nextTempo.getOffsetSeconds() < offsetSeconds) {
                    continue;
                }

                double usableBeats = calculateDeltaBeats(thisTempo, nextTempo);

                if (offsetSeconds > thisTempo.getOffsetSeconds()) {
                    usableBeats -= secondsToBeats(offsetSeconds - thisTempo.getOffsetSeconds(), thisTempo.getBpm());
                }

                final double usableSeconds = beatsToSeconds(usableBeats, thisTempo.getBpm());

                if (lengthBeats <= usableBeats) {
                    timeSpanSeconds += beatsToSeconds(lengthBeats, thisTempo.getBpm());
                    break;
                } else {
                    timeSpanSeconds += usableSeconds;
                    lengthBeats -= usableBeats;
                }
            } else {
                timeSpanSeconds += beatsToSeconds(lengthBeats, thisTempo.getBpm());
            }
        }
        return timeSpanSeconds;
    }

    /**
     * Converts beats to seconds according to {@code bpm}.
     *
     * @param beats the beats
     * @param bpm   the bpm
     * @return the double
     */
    public static double beatsToSeconds(final double beats, final double bpm) {
        return beats * (60d / bpm);
    }

    /**
     * Converts seconds to beats according to {@code bpm}.
     *
     * @param seconds the seconds
     * @param bpm     the bpm
     * @return the double
     */
    public static double secondsToBeats(final double seconds, final double bpm) {
        return (bpm / 60d) * seconds;
    }

    /**
     * This function calculates the time in beats between two {@link TempoChangeEvent TempoChangeEvents}.
     *
     * @param firstTempo  the first tempo where the time span begins, this also sets the tempo for this span
     * @param secondTempo the second tempo where the time span ends
     * @return time in beats between {@code firstTempo} and {@code secondTempo}
     */
    private static double calculateDeltaBeats(final TempoChangeEvent firstTempo, final TempoChangeEvent secondTempo) {
        final double deltaSeconds = secondTempo.getOffsetSeconds() - firstTempo.getOffsetSeconds();
        return secondsToBeats(deltaSeconds, firstTempo.getBpm());
    }

    /**
     * Calculate offset in seconds. The offset is the time to wait until a note is played.
     *
     * @return the offset in seconds
     */
    public double calculateOffsetSeconds() {
        return convertBeatsTimeSpanToSeconds(0d, note.getOffsetBeats(), tempoChangeEvents);
    }

    /**
     * Calculate length seconds double. The length is the time the note is being played.
     *
     * @return the length in seconds
     */
    public double calculateLengthSeconds() {
        double offsetSeconds = calculateOffsetSeconds();
        return convertBeatsTimeSpanToSeconds(offsetSeconds, note.getLengthBeats(), tempoChangeEvents);
    }
}