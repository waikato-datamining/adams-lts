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
 * BinnedNumericClassCrossValidationFoldGenerator.java
 * Copyright (C) 2012-2025 University of Waikato, Hamilton, New Zealand
 */
package weka.classifiers;

import adams.core.Utils;
import adams.data.binning.Bin;
import adams.data.binning.Binnable;
import adams.data.binning.BinnableInstances;
import adams.data.binning.algorithm.BinningAlgorithm;
import adams.data.binning.algorithm.BinningAlgorithmUser;
import adams.data.binning.algorithm.ManualBinning;
import adams.data.binning.operation.Bins;
import adams.data.binning.operation.Stratify;
import adams.data.binning.operation.Wrapping;
import adams.data.binning.postprocessing.MinBinSize;
import adams.data.splitgenerator.generic.core.Subset;
import adams.data.splitgenerator.generic.crossvalidation.CrossValidationGenerator;
import adams.data.splitgenerator.generic.crossvalidation.FoldPair;
import adams.data.splitgenerator.generic.randomization.DefaultRandomization;
import adams.flow.container.WekaTrainTestSetContainer;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.InstancesView;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Helper class for generating cross-validation folds.
 * <br><br>
 * The template for the relation name accepts the following placeholders:
 * @ = original relation name, $T = type (train/test), $N = current fold number
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 */
public class BinnedNumericClassCrossValidationFoldGenerator
  extends AbstractSplitGenerator
  implements CrossValidationFoldGenerator, BinningAlgorithmUser, PerFoldCrossValidationFoldGenerator {

  /** for serialization. */
  private static final long serialVersionUID = -8387205583429213079L;

  /** the number of folds. */
  protected int m_NumFolds;

  /** whether to stratify the data (in case of nominal class). */
  protected boolean m_Stratify;

  /** the current fold. */
  protected transient int m_CurrentFold;

  /** the template for the relation name. */
  protected String m_RelationName;

  /** whether to randomize the data. */
  protected boolean m_Randomize;

  /** the binning algorithm. */
  protected BinningAlgorithm m_Algorithm;

  /** the temporary pairs. */
  protected transient List<FoldPair<Binnable<Instance>>> m_FoldPairs;

  /**
   * Initializes the generator.
   */
  public BinnedNumericClassCrossValidationFoldGenerator() {
    super();
  }

  /**
   * Initializes the generator.
   *
   * @param data	the full dataset
   * @param algorithm 	the algorithm to use
   * @param numFolds	the number of folds, leave-one-out if less than 2
   * @param seed	the seed for randomization
   * @param stratify	whether to perform stratified CV
   */
  public BinnedNumericClassCrossValidationFoldGenerator(Instances data, BinningAlgorithm algorithm, int numFolds, long seed, boolean stratify) {
    this(data, algorithm, numFolds, seed, true, stratify, null);
  }

  /**
   * Initializes the generator.
   *
   * @param data	the full dataset
   * @param algorithm 	the algorithm to use
   * @param numFolds	the number of folds, leave-one-out if less than 2
   * @param seed	the seed value
   * @param randomize 	whether to randomize the data
   * @param stratify	whether to perform stratified CV
   * @param relName	the relation name template, use null to ignore
   */
  public BinnedNumericClassCrossValidationFoldGenerator(Instances data, BinningAlgorithm algorithm, int numFolds, long seed, boolean randomize, boolean stratify, String relName) {
    super();
    setData(data);
    setAlgorithm(algorithm);
    setSeed(seed);
    setNumFolds(numFolds);
    setRelationName(relName);
    setStratify(stratify);
    setRandomize(randomize);
  }

  /**
   * Returns a string describing the object.
   *
   * @return 			a description suitable for displaying in the gui
   */
  @Override
  public String globalInfo() {
    return "Generates cross-validation fold pairs. Uses binning algorithm to obtain similar class distributions.";
  }

  /**
   * Adds options to the internal list of options.
   */
  @Override
  public void defineOptions() {
    super.defineOptions();

    m_OptionManager.add(
      "num-folds", "numFolds",
      10, 2, null);

    m_OptionManager.add(
      "relation-name", "relationName",
      CrossValidationHelper.PLACEHOLDER_ORIGINAL);

    m_OptionManager.add(
      "randomize", "randomize",
      true);

    m_OptionManager.add(
      "stratify", "stratify",
      true);

    m_OptionManager.add(
      "algorithm", "algorithm",
      new ManualBinning());
  }

  /**
   * Resets the generator.
   */
  @Override
  protected void reset() {
    super.reset();

    m_CurrentFold = 1;
    m_FoldPairs   = null;
  }

  /**
   * Sets the original data.
   *
   * @param value	the data
   */
  @Override
  public void setData(Instances value) {
    super.setData(value);
    if (m_Data != null) {
      if (m_Data.classIndex() == -1)
        throw new IllegalArgumentException("No class attribute set!");
      if (!m_Data.classAttribute().isNumeric())
        throw new IllegalArgumentException("Class attribute is not numeric!");
    }
  }

  /**
   * Sets the number of folds to use.
   *
   * @param value	the number of folds, less than 2 for LOO
   */
  @Override
  public void setNumFolds(int value) {
    m_NumFolds = value;
    reset();
  }

  /**
   * Returns the number of folds.
   *
   * @return		the number of folds
   */
  @Override
  public int getNumFolds() {
    return m_NumFolds;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String numFoldsTipText() {
    return "The number of folds; use <2 for leave one out (LOO).";
  }

  /**
   * Returns the actual number of folds used (eg when using LOO).
   *
   * @return		the actual number of folds, -1 if not yet calculated
   * @see		#initializeIterator()
   */
  @Override
  public int getActualNumFolds() {
    return m_NumFolds;
  }

  /**
   * Sets whether to randomize the data.
   *
   * @param value	true if to randomize the data
   */
  @Override
  public void setRandomize(boolean value) {
    m_Randomize = value;
    reset();
  }

  /**
   * Returns whether to randomize the data.
   *
   * @return		true if to randomize the data
   */
  @Override
  public boolean getRandomize() {
    return m_Randomize;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String randomizeTipText() {
    return "If enabled, the data is randomized first.";
  }

  /**
   * Sets whether to stratify the data (nominal class).
   *
   * @param value	whether to stratify the data (nominal class)
   */
  @Override
  public void setStratify(boolean value) {
    m_Stratify = value;
    reset();
  }

  /**
   * Returns whether to stratify the data (in case of nominal class).
   *
   * @return		true if to stratify
   */
  @Override
  public boolean getStratify() {
    return m_Stratify;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String stratifyTipText() {
    return "If enabled, the folds get stratified in case of a nominal class attribute.";
  }

  /**
   * Sets the template for the relation name.
   *
   * @param value	the template
   */
  @Override
  public void setRelationName(String value) {
    m_RelationName = value;
    reset();
  }

  /**
   * Returns the relation name template.
   *
   * @return		the template
   */
  @Override
  public String getRelationName() {
    return m_RelationName;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String relationNameTipText() {
    return CrossValidationHelper.relationNameTemplateTipText();
  }

  /**
   * Sets the binning algorithm.
   *
   * @param value 	the algorithm
   */
  @Override
  public void setAlgorithm(BinningAlgorithm value) {
    m_Algorithm = value;
    reset();
  }

  /**
   * Returns the binning algorithm.
   *
   * @return 		the algorithm
   */
  @Override
  public BinningAlgorithm getAlgorithm() {
    return m_Algorithm;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  @Override
  public String algorithmTipText() {
    return "The binning algorithm to apply to the data.";
  }

  /**
   * Returns whether randomization is enabled.
   *
   * @return		true if to randomize
   */
  @Override
  protected boolean canRandomize() {
    return m_Randomize;
  }

  /**
   * Returns <tt>true</tt> if the iteration has more elements. (In other
   * words, returns <tt>true</tt> if <tt>next</tt> would return an element
   * rather than throwing an exception.)
   *
   * @return 		<tt>true</tt> if the iterator has more elements.
   */
  @Override
  protected boolean checkNext() {
    return (m_CurrentFold <= m_NumFolds);
  }

  /**
   * Initializes the iterator, randomizes the data if required.
   */
  @Override
  protected void doInitializeIterator() {
    if (m_Data == null)
      throw new IllegalStateException("No data provided!");

    if (m_Data.numInstances() < m_NumFolds)
      throw new IllegalArgumentException(
	  "Cannot have less data than folds: "
	      + "required=" + m_NumFolds + ", provided=" + m_Data.numInstances());

    if ((m_RelationName == null) || m_RelationName.isEmpty())
      m_RelationName = CrossValidationHelper.PLACEHOLDER_ORIGINAL;
  }

  /**
   * Returns the next element in the iteration.
   *
   * @return 				the next element in the iteration.
   * @throws NoSuchElementException 	iteration has no more elements.
   */
  @Override
  protected WekaTrainTestSetContainer createNext() {
    WekaTrainTestSetContainer		result;
    List<Binnable<Instance>> 		binnableInst;
    List<Bin<Instance>>			binInst;
    MinBinSize				minBinSize;
    FoldPair<Binnable<Instance>> 	foldPair;
    Instances 				train;
    Instances 				test;
    int[]				trainRows;
    int[]				testRows;
    int					i;
    DefaultRandomization 		rand;
    List<Binnable<Instance>> 		trainBinnable;
    List<Binnable<Instance>> 		testBinnable;
    TIntList				trainIndices;
    TIntList				testIndices;
    Subset<Binnable<Instance>>		trainSub;
    Subset<Binnable<Instance>>		testSub;
    FoldPair<Binnable<Instance>> 	pair;

    if (m_CurrentFold > m_NumFolds)
      throw new NoSuchElementException("No more folds available!");

    // generate pairs
    if (m_FoldPairs == null) {
      try {
	binnableInst = BinnableInstances.toBinnableUsingClass(m_Data);
      }
      catch (Exception e) {
	throw new IllegalStateException("Failed to create binnable Instances!", e);
      }

      Wrapping.addTmpIndex(binnableInst);

      rand = new DefaultRandomization();
      rand.setSeed(m_Seed);
      if (canRandomize())
        binnableInst = rand.randomize(binnableInst);

      binInst = m_Algorithm.generateBins(binnableInst);
      if (isLoggingEnabled())
        getLogger().info("Bins: " + Utils.arrayToString(Bins.binSizes(binInst)));

      minBinSize = new MinBinSize();
      minBinSize.setMinSize(2);
      binInst = minBinSize.postProcessBins(binInst);
      if (isLoggingEnabled())
        getLogger().info("Bins after post-processing: " + Utils.arrayToString(Bins.binSizes(binInst)));

      // use bin index as new value of binnable
      binInst = Bins.useBinIndex(binInst);
      binnableInst = Bins.flatten(binInst);

      if (getStratify())
	binnableInst = Stratify.stratify(binnableInst, m_NumFolds);

      m_FoldPairs = new ArrayList<>();

      // generate pairs
      for (i = 0; i < m_NumFolds; i++) {
	// train
	trainBinnable = CrossValidationGenerator.trainCV(binnableInst, m_NumFolds, i, rand);
	trainIndices  = Wrapping.getTmpIndices(trainBinnable);
	trainSub      = new Subset<>(trainBinnable, trainIndices);

	// test
	testBinnable = CrossValidationGenerator.testCV(binnableInst, m_NumFolds, i);
	testIndices  = Wrapping.getTmpIndices(testBinnable);
	testSub      = new Subset<>(testBinnable, testIndices);

	m_FoldPairs.add(new FoldPair<>(i, trainSub, testSub));
      }

      m_OriginalIndices        = new TIntArrayList();
      m_OriginalIndicesPerFold = new int[m_FoldPairs.size()][];
      for (i = 0; i < m_FoldPairs.size(); i++) {
	pair = m_FoldPairs.get(i);
	m_OriginalIndicesPerFold[i] = pair.getTest().getOriginalIndices().toArray();
	m_OriginalIndices.addAll(m_OriginalIndicesPerFold[i]);
      }
    }

    foldPair = m_FoldPairs.get(m_CurrentFold - 1);

    trainRows = foldPair.getTrain().getOriginalIndices().toArray();
    testRows  = foldPair.getTest().getOriginalIndices().toArray();

    // generate fold pair
    if (m_UseViews) {
      train = new InstancesView(m_Data, trainRows);
      test = new InstancesView(m_Data, testRows);
    }
    else {
      train = BinnableInstances.toInstances(foldPair.getTrain().getData());
      test  = BinnableInstances.toInstances(foldPair.getTest().getData());
    }

    // rename datasets
    train.setRelationName(CrossValidationHelper.createRelationName(m_Data.relationName(), m_RelationName, m_CurrentFold, true));
    test.setRelationName(CrossValidationHelper.createRelationName(m_Data.relationName(), m_RelationName, m_CurrentFold, false));

    result = new WekaTrainTestSetContainer(
      train, test, m_Seed, m_CurrentFold, m_NumFolds, trainRows, testRows);
    m_CurrentFold++;

    if (m_CurrentFold > m_NumFolds)
      m_FoldPairs = null;

    return result;
  }

  /**
   * Returns the cross-validation indices.
   *
   * @return		the indices
   */
  @Override
  public int[] crossValidationIndices() {
    return m_OriginalIndices.toArray();
  }

  /**
   * Returns the cross-validation indices per fold.
   *
   * @return		the indices
   */
  @Override
  public int[][] crossValidationIndicesPerFold() {
    return m_OriginalIndicesPerFold;
  }

  /**
   * Returns a short description of the generator.
   *
   * @return		a short description
   */
  @Override
  public String toString() {
    return super.toString() + ", numFolds=" + m_NumFolds + ", stratify=" + m_Stratify + ", relName=" + m_RelationName;
  }
}
