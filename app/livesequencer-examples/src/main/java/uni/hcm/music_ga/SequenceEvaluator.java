package uni.hcm.music_ga;

import android.util.Log;

import net.sf.jclec.IConfigure;
import net.sf.jclec.IFitness;
import net.sf.jclec.IIndividual;
import net.sf.jclec.base.AbstractEvaluator;
import net.sf.jclec.fitness.SimpleValueFitness;
import net.sf.jclec.fitness.ValueFitnessComparator;
import net.sf.jclec.intarray.IntArrayIndividual;

import org.apache.commons.configuration.Configuration;

import java.util.Arrays;
import java.util.Comparator;


/**
 * The type Sequence evaluator. It creates a new individual, waits for the end of a sequence and
 * uses the rating by the user as fitness function.
 */
public class SequenceEvaluator extends AbstractEvaluator implements IConfigure {
    /**
     * An object to allow wait for notify from
     */
    public static final Object obj = new Object();
    /**
     * The Current genotype.
     */
    public static int[] currentGenotype;
    /**
     * The constant rating.
     */
    public static double rating = -1d;
    private final Comparator<IFitness> comparator = new ValueFitnessComparator();

    /**
     * {@inheritDoc}
     */
    @Override
    public void configure(Configuration settings) {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void evaluate(IIndividual ind) {
        IntArrayIndividual intArrayIndividual = (IntArrayIndividual) ind;

        currentGenotype = intArrayIndividual.getGenotype();
        synchronized (obj) {
            try {
                obj.wait(); // wait until sequences has ended in order to receive feedback
            } catch (InterruptedException e) {
                Log.w("LiveSequencerExamples", "The waiting for the end of a sequence was interrupted.\n" + Arrays.toString(e.getStackTrace()));
            }
        }
        currentGenotype = null;
        ind.setFitness(new SimpleValueFitness(rating));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Comparator<IFitness> getComparator() {
        return comparator;
    }

}
