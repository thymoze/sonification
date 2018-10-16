package uni.hcm.livesequencer;

import org.junit.Test;

import uni.hcm.livesequencer.helper.CsoundOSCHelper;

import static org.junit.Assert.assertEquals;

public class OscUnitTest {
    @Test
    public void increase_handle_counter() {
        String initialActualString = new CsoundOSCHelper(9999).getHandle();
        assertEquals("gihandle0", initialActualString);
        String actualString = new CsoundOSCHelper(8383).getHandle();
        assertEquals("gihandle1", actualString);
    }
}
