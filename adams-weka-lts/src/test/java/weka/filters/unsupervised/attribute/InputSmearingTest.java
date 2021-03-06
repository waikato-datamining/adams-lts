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
 * Copyright (C) 2013 University of Waikato, Hamilton, New Zealand
 */

package weka.filters.unsupervised.attribute;

import adams.data.weka.WekaAttributeRange;
import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;
import weka.classifiers.meta.FilteredClassifier;
import weka.classifiers.rules.ZeroR;
import weka.core.Attribute;
import weka.core.Instances;
import weka.core.TestInstances;
import weka.filters.AbstractAdamsFilterTest;
import weka.filters.Filter;

/**
 * Tests InputSmearing. Run from the command line with: <br><br>
 * java weka.filters.unsupervised.attribute.InputSmearingTest
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 * @version $Revision$
 */
public class InputSmearingTest
  extends AbstractAdamsFilterTest {

  public InputSmearingTest(String name) {
    super(name);
  }

  /**
   * Called by JUnit before each test method.
   *
   * @throws Exception if an error occurs
   */
  protected void setUp() throws Exception {
    super.setUp();

    m_Instances = getFilteredClassifierData();
  }

  /**
   * Creates a default InputSmearing.
   */
  @Override
  public Filter getFilter() {
    return getFilter("1-2", 1.2);
  }

  /**
   * Creates a specialized InputSmearing
   */
  public Filter getFilter(String range, double stdev) {
    InputSmearing result = new InputSmearing();
    result.setAttributeRange(new WekaAttributeRange(range));
    result.setStdDev(stdev);
    return result;
  }

  /**
   * returns data generated for the FilteredClassifier test.
   *
   * @return		the dataset for the FilteredClassifier
   * @throws Exception	if generation of data fails
   */
  protected Instances getFilteredClassifierData() throws Exception {
    TestInstances	testinst;

    testinst = new TestInstances();
    testinst.setNumNominal(0);
    testinst.setNumNumeric(20);
    testinst.setClassType(Attribute.NOMINAL);
    testinst.setNumInstances(50);

    return testinst.generate();
  }

  /**
   * returns the configured FilteredClassifier. Since the base classifier is
   * determined heuristically, derived tests might need to adjust it.
   * 
   * @return the configured FilteredClassifier
   */
  @Override
  protected FilteredClassifier getFilteredClassifier() {
    FilteredClassifier	result;
    Filter		filter;
    
    result = new FilteredClassifier();
    
    filter = getFilter();
    result.setFilter(filter);
    result.setClassifier(new ZeroR());
    
    return result;
  }

  public static Test suite() {
    return new TestSuite(InputSmearingTest.class);
  }

  public static void main(String[] args){
    TestRunner.run(suite());
  }
}
