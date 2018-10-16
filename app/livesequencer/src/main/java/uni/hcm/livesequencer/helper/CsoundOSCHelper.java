package uni.hcm.livesequencer.helper;

import android.util.Log;

import uni.hcm.livesequencer.instrument.CsoundInstrument;
import uni.hcm.livesequencer.sequencer.Sequencer;

/**
 * This class is a helper class to simplify the creation of OSC listeners
 */
public class CsoundOSCHelper {

    private static int counter = 0;
    private final int portNumber;
    private final int id;

    /**
     * Instantiates a new Csound OSC listener, which listens on port {@code portNumber}.
     *
     * @param portNumber the port number this OSC listener should listen to.
     */
    public CsoundOSCHelper(final int portNumber) {
        if (portNumber < 0 || portNumber > 65535) {
            final String errorMessage = "The supplied port number is out of range. It must not be negative and not higher than 65535.";
            Log.w(Sequencer.APP_NAME, errorMessage);
            throw new IllegalArgumentException(errorMessage);
        }
        this.portNumber = portNumber;
        id = counter++;
    }

    /**
     * Return initialization string to create a OSC listener.
     *
     * @return initialization string to be placed in the header section of a Csound orchestra string.
     */
    public String initialize() {
        return getHandle() + " OSCinit " + portNumber + "\n";
    }

    /**
     * Gets handle to use this OSC listener.
     *
     * @return the handle to use in a {@link CsoundInstrument}
     */
    public String getHandle() {
        return "gihandle" + id;
    }
}
