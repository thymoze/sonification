package uni.hcm.livesequencer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import uni.hcm.livesequencer.events.TempoChangeEvent;
import uni.hcm.livesequencer.note.Note;

/**
 * Models a sequence of notes and contains a list of {@link TempoChangeEvent TempoChangeEvents}.
 */
public class Sequence {

    private final ArrayList<Note> noteSequence = new ArrayList<>();
    private final ArrayList<TempoChangeEvent> tempoChangeEvents = new ArrayList<>();

    /**
     * Creates a sequence of notes according to {@code noteSequence}.
     *
     * @param noteSequence a {@link List} of {@link Note Notes} of this sequence
     */
    public Sequence(final List<Note> noteSequence) {
        addNoteSequence(noteSequence);
    }

    /**
     * Creates a sequence of notes according to {@code noteSequence}. The {@code tempoChangeEvents} change the tempo of this sequence at their determined time.
     *
     * @param noteSequence      the {@link List} of {@link Note Notes} of this sequence
     * @param tempoChangeEvents the {@link Collection} of events which can change the tempo of this sequence
     */
    public Sequence(final List<Note> noteSequence, final Collection<TempoChangeEvent> tempoChangeEvents) {
        this(noteSequence);
        setTempoChangeEvents(tempoChangeEvents);
    }

    /**
     * @return the sequence of notes of this sequence
     */
    public ArrayList<Note> getSequence() {
        return noteSequence;
    }

    /**
     * Add the sequence of notes {@code noteSequence} to this sequence.
     *
     * @param noteSequence the added sequence of notes
     */
    public void addNoteSequence(final List<Note> noteSequence) {
        this.noteSequence.addAll(noteSequence);
    }

    /**
     * @return the tempo change events
     */
    public List<TempoChangeEvent> getTempoChangeEvents() {
        return tempoChangeEvents;
    }

    /**
     * @param tempoChangeEvents the {@link Collection} of events which can change the tempo of this sequence
     */
    public void setTempoChangeEvents(final Collection<TempoChangeEvent> tempoChangeEvents) {
        this.tempoChangeEvents.clear();
        this.tempoChangeEvents.addAll(tempoChangeEvents);
    }

    /**
     * Add an additional deferral time to every note in this sequence to wait until this note is played.
     *
     * @param additionalOffsetSeconds the additional time in seconds to wait until the notes are played
     */
    public void addAdditionalOffsetSeconds(final double additionalOffsetSeconds) {
        for (final Note note : noteSequence) {
            note.addAdditionalOffsetSeconds(additionalOffsetSeconds);
        }
    }
}
