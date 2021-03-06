/*
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/*
 * CrossValidationHelper.java
 * Copyright (C) 2016-2019 University of Waikato, Hamilton, NZ
 */

package weka.classifiers;

import gnu.trove.list.array.TIntArrayList;
import weka.classifiers.evaluation.Prediction;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instances;

import java.util.ArrayList;
import java.util.Random;

/**
 * Helper class for cross-validation.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 * @version $Revision$
 */
public class CrossValidationHelper {

  /** the placeholder for the (original) relation name. */
  public final static String PLACEHOLDER_ORIGINAL = "@";

  /** the placeholder for "train" or "test" type. */
  public final static String PLACEHOLDER_TYPE = "$T";

  /** the placeholder for the current fold number. */
  public final static String PLACEHOLDER_CURRENTFOLD = "$N";

  /**
   * Returns the indices from the original dataset for tracing the predictions
   * back to the original dataset (stratified).
   *
   * @param data	the original data
   * @param folds	the number of folds
   * @param random	the random number generator
   * @return		the indices
   */
  public static int[] crossValidationIndices(Instances data, int folds, Random random) {
    return crossValidationIndices(data, folds, random, true);
  }

  /**
   * Returns the indices from the original dataset for tracing the predictions
   * back to the original dataset.
   *
   * @param data	the original data
   * @param folds	the number of folds
   * @param random	the random number generator
   * @param stratify	whether to stratify
   * @return		the indices
   */
  public static int[] crossValidationIndices(Instances data, int folds, Random random, boolean stratify) {
    TIntArrayList 		result;
    ArrayList<Attribute>	atts;
    Instances			dataWithIndices;
    int				i;
    double[]			values;
    DenseInstance		inst;
    int				n;
    Instances 			train;
    Instances 			test;

    result = new TIntArrayList();

    // build fake dataset with indices
    atts = new ArrayList<>();
    atts.add(new Attribute("Index"));
    if (data.classIndex() != -1)
      atts.add((Attribute) data.classAttribute().copy());
    dataWithIndices = new Instances("data with indices", atts, data.numInstances());
    if (data.classIndex() != -1)
      dataWithIndices.setClassIndex(1);
    for (i = 0; i < data.numInstances(); i++) {
      values    = new double[(data.classIndex() != -1) ? 2 : 1];
      values[0] = i;
      if (data.classIndex() != -1)
        values[1] = data.instance(i).classValue();
      inst = new DenseInstance(1.0, values);
      dataWithIndices.add(inst);
    }

    // simulate cross-validation and record indices
    dataWithIndices = new Instances(dataWithIndices);
    dataWithIndices.randomize(random);
    if (stratify && (dataWithIndices.classIndex() != -1) && dataWithIndices.classAttribute().isNominal()) {
      dataWithIndices.stratify(folds);
    }
    for (i = 0; i < folds; i++) {
      train = dataWithIndices.trainCV(folds, i, random);
      test  = dataWithIndices.testCV(folds, i);
      for (n = 0; n < test.numInstances(); n++)
	result.add((int) test.instance(n).value(0));
    }

    return result.toArray();
  }

  /**
   * Reorders the indices to align with the original data.
   *
   * @param original	the original indices
   * @return		the aligned data
   */
  public static int[] alignIndices(int[] original) {
    int[]	result;
    int		i;

    result = new int[original.length];
    for (i = 0; i < original.length; i++)
      result[original[i]] = i;

    return result;
  }

  /**
   * Reorders the predictions to align with the original data.
   *
   * @param predictions	the predictions to align
   * @param original	the original indices
   * @return		the aligned data
   */
  public static ArrayList<Prediction> alignPredictions(ArrayList<Prediction> predictions, int[] original) {
    ArrayList<Prediction>	result;
    int[]			aligned;
    int				i;

    result  = new ArrayList<>();
    aligned = alignIndices(original);
    for (i = 0; i < aligned.length; i++)
      result.add(predictions.get(aligned[i]));

    return result;
  }

  /**
   * Generates a relation name for the current fold.
   *
   * @param relation 	the original relation name
   * @param template 	the template for the relation name
   * @param fold 	the current fold
   * @param train	whether train or test set
   * @return		the relation name
   */
  public static String createRelationName(String relation, String template, int fold, boolean train) {
    StringBuilder	result;
    String		name;
    int			len;

    result = new StringBuilder();
    name   = template;

    while (name.length() > 0) {
      if (name.startsWith(PLACEHOLDER_ORIGINAL)) {
	len = 1;
	result.append(relation);
      }
      else if (name.startsWith(PLACEHOLDER_TYPE)) {
	len = 2;
	if (train)
	  result.append("train");
	else
	  result.append("test");
      }
      else if (name.startsWith(PLACEHOLDER_CURRENTFOLD)) {
	len = 2;
	result.append(Integer.toString(fold));
      }
      else {
	len = 1;
	result.append(name.charAt(0));
      }

      name = name.substring(len);
    }

    return result.toString();
  }

  /**
   * Returns the tiptext for the relation name template.
   *
   * @return		the tiptext
   * @see		#createRelationName(String, String, int, boolean)
   */
  public static String relationNameTemplateTipText() {
    return "The template for the relation name; available placeholders: "
      + CrossValidationHelper.PLACEHOLDER_ORIGINAL + " for original, "
      + CrossValidationHelper.PLACEHOLDER_TYPE + " for type (train/test), "
      + CrossValidationHelper.PLACEHOLDER_CURRENTFOLD + " for current fold";
  }
}
