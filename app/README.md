# Android Live Music Generation Framework
This framework simplifies the creation of live music generators for Android.

## Basic use of API
### CsoundComposable
This is an interface to implement in order to create your live music generator. It contains the following three methods:

    List<Sequence> composeSequence(final double lengthSeconds)
The first method `composeSequence(double)` should return a list of composed sequences with length `lengthSeconds`. All sequences you add to the returned list will be played in parallel.

     Set<CsoundInstrument> getInstrumentSet()
The second method `getInstrumentSet()` should return a set containing all instances of `CsoundInstrument` you use in your composed sequences. They can be created like described in [this Csound Instrument Design Tutorial](http://www.csounds.com/toots/). The body of a `CsoundInstrument` is the Csound Instrument String without the `instr <no>` and `endin`.

    String initializeOrchestra()
The last method `initializeOrchestra()` can return any initialization needed for the instruments in shape of a Csound Instrument String.

### FluidGenerator
In order to create a live music generator with instruments using a Sound Font, you should **extend** the class `FluidGenerator`. This abstract superclass adds functionality to your class and is therefore no interface. However it implements the interface `CsoundComposable`, thus it can be added the same way to the sequencer.

Everything works equal to `CsoundComposable`, except that the function names are `initializeFluidOrchestra()`, `composeFluidSequence(double)` and `getFluidInstrumentSet()`. You can add instances of `FluidCsoundInstrument` and the common `CsoundInstrument` to your instrument set.

### Set up the sequencer
Now that you created your `CsoundComposable` you can setup and start the sequencer.

    String opcodeDir = getBaseContext().getApplicationInfo().nativeLibraryDir;
    CsoundSequencer sequencer = new CsoundSequencer(lengthSeconds, opcodeDir);
    sequencer.addMusicGenerator(csoundComposable);
    sequencer.startSequencer();
The variable `lengthSeconds` contains the desired length of the sequences. The `csoundComposable` contains an instance of your `CsoundComposable` or `FluidGenerator`.

## Midi Read In
After creating a new `MidiReadIn midi` in the constructor of your `CsoundComposable`, you can simply call `midi.getInstruments()` to get all needed instruments in order to play your Midi file. Another call of `midi.readInSequence(lengthSeconds)` reads in all notes of your Midi to a `Sequence`. Please note while the produced `Sequence` contains all notes until `lengthSeconds`, they currently only get played if they fit entirely in the `Sequence`.

## OSC Listener
In order to simplify managing OSC connections, you can use the `CsoundOSCHelper`. You will usually instantiate it in the constructor of your `CsoundComposable`. The function `initialize()` returns the required Csound Orchestra String you should append to the return string of your `initializeOrchestra()`. The handle you get with `getHandle()` should be used in your Csound Instrument Strings. You can find further information about how to produce an OSC listening capable instrument in the [Csound Documentation](http://www.csounds.com/manual/html/OSClisten.html).

## Deal with files
You can add the needed Sound Fonts or Midi files as [Android Raw Resources](https://developer.android.com/guide/topics/resources/providing-resources.html). The here used class `FileUtils` is part of the [Apache Commons IO](https://commons.apache.org/proper/commons-io/).

    protected File getRawResourceFile(final int id) throws IOException {
        final InputStream inputStream = getResources().openRawResource(id);
        final File returnFile = File.createTempFile("res-load", "");
        FileUtils.copyInputStreamToFile(inputStream, returnFile);
        return returnFile;
    }
Alternatively you can add needed files storead at your `sdcard` directory, but you have to make sure you request and grant the proper [App Permissions](https://developer.android.com/guide/topics/permissions/index.html) to read from this directory.

