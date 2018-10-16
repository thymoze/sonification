package uni.hcm.livesequencer.helper;

import android.util.Log;

import java.util.HashSet;
import java.util.Set;

import uni.hcm.livesequencer.instrument.CsoundInstrument;
import uni.hcm.livesequencer.sequencer.Sequencer;

/**
 * This class models a Csound orchestra manager.
 * This class is used to produce a Csound orchestra string, which can be accessed via the {@link #toString()} method.
 */
public class CsoundOrchestraManager {

    private final static String STANDARD_ORCH_HEADER = "nchnls=2\n" +
            "0dbfs=1\n" +
            "ksmps=64\n" +
            "sr = 48000\n\n";
    private final StringBuilder header = new StringBuilder(STANDARD_ORCH_HEADER);
    private final HashSet<CsoundInstrument> instrumentSet = new HashSet<>();

    /**
     * Append to header of the Csound orchestra string. It can be used to initialize variables to be used with a {@link CsoundInstrument}.
     *
     * @param appendToHeader the append to header
     */
    public void appendToHeader(final String appendToHeader) {
        header.append(appendToHeader);
    }

    /**
     * Add instruments to the Csound orchestra string.
     *
     * @param instrumentSet the instrument set
     */
    public void addInstruments(final Set<CsoundInstrument> instrumentSet) {
        for (final CsoundInstrument instrument : instrumentSet) {
            String instrumentBody = instrument.getBody();
            if (instrumentBody.isEmpty()) {
                String errorMessage = "You tried to add an instrument with an empty body. If you wanted to add a FluidCsoundInstrument, you should extend FluidGenerator.";
                throw new IllegalArgumentException(errorMessage);
            }
            if (!instrumentBody.substring(instrumentBody.length() - 1).equals("\n")) {
                instrument.setBody(instrumentBody + "\n");
            }
        }
        this.instrumentSet.addAll(instrumentSet);
    }

    /**
     * @return the compound Csound orchestra style string.
     */
    @Override
    public String toString() {
        final StringBuilder orchestra = new StringBuilder();
        orchestra.append(header).append("\n");

        for (final CsoundInstrument instrument : instrumentSet) {
            orchestra.append("instr ").append(instrument.getId()).append("\n").append(instrument.getBody()).append("endin\n\n");
        }

        Log.d(Sequencer.APP_NAME, "Orchestra String:\n" + orchestra.toString());
        return orchestra.toString();
    }
}
