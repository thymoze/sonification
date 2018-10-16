package uni.hcm.livesequencer.note;

import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import uni.hcm.livesequencer.sequencer.Sequencer;

/**
 * This class models a Csound f Statement (function table) note.
 *
 * @see <a href="http://csound.com/docs/manual/f.html">Csound Documentation f Statement</a>
 */
public class CsoundFunctionTableNote extends Note {

    private static int idCounter = 1;
    private final int id;
    private int tableSize;
    private int selectedGenRoutine;
    private List<Double> additionalArguments = new ArrayList<>();

    /**
     * Instantiates a new Csound f Statement Note.
     *
     * @param tableSize           the table size of function
     * @param selectedGenRoutine  the selected GEN routine to be called
     * @param additionalArguments the additional arguments determined by the particular GEN routine
     */
    public CsoundFunctionTableNote(final int tableSize, final int selectedGenRoutine, final Double... additionalArguments) {
        this(0, 0, tableSize, selectedGenRoutine, additionalArguments);
    }

    /**
     * Instantiates a new Csound f Statement Note.
     *
     * @param offsetBeats         the time in beats to wait until this note is played
     * @param lengthBeats         the length in beats of this note
     * @param tableSize           the table size of function
     * @param selectedGenRoutine  the selected GEN routine to be called
     * @param additionalArguments the additional arguments determined by the particular GEN routine
     */
    public CsoundFunctionTableNote(final double offsetBeats, final double lengthBeats, final int tableSize, final int selectedGenRoutine, final Double... additionalArguments) {
        super(offsetBeats, lengthBeats);
        setTableSize(tableSize);
        setSelectedGenRoutine(selectedGenRoutine);
        setAdditionalArguments(new ArrayList<>(Arrays.asList(additionalArguments)));
        id = idCounter++;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String generatePayload(final double offsetSeconds, final double lengthSeconds) {
        final StringBuilder payload = new StringBuilder();

        payload.append("f");
        payload.append(" ").append(getId());

        payload.append(" ").append(offsetSeconds);

        payload.append(" ").append(getTableSize());

        payload.append(" ").append(getSelectedGenRoutine());

        for (final Double argument : getAdditionalArguments()) {
            payload.append(" ").append(argument);
        }

        return payload.toString();
    }

    /**
     * @return the id of this f Statement
     */
    public int getId() {
        return id;
    }

    /**
     * @return the table size of function
     */
    public int getTableSize() {
        return tableSize;
    }

    /**
     * @param tableSize the table size of function
     */
    public void setTableSize(final int tableSize) {
        if (tableSize > 0 && !(isPowerOfTwo(tableSize) || isPowerOfTwo(tableSize - 1))) {
            final String errorMessage = "A positive table size must be a power of two or a power of two plus one.";
            Log.w(Sequencer.APP_NAME, errorMessage);
            throw new IllegalArgumentException();
        }
        this.tableSize = tableSize;
    }

    private boolean isPowerOfTwo(final int number) {
        return number > 0 && ((number & (number - 1)) == 0);
    }

    /**
     * @return the selected GEN routine
     */
    public int getSelectedGenRoutine() {
        return selectedGenRoutine;
    }

    /**
     * @param selectedGenRoutine the selected GEN routine
     */
    public void setSelectedGenRoutine(int selectedGenRoutine) {
        this.selectedGenRoutine = selectedGenRoutine;
    }

    /**
     * Gets additional arguments. This can be used in Csound for additional attributes for the CsoundInstrument.
     *
     * @return the additional arguments for Csound
     */
    public List<Double> getAdditionalArguments() {
        return additionalArguments;
    }

    /**
     * Sets additional arguments. This can be used in Csound for additional attributes for the CsoundInstrument.
     *
     * @param additionalArguments the additional arguments for Csound
     */
    public void setAdditionalArguments(final List<Double> additionalArguments) {
        this.additionalArguments = additionalArguments;
    }
}
