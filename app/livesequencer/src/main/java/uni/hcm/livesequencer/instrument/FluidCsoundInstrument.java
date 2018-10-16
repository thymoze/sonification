package uni.hcm.livesequencer.instrument;

import java.io.File;

/**
 * This class models an instrument being played with Fluid Synth Engine. This means you can play
 * instruments according to a sound font (e.g. a piano or strings voice).
 */
public class FluidCsoundInstrument extends CsoundInstrument {

    private File soundFont;
    private int bankNumber;
    private int presetNumber;

    /**
     * Creates a model of a Fluid Synth instrument using Csound. The voice of this instrument is
     * determined by the {@code bankNumber} and {@code presetNumber}.
     *
     * @param soundFont    the sound font file this instrument should be played with in SF2 format
     * @param bankNumber   the bank number of the sound font, that defines the instrument
     * @param presetNumber the preset number, that defines the instrument
     */
    public FluidCsoundInstrument(final File soundFont, final int bankNumber, final int presetNumber) {
        super("", false);
        setSoundFont(soundFont);
        setBankNumber(bankNumber);
        setPresetNumber(presetNumber);
    }

    /**
     * @return the sound font file this instrument should be played with in SF2 format
     */
    public File getSoundFont() {
        return soundFont;
    }

    /**
     * @param soundFont the sound font file this instrument should be played with in SF2 format
     */
    public void setSoundFont(final File soundFont) {
        this.soundFont = soundFont;
    }

    /**
     * @return the bank number of the sound font, that defines the instrument
     */
    public int getBankNumber() {
        return bankNumber;
    }

    /**
     * @param bankNumber the bank number of the sound font, that defines the instrument
     */
    public void setBankNumber(final int bankNumber) {
        this.bankNumber = bankNumber;
    }

    /**
     * @return the preset number, that defines the instrument
     */
    public int getPresetNumber() {
        return presetNumber;
    }

    /**
     * @param presetNumber the preset number, that defines the instrument
     */
    public void setPresetNumber(final int presetNumber) {
        this.presetNumber = presetNumber;
    }


}
