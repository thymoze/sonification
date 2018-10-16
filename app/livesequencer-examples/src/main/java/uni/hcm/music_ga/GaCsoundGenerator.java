package uni.hcm.music_ga;

import android.util.Log;

import net.sf.jclec.RunExperiment;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import uni.hcm.livesequencer.Sequence;
import uni.hcm.livesequencer.composable.FluidGenerator;
import uni.hcm.livesequencer.instrument.CsoundInstrument;
import uni.hcm.livesequencer.instrument.FluidCsoundInstrument;
import uni.hcm.livesequencer.note.CsoundMidiNote;
import uni.hcm.livesequencer.note.Note;

/**
 * A CsoundGenerator using a GA with user feedback to generate sequences.
 */
public class GaCsoundGenerator extends FluidGenerator {

    private final FluidCsoundInstrument piano;
    private final SequenceFinishedListener sequenceFinishedListener;

    /**
     * Instantiates a new GA csound generator.
     *
     * @param soundFont the sound font to be played by this generator
     * @param listener  the listener to receive feedback after a finished round
     * @param gaconfig  the configuration file for the Genetic Algorithm
     */
    public GaCsoundGenerator(final File soundFont, final SequenceFinishedListener listener, final File gaconfig) {
        piano = new FluidCsoundInstrument(soundFont, 0, 2);
        sequenceFinishedListener = listener;

        @SuppressWarnings("Convert2Lambda") // for support of Android API < 26
                Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                RunExperiment.main(gaconfig.getAbsolutePath().split("\\s+"));
            }
        });
        t.start();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<CsoundInstrument> getFluidInstrumentSet() {
        Set<CsoundInstrument> returnSet = new HashSet<>();
        returnSet.add(piano);
        return returnSet;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Sequence> composeFluidSequence(final double lengthSeconds) {
        SequenceEvaluator.rating = sequenceFinishedListener.onSequenceFinished();
        synchronized (SequenceEvaluator.obj) {
            SequenceEvaluator.obj.notifyAll();
        }

        while (SequenceEvaluator.currentGenotype == null) {
            try {
                Thread.sleep(500);
                Log.d("LiveSequencerExamples", "Waiting for new genotype.");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        final List<Note> noteList = new ArrayList<>();

        int i = 0;
        for (final int midiNumber : SequenceEvaluator.currentGenotype) {
            final CsoundMidiNote newNote = new CsoundMidiNote(piano, i, 1, 127, midiNumber);
            noteList.add(newNote);
            i++;
        }

        final List<Sequence> sequences = new ArrayList<>();
        sequences.add(new Sequence(noteList));

        return sequences;
    }
}
