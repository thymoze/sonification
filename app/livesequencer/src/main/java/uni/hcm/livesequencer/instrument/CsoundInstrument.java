package uni.hcm.livesequencer.instrument;

import java.util.ArrayList;
import java.util.List;

import uni.hcm.livesequencer.note.CsoundFunctionTableNote;

/**
 * This class models an {@link Instrument} using Csound to produce sound.
 */
public class CsoundInstrument extends Instrument {

    private String body;
    private boolean playInFrequency;
    private List<CsoundFunctionTableNote> functionTableNotes = new ArrayList<>();

    /**
     * Creates a model of a Csound instrument.
     *
     * @param body            the body of {@link CsoundInstrument}. A body includes everything of an instrument in Csound except begin and end commands.
     * @param playInFrequency determines whether to play this instrument in Csound with a frequency or with Midi notes.
     */
    public CsoundInstrument(final String body, final boolean playInFrequency) {
        super();
        setBody(body);
        setPlayInFrequency(playInFrequency);
    }

    /**
     * Creates a model of a Csound instrument.
     *
     * @param body            the body of {@link CsoundInstrument}. A body includes everything of an instrument in Csound except begin and end commands.
     * @param playInFrequency determines whether to play this instrument in Csound with a frequency or with Midi notes.
     */
    public CsoundInstrument(final String body, final boolean playInFrequency, List<CsoundFunctionTableNote> functionTableNotes) {
        this(body, playInFrequency);
        setFunctionTableNotes(functionTableNotes);
    }

    /**
     * Creates a model of a Csound instrument.
     *
     * @param body the body of {@link CsoundInstrument}. A body includes everything of an instrument in Csound except begin and end commands.
     */
    public CsoundInstrument(final String body) {
        this(body, false);
    }

    /**
     * A body contains everything of an instrument in Csound except its begin and end commands.
     *
     * @return the body of {@link CsoundInstrument}.
     */
    public String getBody() {
        return body;
    }

    /**
     * A body includes everything of an instrument in Csound.
     * The frame should be omitted, this means {@code instr}, the label of the instrument and {@code endin} must not be in this string.
     *
     * @param body the body of {@link CsoundInstrument}.
     */
    public void setBody(final String body) {
        this.body = body;
    }

    /**
     * Returns if an instrument is being played in Csound with a frequency or with Midi notes.
     *
     * @return whether to play this instrument in Csound with a frequency or with Midi notes.
     */
    public boolean isPlayInFrequency() {
        return playInFrequency;
    }

    /**
     * Sets if an instrument is being played in Csound with a frequency or with Midi notes.
     *
     * @param playInFrequency whether to play this instrument in Csound with a frequency or with Midi notes.
     */
    public void setPlayInFrequency(final boolean playInFrequency) {
        this.playInFrequency = playInFrequency;
    }

    /**
     * @return the function table notes to be played before initializing this instrument
     */
    public List<CsoundFunctionTableNote> getFunctionTableNotes() {
        return functionTableNotes;
    }

    /**
     * @param functionTableNotes the function table notes to be played before initializing this instrument
     */
    public void setFunctionTableNotes(List<CsoundFunctionTableNote> functionTableNotes) {
        this.functionTableNotes = functionTableNotes;
    }
}
