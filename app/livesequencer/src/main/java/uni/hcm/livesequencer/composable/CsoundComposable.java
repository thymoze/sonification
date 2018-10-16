package uni.hcm.livesequencer.composable;

import java.util.Set;

import uni.hcm.livesequencer.instrument.CsoundInstrument;

/**
 * Csound specific {@link Composable}. Uses {@link CsoundInstrument} to model an Instrument.
 */
public interface CsoundComposable extends Composable {
    /**
     * The orchestra is being initialized by this string. It can contain any Csound orchestra string
     * that should be run before the instruments are listed in Csound orchestra.
     *
     * @return the string to be placed in Csound orchestra header
     */
    String initializeOrchestra();

    /**
     * Return all instruments being used during {@link #composeSequence}. Uses {@link CsoundInstrument} to model instruments.
     *
     * @return the instrument set being used in compositions.
     */
    @Override
    Set<CsoundInstrument> getInstrumentSet();
}