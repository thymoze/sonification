package uni.hcm.livesequencer.note;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import uni.hcm.livesequencer.instrument.CsoundInstrument;
import uni.hcm.livesequencer.sequencer.CsoundSequencer;

/**
 * This class models a {@link MidiNote} to be used with {@link CsoundSequencer}.
 *
 * @see Note
 */
public class CsoundMidiNote extends MidiNote {

    private CsoundInstrument csoundInstrument;
    private List<Double> additionalArguments = new ArrayList<>();

    /**
     * Creates a {@link CsoundMidiNote} with the {@link CsoundInstrument} to be played with.
     *
     * @param csoundInstrument    the instrument to play this note with
     * @param offsetBeats         the number of beats to wait until this note is being played
     * @param lengthBeats         the number of beats this note is being played
     * @param velocity            the force which an instrument is being played with, not only loudness, but also sound can be different
     * @param midiNumber          the number of note in Midi format
     * @param additionalArguments the additional arguments for Csound
     * @see MidiNote
     */
    public CsoundMidiNote(final CsoundInstrument csoundInstrument, final double offsetBeats, final double lengthBeats, final int velocity, final int midiNumber, final Double... additionalArguments) {
        super(offsetBeats, lengthBeats, velocity, midiNumber);
        setCsoundInstrument(csoundInstrument);
        setOffsetBeats(offsetBeats);
        setAdditionalArguments(new ArrayList<>(Arrays.asList(additionalArguments)));
    }

    /**
     * Creates a {@link CsoundMidiNote} with the {@link CsoundInstrument} to be played with.
     *
     * @param csoundInstrument    the instrument to play this note with
     * @param offsetBeats         the number of beats to wait until this note is being played
     * @param lengthBeats         the number of beats this note is being played
     * @param velocity            the force which an instrument is being played with, not only loudness, but also sound can be different
     * @param noteName            the note name of this note
     * @param additionalArguments the additional arguments for Csound
     * @see MidiNote
     */
    public CsoundMidiNote(final CsoundInstrument csoundInstrument, final double offsetBeats, final double lengthBeats, final int velocity, final String noteName, final Double... additionalArguments) {
        super(offsetBeats, lengthBeats, velocity, noteName);
        setCsoundInstrument(csoundInstrument);
        setOffsetBeats(offsetBeats);
        setAdditionalArguments(new ArrayList<>(Arrays.asList(additionalArguments)));
    }

    /**
     * {@inheritDoc} The Csound implementation of the note payload consists multiple entries: First is {@code i} followed by the label of instrument. Second is the offset, the number of beats to wait until this note should be played. Third is the length, the number of beats to play this note.
     * The last two arguments are the velocity (the force which an instrument is being played with), and the frequency to play this instrument with. All of the entries are separated by a whitespace. There can be added an arbitrary number of additional arguments in double data type.
     *
     * @return the generated payload in Csound score style
     */
    @Override
    public String generatePayload(final double offsetSeconds, final double lengthSeconds) {
        final StringBuilder payload = new StringBuilder();
        payload.append("i").append(getCsoundInstrument().getId());

        payload.append(" ").append(offsetSeconds);

        payload.append(" ").append(lengthSeconds);
        payload.append(" ").append(getCsoundVelocity());

        payload.append(" ");

        if (getCsoundInstrument().isPlayInFrequency()) {
            payload.append(String.valueOf(getFrequency(getMidiNumber())));
        } else {
            payload.append(String.valueOf(getMidiNumber()));
        }

        for (final Double argument : getAdditionalArguments()) {
            payload.append(" ").append(argument);
        }

        return payload.toString();
    }

    /**
     * The velocity is defined as the force which an instrument is being played with, which can affect not only loudness, but also sound can be different.
     * In Midi it is defined as a value from 0 to 127, this function converts it to a value between 0 and 1 for Csound.
     *
     * @return velocity this note is being played with
     */
    public float getCsoundVelocity() {
        return ((float) getVelocity()) / 127f;
    }

    /**
     * Get the frequency according to the input {@code midiNumber}.
     *
     * @return frequency of note {@code midiNumber}
     */
    private float getFrequency(final int midiNumber) {
        final double concertAFreq = 440d;
        final int keyA4 = 69;
        return (float) (concertAFreq * Math.pow(2, (midiNumber - keyA4) / 12d));
    }

    /**
     * Gets {@link CsoundInstrument} this note should be played with.
     *
     * @return the linked {@link CsoundInstrument} this note should be played with
     */
    public CsoundInstrument getCsoundInstrument() {
        return csoundInstrument;
    }

    /**
     * Sets {@link CsoundInstrument} this note should be played with.
     *
     * @param csoundInstrument the {@link CsoundInstrument} this note should be played with
     */
    public void setCsoundInstrument(final CsoundInstrument csoundInstrument) {
        this.csoundInstrument = csoundInstrument;
    }

    /**
     * Gets additional arguments. This can be used in Csound for additional attributes for the CsoundInstrument.
     *
     * @return the additional arguments for Csound
     */
    public List<Double> getAdditionalArguments() {
        return additionalArguments;
    }

    /**
     * Sets additional arguments. This can be used in Csound for additional attributes for the CsoundInstrument.
     *
     * @param additionalArguments the additional arguments for Csound
     */
    public void setAdditionalArguments(final List<Double> additionalArguments) {
        this.additionalArguments = additionalArguments;
    }
}
