package uni.hcm.livesequencer.helper;


import android.util.Log;
import android.util.SparseArray;

import com.leff.midi.MidiFile;
import com.leff.midi.MidiTrack;
import com.leff.midi.event.MidiEvent;
import com.leff.midi.event.NoteOff;
import com.leff.midi.event.NoteOn;
import com.leff.midi.event.PitchBend;
import com.leff.midi.event.ProgramChange;
import com.leff.midi.event.meta.Tempo;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import java.util.TreeSet;

import uni.hcm.livesequencer.Sequence;
import uni.hcm.livesequencer.events.TempoChangeEvent;
import uni.hcm.livesequencer.instrument.FluidCsoundInstrument;
import uni.hcm.livesequencer.note.CsoundMidiNote;
import uni.hcm.livesequencer.note.Note;
import uni.hcm.livesequencer.sequencer.CsoundSequencer;
import uni.hcm.livesequencer.sequencer.Sequencer;

/**
 * This class reads in a Midi file to be played with {@link CsoundSequencer}. Furthermore you can
 * get all according {@link FluidCsoundInstrument FluidSynthInstruments} used in Midi file.
 * These {@link FluidCsoundInstrument FluidSynthInstruments} use Fluid Synth to play the voices from
 * a sound file in SF2 format.
 */
public class MidiReadIn {

    private final MidiFile midiFile;
    private final FluidCsoundInstrument fallbackInstrument;
    private final SparseArray<FluidCsoundInstrument> instrumentMap = new SparseArray<>();
    private final HashSet<FluidCsoundInstrument> instruments = new HashSet<>();

    /**
     * A {@code MidiReadIn} reads two things: First it reads all instruments that are used in a
     * defined Midi file {@code input}. In order to play these instruments, Fluid Synth Engine is used
     * to play voices from a sound file in SF2 format.
     * <p>
     * The second thing this class does is to read in the score of a Midi file into a Sequence to
     * be handled by a {@code FluidGenerator}.
     *
     * @param input     the input Midi file
     * @param soundFont the input sound font this Midi should be played with
     * @throws IOException if midi file cannot be found
     */
    public MidiReadIn(final File input, final File soundFont) throws IOException {
        this.fallbackInstrument = new FluidCsoundInstrument(soundFont, 0, 1);
        instruments.add(fallbackInstrument);

        try {
            midiFile = new MidiFile(input);
            loadAllInstrumentsInMidi(midiFile, soundFont);
        } catch (IOException e) {
            final String errorMessage = "Midi file could not be found.";
            Log.w(Sequencer.APP_NAME, errorMessage);
            throw new IOException(errorMessage, e);
        }
    }

    /**
     * Searches a {@link NoteOn} in the provided {@code searchList} with {@code midiNoteNumber}.
     *
     * @param searchList     the list to search in
     * @param midiNoteNumber the Midi note number that should be found.
     * @return the according {@link NoteOn} with {@code midiNoteNumber}, if not found, returns {@code null}
     */
    private static NoteOn searchNoteInNoteOnList(final List<? extends NoteOn> searchList, final int midiNoteNumber) {
        final Iterator<? extends NoteOn> iterator = searchList.iterator();
        while (iterator.hasNext()) {
            final NoteOn noteOn = iterator.next();
            if (noteOn.getNoteValue() == midiNoteNumber) {
                iterator.remove();
                return noteOn;
            }
        }
        return null;
    }

    /**
     * Calculate the seconds that are needed to play {@code ticks} Ticks.
     *
     * @param ticks      the ticks the amount of seconds should be calculated
     * @param bpm        the current speed in beats per minute
     * @param resolution the resolution of this Midi file in pulses per quarter note (ppq)
     * @return the amount of seconds that are needed to play {@code ticks} Ticks.
     */
    public static double ticksToSeconds(final long ticks, final double bpm, final int resolution) {
        final double tickSize = 60000d / (bpm * (double) resolution) / 1000d;
        return tickSize * (double) ticks;
    }

    /**
     * @return the instruments used in this Midi file
     */
    public Set<FluidCsoundInstrument> getInstruments() {
        return instruments;
    }

    /**
     * Load all instruments of Midi file {@code input} and sound font {@code soundFont}.
     *
     * @param input     the Midi files which instruments should be loaded
     * @param soundFont the sound font that should be used for instrumentation of {@code input}
     */
    private void loadAllInstrumentsInMidi(final MidiFile input, final File soundFont) {
        for (final MidiTrack midiTrack : input.getTracks()) {
            for (final MidiEvent midiEvent : midiTrack.getEvents()) {
                if (midiEvent instanceof ProgramChange) {
                    final ProgramChange pc = (ProgramChange) midiEvent;
                    if (instrumentMap.get(pc.getProgramNumber()) == null) {
                        final FluidCsoundInstrument newInstrument = new FluidCsoundInstrument(soundFont, 0, pc.getProgramNumber());
                        instrumentMap.put(pc.getProgramNumber(), newInstrument);
                        instruments.add(newInstrument);
                    }
                }
            }
        }
    }

    /**
     * Read in sequence from Midi file. Note that the arguments of this function determine only
     * the amount of time where new {@link MidiEvent MidiEvents} are being evaluated. This means
     * all previously played {@link NoteOn} events are not being considered, even if the {@link NoteOff}
     * is in the span defined by the function arguments.
     * Furthermore if a played {@link NoteOn} continues playing after {@code lengthSeconds}, it will
     * not be played because of a limitation by the fluidNote opcode.
     *
     * @param lengthSeconds the length of the sequence to read in seconds
     * @return the read in sequence of notes to be played by {@link FluidCsoundInstrument FluidSynthInstruments}
     */
    public Sequence readInSequence(final double lengthSeconds) {
        return readInSequence(lengthSeconds, 0);
    }

    /**
     * Read in sequence from Midi file. Note that the arguments of this function determine only
     * the amount of time where new {@link MidiEvent MidiEvents} are being evaluated. This means
     * all previously played {@link NoteOn} events are not being considered, even if the {@link NoteOff}
     * is in the span defined by the function arguments.
     * Furthermore if a played {@link NoteOn} continues playing after {@code lengthSeconds}, it will
     * not be played because of a limitation by the fluidNote opcode.
     *
     * @param lengthSeconds the length of the sequence to read in seconds
     * @param beginSecond   the begin second to start the read in process
     * @return the read in sequence of notes to be played by {@link FluidCsoundInstrument FluidSynthInstruments}
     */
    public Sequence readInSequence(final double lengthSeconds, final double beginSecond) {
        final List<Note> noteList = new ArrayList<>();
        final TreeSet<TempoChangeEvent> tempoChangeEventList = getTempoChangeEventTreeSet();

        for (final MidiTrack midiTrack : midiFile.getTracks()) {

            final ArrayList<NoteOn> noteOnList = new ArrayList<>();
            Tempo lastTempo = new Tempo();
            lastTempo.setBpm(120f);
            FluidCsoundInstrument lastInstrument = fallbackInstrument;

            for (final MidiEvent midiEvent : midiTrack.getEvents()) {
                if (midiEvent instanceof ProgramChange) {
                    final ProgramChange programChange = (ProgramChange) midiEvent;
                    programChange.getProgramNumber();
                }
                if (midiEvent instanceof NoteOn) {
                    final NoteOn noteOn = (NoteOn) midiEvent;
                    noteOnList.add(noteOn);
                }
                if (midiEvent instanceof NoteOff) {
                    final NoteOff noteOff = (NoteOff) midiEvent;

                    final NoteOn correspondingNoteOn = searchNoteInNoteOnList(noteOnList, noteOff.getNoteValue());

                    if (correspondingNoteOn != null) {
                        final long noteLength = noteOff.getTick() - correspondingNoteOn.getTick();
                        final double noteLengthSeconds = ticksToSeconds(noteLength, (int) 60d, midiFile.getResolution());
                        final long noteOn = correspondingNoteOn.getTick();
                        final double fnoteOn = ticksToSeconds(noteOn, (int) 60d, midiFile.getResolution());
                        final CsoundMidiNote csoundMidiNote = new CsoundMidiNote(lastInstrument, fnoteOn, noteLengthSeconds, correspondingNoteOn.getVelocity(), noteOff.getNoteValue());
                        noteList.add(csoundMidiNote);
                    }
                }


                if (midiEvent instanceof ProgramChange) {
                    final ProgramChange programChange = (ProgramChange) midiEvent;
                    lastInstrument = instrumentMap.get(programChange.getProgramNumber());
                }

                if (midiEvent instanceof PitchBend) {
                    Log.d(Sequencer.APP_NAME, "Unhandled Pitch Midi Event.");
                }
            }
        }

        final Iterator<Note> iterator = noteList.iterator();
        while (iterator.hasNext()) {
            Note addNote = iterator.next();
            BeatsToSecondsConverter c = new BeatsToSecondsConverter(tempoChangeEventList, addNote);
            if (c.calculateOffsetSeconds() > beginSecond + lengthSeconds || c.calculateOffsetSeconds() < beginSecond) {
                iterator.remove();
            }
        }


        final Sequence returnSequence = new Sequence(noteList);

        returnSequence.setTempoChangeEvents(getTempoChangeEventTreeSet());
        returnSequence.addAdditionalOffsetSeconds(-beginSecond); // shift back beginning to 0
        return returnSequence;
    }

    /**
     * This function transforms a Midi tempo list to a {@link List} of {@link TempoChangeEvent TempoChangeEvents}.
     * The first set tempo in Midi file gets set as the tempo from beginning since Midi does not
     * specify a standard speed.
     *
     * @param tempoList the {@link List} of Midi tempos that should be transformed
     * @return the transformed {@link List} of {@link TempoChangeEvent TempoChangeEvents}
     */
    private TreeSet<TempoChangeEvent> convertToTempoChangeEventList(final List<Tempo> tempoList) {
        Collections.sort(tempoList);
        final ListIterator<Tempo> iterator = tempoList.listIterator();

        final TreeSet<TempoChangeEvent> tempoChangeEventList = new TreeSet<>();

        double lastTempoSeconds = 0;
        if (!tempoList.isEmpty()) {
            tempoChangeEventList.add(new TempoChangeEvent(0d, (double) tempoList.get(0).getBpm()));
        }

        while (iterator.hasNext()) {
            final Tempo thisTempo = iterator.next();

            if (iterator.hasNext()) {
                Tempo nextTempo = tempoList.get(iterator.nextIndex());
                final long delta = nextTempo.getTick() - thisTempo.getTick();
                lastTempoSeconds += ticksToSeconds(delta, thisTempo.getBpm(), midiFile.getResolution());
                tempoChangeEventList.add(new TempoChangeEvent(lastTempoSeconds, nextTempo.getBpm()));
            }
        }
        return tempoChangeEventList;
    }

    /**
     * @return a list of all {@link MidiEvent MidiEvents} in {@code midiFile}, which are instances of {@link Tempo}.
     */
    private List<Tempo> getTempoList() {
        final List<Tempo> tempoList = new ArrayList<>();

        for (final MidiTrack midiTrack : midiFile.getTracks()) {
            for (final MidiEvent midiEvent : midiTrack.getEvents()) {
                if (midiEvent instanceof Tempo) {
                    tempoList.add((Tempo) midiEvent);
                }
            }
        }

        return tempoList;
    }

    /**
     * @return the to a {@link TreeSet} of {@link TempoChangeEvent TempoChangeEvents} converted of all {@link Tempo} events in {@code midiFile}
     */
    private TreeSet<TempoChangeEvent> getTempoChangeEventTreeSet() {
        return convertToTempoChangeEventList(getTempoList());
    }
}
