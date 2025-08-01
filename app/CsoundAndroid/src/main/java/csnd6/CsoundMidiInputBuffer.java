/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 3.0.12
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package csnd6;

public class CsoundMidiInputBuffer {
  private transient long swigCPtr;
  protected transient boolean swigCMemOwn;

  protected CsoundMidiInputBuffer(long cPtr, boolean cMemoryOwn) {
    swigCMemOwn = cMemoryOwn;
    swigCPtr = cPtr;
  }

  protected static long getCPtr(CsoundMidiInputBuffer obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  protected void finalize() {
    delete();
  }

  public synchronized void delete() {
    if (swigCPtr != 0) {
      if (swigCMemOwn) {
        swigCMemOwn = false;
        csndJNI.delete_CsoundMidiInputBuffer(swigCPtr);
      }
      swigCPtr = 0;
    }
  }

  public CsoundMidiInputBuffer(SWIGTYPE_p_unsigned_char buf, int bufSize) {
    this(csndJNI.new_CsoundMidiInputBuffer(SWIGTYPE_p_unsigned_char.getCPtr(buf), bufSize), true);
  }

  public void SendMidiMessage(int msg) {
    csndJNI.CsoundMidiInputBuffer_SendMidiMessage__SWIG_0(swigCPtr, this, msg);
  }

  public void SendMidiMessage(int status, int channel, int data1, int data2) {
    csndJNI.CsoundMidiInputBuffer_SendMidiMessage__SWIG_1(swigCPtr, this, status, channel, data1, data2);
  }

  public void SendNoteOn(int channel, int key, int velocity) {
    csndJNI.CsoundMidiInputBuffer_SendNoteOn(swigCPtr, this, channel, key, velocity);
  }

  public void SendNoteOff(int channel, int key, int velocity) {
    csndJNI.CsoundMidiInputBuffer_SendNoteOff__SWIG_0(swigCPtr, this, channel, key, velocity);
  }

  public void SendNoteOff(int channel, int key) {
    csndJNI.CsoundMidiInputBuffer_SendNoteOff__SWIG_1(swigCPtr, this, channel, key);
  }

  public void SendPolyphonicPressure(int channel, int key, int value) {
    csndJNI.CsoundMidiInputBuffer_SendPolyphonicPressure(swigCPtr, this, channel, key, value);
  }

  public void SendControlChange(int channel, int ctl, int value) {
    csndJNI.CsoundMidiInputBuffer_SendControlChange(swigCPtr, this, channel, ctl, value);
  }

  public void SendProgramChange(int channel, int pgm) {
    csndJNI.CsoundMidiInputBuffer_SendProgramChange(swigCPtr, this, channel, pgm);
  }

  public void SendChannelPressure(int channel, int value) {
    csndJNI.CsoundMidiInputBuffer_SendChannelPressure(swigCPtr, this, channel, value);
  }

  public void SendPitchBend(int channel, int value) {
    csndJNI.CsoundMidiInputBuffer_SendPitchBend(swigCPtr, this, channel, value);
  }

}
