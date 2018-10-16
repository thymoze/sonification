package uni.hcm.livesequencer;

import org.junit.Test;

import uni.hcm.livesequencer.instrument.CsoundInstrument;
import uni.hcm.livesequencer.note.CsoundMidiNote;

import static org.junit.Assert.assertEquals;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class MidiUnitTest {
    @Test
    public void midi_code_to_number() {
        CsoundInstrument dummy = new CsoundInstrument("dummy", false);
        CsoundMidiNote note = new CsoundMidiNote(dummy, 0, 1, 100, 69);
        assertEquals("A4", note.getName());
    }

    @Test
    public void number_to_midi_code() {
        CsoundInstrument dummy = new CsoundInstrument("dummy", false);
        CsoundMidiNote note = new CsoundMidiNote(dummy, 0, 1, 100, "A#4");
        assertEquals(70, note.getMidiNumber());

        note = new CsoundMidiNote(dummy, 0, 1, 100, "C-1");
        assertEquals(0, note.getMidiNumber());

        note = new CsoundMidiNote(dummy, 0, 1, 100, "G9");
        assertEquals(127, note.getMidiNumber());
    }

    @Test(expected = IllegalArgumentException.class)
    public void number_to_midi_code_out_of_upper_bounds() {
        CsoundInstrument dummy = new CsoundInstrument("dummy", false);
        new CsoundMidiNote(dummy, 0, 1, 100, "B9");
    }

    @Test(expected = IllegalArgumentException.class)
    public void number_to_midi_code_out_of_lower_bounds() {
        CsoundInstrument dummy = new CsoundInstrument("dummy", false);
        new CsoundMidiNote(dummy, 0, 1, 100, "B-2");
    }
}