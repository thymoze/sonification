package uni.hcm.livesequencer.helper;

/**
 * Enumeration of different musical scales.
 */
@SuppressWarnings("unused")
public enum ScaleReference {
    MINOR(new int[]{0, 2, 3, 5, 7, 8, 10}),
    MAJOR(new int[]{0, 2, 4, 5, 7, 9, 11}),
    CHROMATIC(new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11}),
    PENTATONIC(new int[]{0, 2, 4, 7, 9});

    private final int[] midiNoteNumbers;

    /**
     * Enumeration of different musical scales.
     *
     * @param midiNoteNumbers the midi note numbers of the scale. May be added to other midi note numbers in order to get different keys of a particular scale.
     */
    ScaleReference(final int[] midiNoteNumbers) {
        this.midiNoteNumbers = midiNoteNumbers;
    }

    /**
     * The returned {@code midiNoteNumbers} may be added to other midi note numbers in order to get different keys of a particular scale.
     *
     * @return midiNoteNumbers an array of midi note numbers of the scale.
     */
    public int[] getValue() {
        return midiNoteNumbers;
    }
}
