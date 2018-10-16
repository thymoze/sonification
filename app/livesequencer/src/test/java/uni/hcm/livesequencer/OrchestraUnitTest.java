package uni.hcm.livesequencer;

import org.junit.Test;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import uni.hcm.livesequencer.helper.CsoundOrchestraManager;
import uni.hcm.livesequencer.instrument.CsoundInstrument;
import uni.hcm.livesequencer.instrument.FluidCsoundInstrument;


public class OrchestraUnitTest {
    @Test(expected = IllegalArgumentException.class)
    public void increase_handle_counter() {
        CsoundOrchestraManager orchestraManager = new CsoundOrchestraManager();
        File soundFontDummy = new File("");
        Set<CsoundInstrument> instrumentSet = new HashSet<>();
        instrumentSet.add(new FluidCsoundInstrument(soundFontDummy, 1, 1));
        orchestraManager.addInstruments(instrumentSet);
    }
}
