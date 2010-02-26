package edlin.classification;

/***
 * This class implements one best structured Mira
 * @author Kuzman Ganchev and Georgi Georgiev
 * <A HREF="mailto:georgiev@ontotext.com>georgi.georgiev@ontotext.com</A>
 * <A HREF="mailto:ganchev@ontotext.com>kuzman.ganchev@ontotext.com</A>
 * Date: Thu Feb 26 12:27:56 EET 2009
 */

import java.util.ArrayList;

import edlin.types.Alphabet;
import edlin.types.ClassificationInstance;
import edlin.types.LinearClassifier;
import edlin.types.SparseVector;
import edlin.types.StaticUtils;
import gnu.trove.TIntDoubleHashMap;
import gnu.trove.TIntDoubleIterator;


public class Mira {

	boolean performAveraging;
	int numIterations;
	Alphabet xAlphabet;
	Alphabet yAlphabet;
	CompleteFeatureFunction fxy;
	Loss loss;

	public Mira(boolean performAveraging, int numIterations,
			Alphabet xAlphabet, Alphabet yAlphabet, CompleteFeatureFunction fxy, Loss loss) {
		this.performAveraging = performAveraging;
		this.numIterations = numIterations;
		this.xAlphabet = xAlphabet;
		this.yAlphabet = yAlphabet;
		this.fxy = fxy;
		this.loss = loss;
	}

	private double calculateDenom(SparseVector a, SparseVector b){

		double result = 0;

		TIntDoubleHashMap diff = new TIntDoubleHashMap();

		for (int i = 0; i < a.numEntries(); i++) {
			int ind = a.getIndexAt(i);
			double val = a.getValueAt(i);
			if (!diff.containsKey(ind)){
				diff.put(ind, 0);
			}
			diff.put(ind, diff.get(ind)+val);
		}

		for (int i = 0; i < b.numEntries(); i++) {
			int ind = b.getIndexAt(i);
			double val = b.getValueAt(i);
			if (!diff.containsKey(ind)){
				diff.put(ind, 0);
			}
			diff.put(ind, diff.get(ind)-val);
		}

		for (TIntDoubleIterator iterator = diff.iterator(); iterator.hasNext();) {
			iterator.advance();
			result += Math.pow(iterator.value(), 2);
		}

		return result;

	}


	public LinearClassifier batchTrain(ArrayList<ClassificationInstance> trainingData) {
		LinearClassifier w = new LinearClassifier(this.xAlphabet, this.yAlphabet, this.fxy);
		LinearClassifier theta = null;
		if (this.performAveraging) {
			theta = new LinearClassifier(this.xAlphabet, this.yAlphabet, this.fxy);
		}
		for (int iter = 0; iter < this.numIterations; iter++) {
			for (ClassificationInstance inst : trainingData) {
				int yhat = w.label(inst.x);
				//calculate loss
				double lloss = this.loss.calculate(inst.y, yhat);
				//calculate alpha

				double alpha = lloss + StaticUtils.dotProduct(this.fxy.apply(inst.x, yhat), w.w) -
				StaticUtils.dotProduct(this.fxy.apply(inst.x, inst.y), w.w);
				if (alpha <= 0) {
					continue;
				}
				alpha = alpha / this.calculateDenom(this.fxy.apply(inst.x, inst.y), this.fxy.apply(inst.x, yhat));

				// if y = yhat then this update will not change w.
				StaticUtils.plusEquals(w.w, this.fxy.apply(inst.x, inst.y), alpha);
				StaticUtils.plusEquals(w.w, this.fxy.apply(inst.x, yhat), -alpha);
				if (this.performAveraging) {
					StaticUtils.plusEquals(theta.w, w.w, 1);
				}
			}
		}
		if (this.performAveraging) {
			return theta;
		}
		return w;
	}

}
