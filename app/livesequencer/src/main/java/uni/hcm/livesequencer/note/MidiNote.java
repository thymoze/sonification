package uni.hcm.livesequencer.note;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class models a note with entities being in Midi format.
 */
public abstract class MidiNote extends Note {

    /**
     * A constant array to translate note names into numbers and vice versa.
     */
    protected final static String[] noteString = new String[]{"C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"};
    private int midiNumber;
    private int velocity;

    /**
     * Creates a {@link MidiNote} with entities being in Midi format.
     *
     * @param offsetBeats the number of beats until this note is played
     * @param lengthBeats the number of beats this note is being played
     * @param velocity    the force which an instrument is being played with, not only loudness, but also sound can differ
     * @param midiNumber  the number of note in midi format
     */
    public MidiNote(final double offsetBeats, final double lengthBeats, final int velocity, final int midiNumber) {
        super(offsetBeats, lengthBeats);
        setMidiNumber(midiNumber);
        setVelocity(velocity);
    }

    /**
     * Creates a {@link MidiNote} with entities being in Midi format.
     *
     * @param offsetBeats the number of beats until this note is played
     * @param lengthBeats the number of beats this note is being played
     * @param velocity    the force which an instrument is being played with, not only loudness, but also sound can differ
     * @param noteName    a human readable note name, can only consist of one note name letter, optionally one # to indicate an augmentation of the note, plus an octave number from -1 to 8
     */
    public MidiNote(final double offsetBeats, final double lengthBeats, final int velocity, String noteName) {
        this(offsetBeats, lengthBeats, velocity, getMidiNumberFromName(noteName));
    }

    /**
     * Convert a human readable note name string to the according Midi number.
     *
     * @param name human readable note name, can only consist of one note name letter, optionally one # to indicate an augmentation of the note, plus an octave number from -1 to 8
     * @return the converted Midi number according to the input {@code name} string
     * @throws IllegalArgumentException if a String gets input not fitting the described input format
     */
    public static int getMidiNumberFromName(final String name) throws IllegalArgumentException {
        Pattern p = Pattern.compile("[ABCDEFG#]+|-?\\d+");
        Matcher m = p.matcher(name);
        ArrayList<String> allMatches = new ArrayList<>();
        while (m.find()) {
            allMatches.add(m.group());
        }
        final String errorMessage = "This note name is not valid. You can only input a note name with one letter, optionally one # and an octave number from -1 to 8.";
        if (allMatches.size() != 2) {
            throw new IllegalArgumentException(errorMessage);
        }

        final int midiNumber = Arrays.asList(noteString).indexOf(allMatches.get(0));
        if (midiNumber == -1) {
            throw new IllegalArgumentException("Midi number invalid. " + errorMessage);
        } else {
            int octaveNumber;
            try {
                octaveNumber = Integer.parseInt(allMatches.get(1));
                if (octaveNumber < -1 || octaveNumber > 9) {
                    throw new IllegalArgumentException();
                }
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Octave number invalid. " + errorMessage);
            }

            int calculatedMidiNumber = (octaveNumber + 1) * 12 + midiNumber;
            if (calculatedMidiNumber < 0 || calculatedMidiNumber > 127) {
                throw new IllegalArgumentException("This note can not be represented by a Midi note.");
            }
            return calculatedMidiNumber;
        }
    }

    /**
     * Get name of the note as a human readable string.
     *
     * @return name of note
     */
    public String getName() {
        final int octave = (midiNumber / 12) - 1;
        final int noteIndex = (midiNumber % 12);
        final String note = noteString[noteIndex];
        return note + octave;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract String generatePayload(final double offsetSeconds, final double lengthSeconds);

    /**
     * The Midi number is a number that defines the pitch of a note. It is can have values from 0 to 127. An A4 note equals to 69 in Midi.
     *
     * @return the Midi number of this note
     */
    public int getMidiNumber() {
        return midiNumber;
    }

    /**
     * The Midi number is a number that defines the pitch of a note. It is can have values from 0 to 127. An A4 note equals to 69 in Midi.
     *
     * @param midiNumber the Midi number of this note.
     */
    public void setMidiNumber(final int midiNumber) {
        if (midiNumber >= 0 && midiNumber <= 127) {
            this.midiNumber = midiNumber;
        } else {
            throw new IllegalArgumentException("Midi number must be between 0 and 127.");
        }
    }

    /**
     * The velocity is defined as the force which an instrument is being played with, which can
     * affect not only loudness, but also sound can be different. In Midi, this should be a value between 0 and 127.
     *
     * @return the velocity this note is being played with
     */
    public int getVelocity() {
        return velocity;
    }

    /**
     * The velocity is defined as the force which an instrument is being played with, which can
     * affect not only loudness, but also sound can be different. In Midi, this should be a value between 0 and 127.
     *
     * @param velocity the velocity this note is being played with
     */
    public void setVelocity(final int velocity) {
        if (velocity >= 0 && velocity <= 127) {
            this.velocity = velocity;
        } else {
            throw new IllegalArgumentException("Midi velocity must be between 0 and 127.");
        }
    }
}
