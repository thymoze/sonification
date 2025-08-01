/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 3.0.12
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package csnd6;

public class CsoundMutex {
  private transient long swigCPtr;
  protected transient boolean swigCMemOwn;

  protected CsoundMutex(long cPtr, boolean cMemoryOwn) {
    swigCMemOwn = cMemoryOwn;
    swigCPtr = cPtr;
  }

  protected static long getCPtr(CsoundMutex obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  protected void finalize() {
    delete();
  }

  public synchronized void delete() {
    if (swigCPtr != 0) {
      if (swigCMemOwn) {
        swigCMemOwn = false;
        csndJNI.delete_CsoundMutex(swigCPtr);
      }
      swigCPtr = 0;
    }
  }

  public void Lock() {
    csndJNI.CsoundMutex_Lock(swigCPtr, this);
  }

  public int TryLock() {
    return csndJNI.CsoundMutex_TryLock(swigCPtr, this);
  }

  public void Unlock() {
    csndJNI.CsoundMutex_Unlock(swigCPtr, this);
  }

  public CsoundMutex() {
    this(csndJNI.new_CsoundMutex__SWIG_0(), true);
  }

  public CsoundMutex(int isRecursive) {
    this(csndJNI.new_CsoundMutex__SWIG_1(isRecursive), true);
  }

}
