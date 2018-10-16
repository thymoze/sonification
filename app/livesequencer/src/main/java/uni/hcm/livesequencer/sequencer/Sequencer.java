package uni.hcm.livesequencer.sequencer;

import android.util.Log;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import uni.hcm.livesequencer.Sequence;
import uni.hcm.livesequencer.composable.CsoundComposable;
import uni.hcm.livesequencer.events.TempoChangeEvent;
import uni.hcm.livesequencer.helper.BeatsToSecondsConverter;
import uni.hcm.livesequencer.note.Note;

/**
 * A sequencer to play {@link CsoundComposable CsoundComposables}. The sequencer uses the {@link CsoundComposable CsoundComposables} to generate
 * two sequences at first, then playing every sequence deferred from calculation. This allows interruption free playback if sequence length is set high enough.
 *
 * @see Sequencer#playSequence()
 */
public abstract class Sequencer {

    /**
     * The app name.
     */
    public static final String APP_NAME = "LiveSequencer";

    /**
     * This constant defines the initial tempo of this sequencer, if not overridden by a Sequence.
     */
    protected static final double INITIAL_TEMPO_BPM = 60;
    private static final int NUMBER_OF_CORES = Runtime.getRuntime().availableProcessors();

    private final List<Sequence> nextSequences = new ArrayList<>();
    private final Set<CsoundComposable> csoundComposables = new HashSet<>();
    private final double sequenceLengthSeconds;
    /**
     * The sequence timer future to enable the shutdown of the scheduled sequencing task.
     */
    private ScheduledFuture<?> sequenceTimerFuture;
    private boolean isFirstSequence = true;

    /**
     * Instantiates a new sequencer with a sequence length {@code sequenceLengthSeconds}.
     *
     * @param sequenceLengthSeconds the length of one sequence in seconds
     */
    Sequencer(final double sequenceLengthSeconds) {
        this.sequenceLengthSeconds = sequenceLengthSeconds;
    }

    /**
     * Add an additional offset in seconds to every {@link Note} in the provided {@code sequenceSet}.
     *
     * @param sequenceSet             the sequences to set the additional offset to
     * @param additionalOffsetSeconds the additional time in seconds to wait until this note is played
     * @return the new {@link Set} of sequences with the additional offset seconds set
     */
    private static List<Sequence> setAdditionalOffsetSeconds(final List<Sequence> sequenceSet, final double additionalOffsetSeconds) {
        for (final Sequence sequence : sequenceSet) {
            sequence.addAdditionalOffsetSeconds(additionalOffsetSeconds);
        }
        return sequenceSet;
    }

    /**
     * This function is called to play a note. For this purpose a payload string is being generated, that contains all entities needed to play this note.
     *
     * @param payload the payload of the note, this contains everything of this note needed to play
     */
    protected abstract void playNote(final String payload);

    /**
     * Stop sequencer and generation of new sequences.
     */
    public void stop() {
        sequenceTimerFuture.cancel(true);
    }

    /**
     * Start sequencer. This function should initialize everything needed for creation of new sequences.
     */
    public void startSequencer() {
        final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(NUMBER_OF_CORES);

        sequenceTimerFuture = executorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                playSequence();
            }
        }, 0, (long) (sequenceLengthSeconds * 1000d), TimeUnit.MILLISECONDS);
    }

    /**
     * Plays a set of {@link CsoundComposable CsoundComposables}. In order to ensure fluid real time playback, this adds at first two sequences.
     * The first to start immediately, the second one to start after the first one is finished. This allows the sequencer to play
     * the following sequences deferred. The sequence can be calculated while another one is played, without interruption of
     * playback. Every new sequence gets calculated and added to the next sequences set while another one is being played.
     */
    public void playSequence() {
        if (isFirstSequence) {
            for (final CsoundComposable csoundComposable : getCsoundComposables()) {
                nextSequences.addAll(csoundComposable.composeSequence(sequenceLengthSeconds));
                nextSequences.addAll(setAdditionalOffsetSeconds(csoundComposable.composeSequence(sequenceLengthSeconds), sequenceLengthSeconds));
            }
            isFirstSequence = false;
        }

        final TreeSet<TempoChangeEvent> tempoChangeEventTreeSet = computeTempoChangeEventTreeSet();

        for (final Sequence currentSequence : nextSequences) {
            for (Note currentNote : currentSequence.getSequence()) {
                BeatsToSecondsConverter converter = new BeatsToSecondsConverter(tempoChangeEventTreeSet, currentNote);
                playNote(currentNote.generatePayload(converter.calculateOffsetSeconds() + currentNote.getAdditionalOffsetSeconds(), converter.calculateLengthSeconds()));
            }
        }

        nextSequences.clear();

        for (final CsoundComposable csoundComposable : getCsoundComposables()) {
            nextSequences.addAll(setAdditionalOffsetSeconds(csoundComposable.composeSequence(sequenceLengthSeconds), sequenceLengthSeconds));
        }
    }

    /**
     * @return the {@link CsoundComposable CsoundComposables} used to produce sequences
     */
    protected Set<CsoundComposable> getCsoundComposables() {
        return csoundComposables;
    }

    /**
     * @param csoundComposable the {@link CsoundComposable CsoundComposables} used to produce sequences
     */
    public void addMusicGenerator(final CsoundComposable csoundComposable) {
        this.csoundComposables.add(csoundComposable);
    }

    /**
     * Compute a {@link TreeSet} of all {@link TempoChangeEvent TempoChangeEvents} in all sequences of this sequencer.
     *
     * @return the {@link TreeSet} of all {@link TempoChangeEvent TempoChangeEvents} in all sequences
     */
    private TreeSet<TempoChangeEvent> computeTempoChangeEventTreeSet() {
        TreeSet<TempoChangeEvent> tempoChangeEventTreeSet = new TreeSet<>();
        for (final Sequence currentSequence : nextSequences) {
            tempoChangeEventTreeSet.addAll(currentSequence.getTempoChangeEvents());
        }

        double minimum = Double.MAX_VALUE;
        for (final TempoChangeEvent tempoChangeEvent : tempoChangeEventTreeSet) {
            if (tempoChangeEvent.getOffsetSeconds() < minimum) {
                minimum = tempoChangeEvent.getOffsetSeconds();
            }
            if (tempoChangeEvent.getOffsetSeconds() == 0) {
                minimum = 0;
                break;
            }
        }

        if (minimum != 0) {
            tempoChangeEventTreeSet.add(new TempoChangeEvent(0, INITIAL_TEMPO_BPM));
            Log.d(Sequencer.APP_NAME, "Set initial tempo because you did not set one.");
        }

        return tempoChangeEventTreeSet;
    }

    /**
     * @return the length of one sequence in seconds
     */
    protected double getSequenceLengthSeconds() {
        return sequenceLengthSeconds;
    }
}
