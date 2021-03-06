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
 * DarkLord.java
 * Copyright (C) 2009-2018 University of Waikato, Hamilton, New Zealand
 */

package adams.opt.genetic;

import adams.core.ObjectCopyHelper;
import adams.core.Properties;
import adams.core.option.OptionUtils;
import weka.classifiers.Classifier;
import weka.classifiers.meta.FilteredClassifier;
import weka.core.Instance;
import weka.core.Instances;
import weka.filters.unsupervised.attribute.Remove;

import java.util.Map;
import java.util.logging.Level;

/**
 <!-- globalinfo-start -->
 <!-- globalinfo-end -->
 *
 <!-- options-start -->
 <!-- options-end -->
 *
 * @author Dale (dale at cs dot waikato dot ac dot nz)
 * @author FracPete (fracpete at waikato dot ac dot nz)
 */
public class DarkLord
  extends AbstractClassifierBasedGeneticAlgorithmWithSecondEvaluation {

  /** for serialization. */
  private static final long serialVersionUID = 4822397823362084867L;

  /**
   * A job class specific to The Dark Lord.
   *
   * @author  dale
   * @version $Revision: 4322 $
   */
  public static class DarkLordJob
    extends ClassifierBasedGeneticAlgorithmWithSecondEvaluationJob<DarkLord> {

    /** for serialization. */
    private static final long serialVersionUID = 8259167463381721274L;

    /**
     * Initializes the job.
     *
     * @param g		the algorithm object this job belongs to
     * @param num	the number of chromsomes
     * @param w		the initial weights
     * @param data	the data to use
     * @param testData	the test data to use, null for cross-validation
     */
    public DarkLordJob(DarkLord g, int num, int[] w, Instances data, Instances testData) {
      super(g, num, w, data, testData);
    }

    /**
     * Returns the "mask" of attributes as range string.
     *
     * @return		the mask
     */
    public String getMaskAsString(){
      String ret = "[";
      int pos = 0;
      int last = -1;
      boolean thefirst = true;
      for(int a = 0; a < getInstances().numAttributes(); a++)
      {
        if(a == getInstances().classIndex())
          continue;
        if(m_Weights[a] == 0)
        {
          if(last == -1)
            continue;
          if(thefirst)
            thefirst = false;
          else
            ret = (new StringBuilder(String.valueOf(ret))).append(",").toString();
          if(pos - last > 1)
            ret = (new StringBuilder(String.valueOf(ret))).append(last).append("-").append(pos).toString();
          else
          if(pos - last == 1)
            ret = (new StringBuilder(String.valueOf(ret))).append(last).append(",").append(pos).toString();
          else
            ret = (new StringBuilder(String.valueOf(ret))).append(last).toString();
          last = -1;
        }
        if(m_Weights[a] != 0)
        {
          if(last == -1)
            last = a;
          pos = a;
        }
      }

      if(last != -1)
      {
        if(!thefirst)
          ret = (new StringBuilder(String.valueOf(ret))).append(",").toString();
        if(pos - last > 1)
          ret = (new StringBuilder(String.valueOf(ret))).append(last).append("-").append(pos).toString();
        else
        if(pos - last == 1)
          ret = (new StringBuilder(String.valueOf(ret))).append(last).append(",").append(pos).toString();
        else
          ret = (new StringBuilder(String.valueOf(ret))).append(last).toString();
      }
      return (new StringBuilder(String.valueOf(ret))).append("]").toString();

    }

    /**
     * Generates a range string of attributes to keep (= one has to use
     * the inverse matching sense with the Remove filter).
     *
     * @return		the range of attributes to keep
     */
    public String getRemoveAsString(){
      String ret = "";
      int pos = 0;
      int last = -1;
      boolean thefirst = true;
      for(int a = 0; a < getInstances().numAttributes(); a++)
      {
        if(m_Weights[a] == 0 && a != getInstances().classIndex())
        {
          if(last == -1)
            continue;
          if(thefirst)
            thefirst = false;
          else
            ret = (new StringBuilder(String.valueOf(ret))).append(",").toString();
          if(pos - last > 1)
            ret = (new StringBuilder(String.valueOf(ret))).append(last + 1).append("-").append(pos + 1).toString();
          else
          if(pos - last == 1)
            ret = (new StringBuilder(String.valueOf(ret))).append(last + 1).append(",").append(pos + 1).toString();
          else
            ret = (new StringBuilder(String.valueOf(ret))).append(last + 1).toString();
          last = -1;
        }
        if(m_Weights[a] != 0 || a == getInstances().classIndex())
        {
          if(last == -1)
            last = a;
          pos = a;
        }
      }

      if(last != -1)
      {
        if(!thefirst)
          ret = (new StringBuilder(String.valueOf(ret))).append(",").toString();
        if(pos - last > 1)
          ret = (new StringBuilder(String.valueOf(ret))).append(last + 1).append("-").append(pos + 1).toString();
        else
        if(pos - last == 1)
          ret = (new StringBuilder(String.valueOf(ret))).append(last + 1).append(",").append(pos + 1).toString();
        else
          ret = (new StringBuilder(String.valueOf(ret))).append(last + 1).toString();
      }
      return ret;

    }

    /**
     * Assembles the data for the textual setup output.
     *
     * @param fitness		the current fitness
     * @param cls		the current classifier
     * @param chromosome	the chromosome responsible
     * @param weights		the current weights
     * @return			the data
     */
    protected Map<String,Object> assembleSetup(double fitness, Classifier cls, int chromosome, int[] weights) {
      Map<String,Object>	result;
      FilteredClassifier	fc;
      Remove			remove;
      StringBuilder		range;
      int			i;

      range = new StringBuilder();
      for (i = 0; i < weights.length; i++) {
        if (weights[i] == 1)
          continue;
        if (range.length() > 0)
          range.append(",");
	range.append("" + (i+1));
      }
      remove = new Remove();
      remove.setAttributeIndices(range.toString());
      fc = new FilteredClassifier();
      fc.setFilter(remove);
      fc.setClassifier(ObjectCopyHelper.copyObject(getOwner().getClassifier()));

      result = super.assembleSetup(fitness, cls, chromosome, weights);
      result.put("Mask", getMaskAsString());
      result.put("FilteredSetup", OptionUtils.getCommandLine(fc));

      return result;
    }

    /**
     * Calculates the new fitness.
     */
    @Override
    public void calcNewFitness(){
      try {
	String weightsStr = weightsToString();
        getLogger().fine((new StringBuilder("calc for:")).append(weightsStr).toString());

        // was measure already calculated for this attribute setup?
        Double cc = getOwner().getResult(weightsStr);
        if (cc != null){
          getLogger().info((new StringBuilder("Already present: ")).append(Double.toString(cc.doubleValue())).toString());
          m_Fitness = cc;
          return;
        }

        Instances newInstances = new Instances(getInstances());
	Instances newTest = null;
	if (getTestInstances() != null)
	  newTest = new Instances(getTestInstances());

        for (int i = 0; i < getInstances().numInstances(); i++) {
          Instance in = newInstances.instance(i);
          int cnt = 0;
          for (int a = 0; a < getInstances().numAttributes(); a++) {
            if (a == getInstances().classIndex())
              continue;
            if (m_Weights[cnt++] == 0){
              in.setValue(a,0);
            }else {
              in.setValue(a,in.value(a));
            }
          }
        }

	// evaluate classifier
	Classifier newClassifier = ObjectCopyHelper.copyObject(getOwner().getClassifier());
	if (newTest == null)
	  m_Fitness = evaluateClassifier(newClassifier, newInstances, getFolds(), getSeed());
	else
	  m_Fitness = evaluateClassifier(newClassifier, newInstances, newTest);

        // process fitness
	if (getOwner().isBetterFitness(m_Fitness)) {
	  boolean canAdd = true;

	  // second evaluation?
	  if (getUseSecondEvaluation() && (newTest == null)) {
	    Classifier newSecondClassifier = ObjectCopyHelper.copyObject(getOwner().getClassifier());
	    m_SecondFitness = evaluateClassifier(newSecondClassifier , newInstances, getSecondFolds(), getSecondSeed());
	    canAdd = getOwner().isSecondBetterFitness(m_SecondFitness);
	    if (getOwner().setSecondNewFitness(m_SecondFitness, newSecondClassifier, m_Chromosome, m_Weights)) {
	      if (isLoggingEnabled())
		getLogger().info("Second evaluation is also better: " + m_SecondFitness);
	    }
	    else {
	      if (isLoggingEnabled())
		getLogger().info("Second evaluation is not better: " + m_SecondFitness);
	    }
	    getOwner().addSecondResult(weightsStr, m_SecondFitness);
	  }

	  if (canAdd && getOwner().setNewFitness(m_Fitness, newClassifier, m_Chromosome, m_Weights)) {
	    generateOutput(m_Fitness, newInstances, newClassifier, m_Chromosome, m_Weights);
	    // notify the listeners
	    getOwner().notifyFitnessChangeListeners(getMeasure().adjust(m_Fitness), newClassifier, m_Weights);
	  }
	}
        else {
          getLogger().fine(getMaskAsString());
        }

        getOwner().addResult(weightsStr, m_Fitness);
      }
      catch(Exception e){
        getLogger().log(Level.SEVERE, "Error: ", e);
        m_Fitness = null;
	if (getUseSecondEvaluation())
	  m_SecondFitness = null;
      }
    }
  }

  /**
   * Returns a string describing the object.
   *
   * @return 			a description suitable for displaying in the gui
   */
  @Override
  public String globalInfo() {
    return "The Dark Lord.";
  }

  /**
   * Returns the default output type to use.
   *
   * @return		the type
   */
  protected OutputType getDefaultOutputType() {
    return OutputType.ALL;
  }

  /**
   * Creates a new Job instance.
   *
   * @param chromosome		the number of chromosomes
   * @param w		the initial weights
   * @return		the instance
   * @param data	the data to use
   * @param testData	the test data to use, null for cross-validation
   */
  @Override
  protected DarkLordJob newJob(int chromosome, int[] w, Instances data, Instances testData) {
    return new DarkLordJob(this, chromosome, w, data, testData);
  }

  /**
   * Generates a Properties file that stores information on the setup of
   * the genetic algorithm. E.g., it backs up the original relation name.
   * The generated properties file will be used as new relation name for
   * the data.
   *
   * @param data	the data to create the setup for
   * @param job		the associated job
   * @see		#PROPS_RELATION
   * @return		the generated setup
   */
  @Override
  protected Properties storeSetup(Instances data, GeneticAlgorithmJob job) {
    Properties		result;
    DarkLordJob		jobDL;
    Remove		remove;

    result = super.storeSetup(data, job);
    jobDL  = (DarkLordJob) job;

    // mask string
    result.setProperty(PROPS_MASK, jobDL.getMaskAsString());

    // remove filter setup
    remove = new Remove();
    remove.setAttributeIndices(jobDL.getRemoveAsString());
    remove.setInvertSelection(true);
    result.setProperty(PROPS_FILTER, OptionUtils.getCommandLine(remove));

    return result;
  }

  /**
   * Some more initializations.
   */
  @Override
  protected void preRun() {
    super.preRun();

    // setup structures
    init(m_NumChrom, m_Instances.numAttributes() * m_BitsPerGene);
  }
}
