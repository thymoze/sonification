package uni.hcm.livesequencer.sequencer;

import android.util.Log;

import com.csounds.CsoundObj;

import java.io.File;
import java.util.List;
import java.util.Set;

import uni.hcm.livesequencer.composable.CsoundComposable;
import uni.hcm.livesequencer.helper.CsoundOrchestraManager;
import uni.hcm.livesequencer.instrument.CsoundInstrument;
import uni.hcm.livesequencer.note.CsoundFunctionTableNote;

/**
 * This class models a {@link Sequencer} which uses the {@link CsoundObj} API.
 */
public class CsoundSequencer extends Sequencer {

    private final CsoundOrchestraManager orchestraManager = new CsoundOrchestraManager();
    private CsoundObj csoundObj;

    /**
     * Instantiates a new Csound sequencer, which plays sequences with length {@code sequenceLengthSeconds}. It uses the {@link CsoundObj} API to produce sound.
     *
     * @param sequenceLengthSeconds the length of one sequence in seconds
     * @param opcodeDir             the absolute path to the Csound opcode library directory
     */
    public CsoundSequencer(final double sequenceLengthSeconds, final String opcodeDir) {
        super(sequenceLengthSeconds);
        createCsoundObj(opcodeDir);
        csoundObj.SetOption("-odac");
        csoundObj.SetOption("-B2048");
        csoundObj.SetOption("-d");
        csoundObj.SetOption("-b512");
        csoundObj.SetOption("-t " + INITIAL_TEMPO_BPM);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void startSequencer() {
        for (final CsoundComposable csoundComposable : getCsoundComposables()) {
            orchestraManager.appendToHeader(csoundComposable.initializeOrchestra());
            addInstruments(csoundComposable.getInstrumentSet());
        }

        // an empty filename causes Csound to run infinitely
        csoundObj.startCsound(new File(""));
        super.startSequencer();
    }

    public CsoundObj getCsoundObj() {
        return csoundObj;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void stop() {
        super.stop();
        csoundObj.stop();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void playNote(final String payload) {
        Log.d(Sequencer.APP_NAME, "Play note: " + payload);
        csoundObj.sendScore(payload);
    }

    /**
     * Add instruments to {@link CsoundOrchestraManager} that can be used with this sequencer.
     *
     * @param instrumentSet the instrument set with all instruments that should be usable with this sequencer
     */
    public void addInstruments(final Set<CsoundInstrument> instrumentSet) {
        orchestraManager.addInstruments(instrumentSet);
        for (CsoundInstrument instrument : instrumentSet) {
            final List<CsoundFunctionTableNote> csoundFunctionTableNotes = instrument.getFunctionTableNotes();
            if (!csoundFunctionTableNotes.isEmpty()) {
                for (CsoundFunctionTableNote functionTableNote : csoundFunctionTableNotes) {
                    String payload = functionTableNote.generatePayload(0, getSequenceLengthSeconds());
                    csoundObj.sendScore(payload);
                    Log.d(Sequencer.APP_NAME, "Play f table note: " + payload);
                }
            }
        }
        csoundObj.compileOrc(orchestraManager.toString());
    }

    /**
     * Creates a {@link CsoundObj} to produce sound. It is able to use all Csound opcode libraries from {@code opcodeDir}.
     *
     * @param opcodeDir the absolute path to the Csound opcode library directory
     */
    private void createCsoundObj(final String opcodeDir) {
        if (csoundObj == null) {
            // This must be set before the Csound object is created
            csnd6.csndJNI.csoundSetGlobalEnv("OPCODE6DIR", opcodeDir);

            csoundObj = new CsoundObj();
        }
    }
}
