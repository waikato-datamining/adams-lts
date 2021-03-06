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
 * RemoveWorst.java
 * Copyright (C) 2019 University of Waikato, Hamilton, NZ
 */

package adams.flow.transformer.wekaevaluationpostprocessor;

import adams.core.QuickInfoHelper;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import weka.classifiers.Evaluation;
import weka.classifiers.evaluation.Prediction;

import java.util.ArrayList;
import java.util.List;

/**
 * Removes the worst predictions, which are considered outliers that
 * detract from the actual model performance.
 *
 * Only works on numeric predictions.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 */
public class RemoveWorst
  extends AbstractNumericClassPostProcessor {

  private static final long serialVersionUID = -8126062783012759418L;

  /** the percentage of the worst predictions to remove (0-1). */
  protected double m_Percent;

  /**
   * Returns a string describing the object.
   *
   * @return 			a description suitable for displaying in the gui
   */
  @Override
  public String globalInfo() {
    return "Removes the worst predictions, which are considered outliers that "
      + "detract from the actual model performance.\n"
      + "Only works on numeric predictions.";
  }

  /**
   * Adds options to the internal list of options.
   */
  @Override
  public void defineOptions() {
    super.defineOptions();

    m_OptionManager.add(
      "percent", "percent",
      0.01, 0.0, 1.0);
  }

  /**
   * Sets the percentage to remove.
   *
   * @param value	the percent (0-1)
   */
  public void setPercent(double value) {
    if (getOptionManager().isValid("percent", value)) {
      m_Percent = value;
      reset();
    }
  }

  /**
   * Returns the percentage to remove.
   *
   * @return		the percent (0-1)
   */
  public double getPercent() {
    return m_Percent;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String percentTipText() {
    return "The percentage of worst predictions to remove (0-1).";
  }

  /**
   * Returns a quick info about the object, which can be displayed in the GUI.
   *
   * @return		null if no info available, otherwise short string
   */
  public String getQuickInfo() {
    return QuickInfoHelper.toString(this, "percent", m_Percent, "percent: ");
  }

  /**
   * Post-processes the evaluation.
   *
   * @param eval	the Evaluation to post-process
   * @return		the generated evaluations
   */
  @Override
  protected List<Evaluation> doPostProcess(Evaluation eval) {
    List<Evaluation>	result;
    List<Prediction>	sorted;
    double		threshold;
    TIntList 		indices;
    int			index;
    Prediction		pred;
    int			i;

    result  = new ArrayList<>();

    // sort by prediction error
    sorted  = new ArrayList<>(eval.predictions());
    sorted.sort(new AbsolutePredictionErrorComparator());

    // determine threshold
    index   = (int) Math.round((1.0 - m_Percent) * sorted.size());
    if (index >= sorted.size())
      index = sorted.size() - 1;
    threshold = Math.abs(sorted.get(index).actual() - sorted.get(index).predicted());
    if (isLoggingEnabled())
      getLogger().info("threshold: " + threshold);

    // determine predictions to keep
    indices = new TIntArrayList();
    for (i = 0; i < eval.predictions().size(); i++) {
      pred = eval.predictions().get(i);
      if (Math.abs(pred.actual() - pred.predicted()) < threshold)
        indices.add(i);
    }

    result.add(newEvaluation("-removed_worst_" + m_Percent, eval, indices));

    return result;
  }
}
