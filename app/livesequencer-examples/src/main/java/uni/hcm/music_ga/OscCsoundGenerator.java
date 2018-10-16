package uni.hcm.music_ga;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import uni.hcm.livesequencer.Sequence;
import uni.hcm.livesequencer.composable.CsoundComposable;
import uni.hcm.livesequencer.helper.CsoundOSCHelper;
import uni.hcm.livesequencer.instrument.CsoundInstrument;
import uni.hcm.livesequencer.note.CsoundFunctionTableNote;
import uni.hcm.livesequencer.note.CsoundMidiNote;
import uni.hcm.livesequencer.note.Note;

/**
 * Easy OSC listener to show the ability to listen for OSC messages to control a {@link CsoundInstrument}.
 * Send float values to port 8000 and hear some siren-like synthesizer sound.
 * Furthermore you can control the amplitude with a binding on UI with the name "amplitude".
 */
public class OscCsoundGenerator implements CsoundComposable {

    final CsoundOSCHelper csoundOSCHelper = new CsoundOSCHelper(8000);
    final CsoundFunctionTableNote csoundFunctionTableNote = new CsoundFunctionTableNote(4096, 10, 1d);
    CsoundInstrument synth;

    /**
     * This adds the OSC listener to the Csound orchestra header.
     *
     * @return Csound orchestra string containing initialization of OSC listener
     */
    @Override
    public String initializeOrchestra() {
        return csoundOSCHelper.initialize();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<CsoundInstrument> getInstrumentSet() {
        synth = new CsoundInstrument("   kf1 init 0\n" +
                "   kf2 init 0\n" +
                "   kf3 init 0\n" +
                "   ksl chnget \"amplitude\" \n" +
                "nxtmsg:\n" +
                "    kk  OSClisten " + csoundOSCHelper.getHandle() + ", \"/foo/bar\", \"fii\", kf1, kf2, kf3\n" +
                "a1   \toscili   ksl, kf1, " + csoundFunctionTableNote.getId() + "\n" +
                "       \tout     a1\n" +
                "if (kk == 0) goto ex\n" +
                "    printk 0,kf1\n" +
                "    kgoto nxtmsg\n" +
                "ex:\n", true);

        final List<CsoundFunctionTableNote> functionTableNotes = new ArrayList<>();
        functionTableNotes.add(csoundFunctionTableNote);
        synth.setFunctionTableNotes(functionTableNotes);

        final Set<CsoundInstrument> returnSet = new HashSet<>();
        returnSet.add(synth);

        return returnSet;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Sequence> composeSequence(final double lengthSeconds) {
        final Note listenerNote = new CsoundMidiNote(synth, 0, lengthSeconds, 127, 69);
        final ArrayList<Note> noteList = new ArrayList<>();

        noteList.add(listenerNote);

        final Sequence s = new Sequence(noteList);
        final List<Sequence> listenerSequences = new ArrayList<>();

        listenerSequences.add(s);

        return listenerSequences;
    }
}
