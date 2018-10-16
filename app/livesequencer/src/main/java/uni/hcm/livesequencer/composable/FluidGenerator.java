package uni.hcm.livesequencer.composable;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import uni.hcm.livesequencer.Sequence;
import uni.hcm.livesequencer.instrument.CsoundInstrument;
import uni.hcm.livesequencer.instrument.FluidCsoundInstrument;
import uni.hcm.livesequencer.note.CsoundMidiNote;
import uni.hcm.livesequencer.note.Note;

/**
 * This class provides an abstract base class for music generators using {@link FluidCsoundInstrument FluidSynthInstruments}.
 */
public abstract class FluidGenerator implements CsoundComposable {

    private final static int AMPLITUDE = 3;
    private int fluidEnginesCount = 0;
    private CsoundInstrument fluidSynth;

    /**
     * {@inheritDoc}
     */
    @Override
    public final String initializeOrchestra() {
        final Set<FluidCsoundInstrument> fluidCsoundInstruments = new HashSet<>();

        for (final CsoundInstrument instrument : getFluidInstrumentSet()) {
            if (instrument instanceof FluidCsoundInstrument) {
                fluidCsoundInstruments.add((FluidCsoundInstrument) instrument);
            }
        }

        final StringBuilder fluidLoads = new StringBuilder();

        int fluidEngineCounter = 0;
        int fluidChannelCounter = 0;
        while (!fluidCsoundInstruments.isEmpty()) {
            File firstSoundFont = fluidCsoundInstruments.iterator().next().getSoundFont();

            fluidLoads.append("gienginenum")
                    .append(fluidEngineCounter).append("\tfluidEngine\n")
                    .append("\n")
                    .append("isfnum").append(fluidEngineCounter).append("\tfluidLoad \"")
                    .append(firstSoundFont.getAbsolutePath())
                    .append("\", gienginenum")
                    .append(fluidEngineCounter).append(", 1\n");

            final Iterator<FluidCsoundInstrument> iterator = fluidCsoundInstruments.iterator();

            while (iterator.hasNext()) {
                FluidCsoundInstrument instrument = iterator.next();
                if (instrument.getSoundFont().equals(firstSoundFont)) {
                    final int bankNumber = instrument.getBankNumber();
                    final int presetNumber = instrument.getPresetNumber();
                    fluidLoads.append("\t\tfluidProgramSelect\tgienginenum")
                            .append(fluidEngineCounter).append(", ")
                            .append(fluidChannelCounter)
                            .append(", isfnum").append(fluidEngineCounter).append(", ").append(bankNumber)
                            .append(", ").append(presetNumber).append("\n");
                    setInstrumentString(instrument, fluidChannelCounter, fluidEngineCounter);
                    iterator.remove();
                    fluidChannelCounter++;
                }
            }
            fluidEngineCounter++;
        }
        fluidEnginesCount = fluidEngineCounter;

        fluidLoads.append("\n").append(initializeFluidOrchestra());
        return fluidLoads.toString();
    }

    /**
     * The orchestra is being initialized by this string. It can contain any additional Csound orchestra string
     * that should be run before the instruments are listed in Csound orchestra.
     *
     * @return the string to be additionally placed in Csound orchestra header
     */
    @SuppressWarnings({"SameReturnValue"})
    public String initializeFluidOrchestra() {
        return "";
    }

    /**
     * Set the instrument string (body) for {@code instrument}.
     *
     * @param instrument         the {@link FluidCsoundInstrument} this body should be set
     * @param fluidChannelNumber the number of Fluid Channel this instrument should be assigned to
     * @param fluidEngineNumber  the number of Fluid Engine this instrument should be assigned to
     */
    private void setInstrumentString(final FluidCsoundInstrument instrument, final int fluidChannelNumber, final int fluidEngineNumber) {
        instrument.setBody("ichannel   = " + fluidChannelNumber + "\n" +
                "  ikey       = p5\n" +
                "  ivelocity  = p4 * 127 \n" +
                "\tfluidNote gienginenum" + fluidEngineNumber + ", ichannel, ikey, ivelocity\n");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final Set<CsoundInstrument> getInstrumentSet() {
        if (fluidSynth == null) {
            final StringBuilder fluidOutput = new StringBuilder();
            for (int fluidEngine = 0; fluidEngine < fluidEnginesCount; fluidEngine++) {
                fluidOutput.append("aleft").append(fluidEngine).append(", aright").append(fluidEngine).append(" fluidOut   gienginenum").append(fluidEngine).append("\n");
            }
            fluidOutput.append("                outs       ");
            fluidOutput.append(fluidOutputLine("left")).append(",  \\\n                           ");
            fluidOutput.append(fluidOutputLine("right")).append("\n");

            fluidSynth = new CsoundInstrument(fluidOutput.toString());
        }

        final Set<CsoundInstrument> returnInstrumentSet = new HashSet<>(getFluidInstrumentSet());
        returnInstrumentSet.add(fluidSynth);
        return returnInstrumentSet;
    }

    /**
     * {@inheritDoc}
     */
    public abstract Set<CsoundInstrument> getFluidInstrumentSet();

    /**
     * Get one line of Fluid Output instrument. This instrument is used to output all instruments of one Fluid Engine.
     *
     * @param side the side of output for this Fluid Output line (left or right)
     * @return one line of Fluid Output instrument string.
     */
    private String fluidOutputLine(final String side) {
        final StringBuilder fluidOutputLine = new StringBuilder();
        for (int fluidEngine = 0; fluidEngine < fluidEnginesCount; fluidEngine++) {
            if (fluidEngine != 0) {
                fluidOutputLine.append(" + ");
            }
            fluidOutputLine.append("(a").append(side).append(fluidEngine).append(" * ").append(AMPLITUDE).append(")");
        }
        return fluidOutputLine.toString();
    }

    /**
     * Play fluid output sequence. The Fluid Output instrument is used to output all instruments of one Fluid Engine.
     *
     * @param lengthSeconds the length in seconds the Fluid Output instrument should be played
     * @return the sequence of this Fluid Output instrument, consisting in one note
     */
    private Sequence playFluidOutput(final double lengthSeconds) {
        final CsoundMidiNote note = new CsoundMidiNote(fluidSynth, 0, lengthSeconds, 127, 69);
        final ArrayList<Note> dummyNoteList = new ArrayList<>();
        dummyNoteList.add(note);
        return new Sequence(dummyNoteList);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final List<Sequence> composeSequence(final double lengthSeconds) {
        final List<Sequence> fluidSequences = composeFluidSequence(lengthSeconds);
        fluidSequences.add(playFluidOutput(lengthSeconds));
        return fluidSequences;
    }

    /**
     * Compose sequence for a set of {@link CsoundInstrument CsoundInstruments}, including {@link FluidCsoundInstrument FluidSynthInstruments}, with length {@code lengthSeconds}.
     * All instruments being used in this sequences should be added with {@link #getFluidInstrumentSet} function.
     *
     * @param lengthSeconds the desired length of the composed sequence in seconds
     * @return the set of newly composed sequences.
     */
    public abstract List<Sequence> composeFluidSequence(final double lengthSeconds);


}
