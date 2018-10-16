package uni.hcm.livesequencer;

import org.junit.Test;

import java.util.TreeSet;

import uni.hcm.livesequencer.events.TempoChangeEvent;
import uni.hcm.livesequencer.helper.BeatsToSecondsConverter;
import uni.hcm.livesequencer.instrument.CsoundInstrument;
import uni.hcm.livesequencer.note.CsoundMidiNote;
import uni.hcm.livesequencer.note.Note;

import static org.junit.Assert.assertEquals;

public class TempoUnitTest {
    @Test
    public void calculate_offset_seconds() {

        Note testNote = new CsoundMidiNote(new CsoundInstrument("dummy", false), 4d, 2.5d, 127, 69);
        TreeSet<TempoChangeEvent> tempoChangeEventList = new TreeSet<>();
        tempoChangeEventList.add(new TempoChangeEvent(0d, 60d));
        tempoChangeEventList.add(new TempoChangeEvent(2d, 120d));
        tempoChangeEventList.add(new TempoChangeEvent(4d, 30d));

        BeatsToSecondsConverter converter = new BeatsToSecondsConverter(tempoChangeEventList, testNote);

        assertEquals(3d, converter.calculateOffsetSeconds(), 0d);
    }

    @Test
    public void calculate_length_seconds() {

        Note testNote = new CsoundMidiNote(new CsoundInstrument("dummy", false), 4d, 2.5d, 127, 69);
        TreeSet<TempoChangeEvent> tempoChangeEventList = new TreeSet<>();
        tempoChangeEventList.add(new TempoChangeEvent(0d, 60d));
        tempoChangeEventList.add(new TempoChangeEvent(2d, 120d));
        tempoChangeEventList.add(new TempoChangeEvent(4d, 30d));

        BeatsToSecondsConverter converter = new BeatsToSecondsConverter(tempoChangeEventList, testNote);

        assertEquals(2d, converter.calculateLengthSeconds(), 0d);
    }
}
