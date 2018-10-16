package uni.hcm.music_ga;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

import uni.hcm.livesequencer.Sequence;
import uni.hcm.livesequencer.composable.FluidGenerator;
import uni.hcm.livesequencer.helper.ScaleReference;
import uni.hcm.livesequencer.instrument.CsoundInstrument;
import uni.hcm.livesequencer.instrument.FluidCsoundInstrument;
import uni.hcm.livesequencer.note.CsoundMidiNote;
import uni.hcm.livesequencer.note.Note;

/**
 * A configurable sound generator to produce easy harmonic sequences.
 */
public class HarmonixCsoundGenerator extends FluidGenerator {
    private final static double DIRECTION_CHANGE_PROBABILITY = 0.1;
    private final static int MAX_STEP_SIZE = 12;
    private final static int BASE_NOTE = 80;
    private final static int MIDI_MIN_NUMBER = 50;
    private final static int MIDI_MAX_NUMBER = 100;
    private final static int MIN_SAMETIME_NOTES = 1;
    private final static int MAX_SAMETIME_NOTES = 2;
    private final FluidCsoundInstrument glockenspiel;
    private final Random random;
    private final ScaleReference selectedNotes = ScaleReference.PENTATONIC;
    private boolean directionUpwards = true;

    /**
     * Instantiates a new Harmonix Csound generator. It produces easy harmonic sequences.
     *
     * @param soundFont the sound font to play the instruments with
     */
    public HarmonixCsoundGenerator(final File soundFont) {
        glockenspiel = new FluidCsoundInstrument(soundFont, 0, 9);
        random = new Random();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<CsoundInstrument> getFluidInstrumentSet() {
        Set<CsoundInstrument> instrumentSet = new HashSet<>();
        instrumentSet.add(glockenspiel);
        return instrumentSet;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Sequence> composeFluidSequence(final double lengthSeconds) {
        List<Sequence> sequences = new ArrayList<>();
        ArrayList<Note> noteList = new ArrayList<>();

        int lastNoteMidiNumber = BASE_NOTE;
        for (int sequenceStep = 0; sequenceStep < (int) lengthSeconds; sequenceStep++) {
            for (int sametimesNote = 0; sametimesNote < getSametimeNotesNumber(); sametimesNote++) {
                CsoundMidiNote createdNote = createNote(lastNoteMidiNumber, sequenceStep);
                lastNoteMidiNumber = createdNote.getMidiNumber();
                noteList.add(createdNote);
            }
        }

        sequences.add(new Sequence(noteList));
        return sequences;
    }

    /**
     * Creates a new note in this sequence. Plays only notes of the subset {@code selectedNotes} and their octaves.
     * Changes the direction of the notes in sequence according to a set probability and uses a maximum up or down step size.
     *
     * @param lastNoteMidiNumber the midi number of last played note
     * @param noteInSequence     the number which note this is in this sequence
     * @return created next {@link CsoundMidiNote}
     */
    private CsoundMidiNote createNote(final int lastNoteMidiNumber, final int noteInSequence) {
        int midiNumber;
        if (DIRECTION_CHANGE_PROBABILITY <= random.nextDouble()) {
            directionUpwards = !directionUpwards;
        }

        int stepSize = random.nextInt(MAX_STEP_SIZE);

        if (directionUpwards) {
            midiNumber = lastNoteMidiNumber + stepSize;
            if (midiNumber > MIDI_MAX_NUMBER) {
                midiNumber = MIDI_MAX_NUMBER;
            }
            while (isNoteDisallowed(midiNumber)) {
                midiNumber--;
            }

        } else {
            midiNumber = lastNoteMidiNumber - stepSize;
            if (midiNumber < MIDI_MIN_NUMBER) {
                midiNumber = MIDI_MIN_NUMBER;
            }
            while (isNoteDisallowed(midiNumber)) {
                midiNumber++;

            }
        }
        return new CsoundMidiNote(glockenspiel, noteInSequence, 1, 100, midiNumber);
    }

    /**
     * Check if the Midi note is in the allowed subset {@code selectedNotes} of notes or their octaves.
     *
     * @param midiNumber the Midi number of the note to check
     * @return if this note is in the allowd subset or not
     */
    private boolean isNoteDisallowed(final int midiNumber) {
        int key = BASE_NOTE % 12;
        for (final int i : selectedNotes.getValue()) {
            if ((midiNumber - key - i) % 12 == 0) {
                return false;
            }
        }
        return true;
    }

    /**
     * @return the number how many notes should be played at same time
     */
    private int getSametimeNotesNumber() {
        return ThreadLocalRandom.current().nextInt(MIN_SAMETIME_NOTES, MAX_SAMETIME_NOTES + 1);
    }
}

