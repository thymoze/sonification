package uni.hcm.music_ga;

import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import uni.hcm.livesequencer.Sequence;
import uni.hcm.livesequencer.composable.FluidGenerator;
import uni.hcm.livesequencer.helper.MidiReadIn;
import uni.hcm.livesequencer.instrument.CsoundInstrument;
import uni.hcm.livesequencer.instrument.FluidCsoundInstrument;

/**
 * Easy playback of a Midi file to show the ability to read in Midi file and play with a sound font.
 */
public class MidiCsoundGenerator extends FluidGenerator {

    MidiReadIn midi;

    /**
     * Reads in a Midi file and gets the according {@link FluidCsoundInstrument FluidSynthInstruments} of the provided sound file.
     * After that it adds the according Instruments to make them playable.
     */
    public MidiCsoundGenerator(final File inputMidi, final File soundFont) {
        try {
            midi = new MidiReadIn(inputMidi, soundFont);
        } catch (IOException e) {
            Log.e("E", "Error on loading midi file.", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<CsoundInstrument> getFluidInstrumentSet() {
        return new HashSet<>(midi.getInstruments());
    }

    /**
     * Reads in a sequence of {@code lengthSeconds} of provided Midi file.
     *
     * @param lengthSeconds the desired length of the composed sequence in seconds
     * @return a set containing only the sequence of {@code lengthSeconds} of provided Midi file.
     * @see MidiReadIn#readInSequence(double)
     * @see FluidGenerator#composeFluidSequence(double)
     */
    @Override
    public List<Sequence> composeFluidSequence(final double lengthSeconds) {
        final List<Sequence> sequences = new ArrayList<>();
        sequences.add(midi.readInSequence(lengthSeconds));
        return sequences;
    }
}