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

package weka.filters.unsupervised.instance;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;
import weka.core.Instances;
import weka.core.converters.ArffLoader;
import weka.filters.AbstractAdamsFilterTest;
import weka.filters.Filter;
import weka.test.AdamsTestHelper;

/**
 * Tests SortOnAttribute. Run from the command line with: <br><br>
 * java weka.filters.unsupervised.instance.SortOnAttributeTest
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 * @version $Revision$
 */
public class SortOnAttributeTest
  extends AbstractAdamsFilterTest {

  /**
   * Initializes the test.
   *
   * @param name	the name of the test
   */
  public SortOnAttributeTest(String name) {
    super(name);
  }

  /**
   * Called by JUnit before each test method.
   *
   * @throws Exception if an error occurs
   */
  @Override
  protected void setUp() throws Exception {
    ArffLoader	loader;

    super.setUp();

    loader = new ArffLoader();
    loader.setSource(ClassLoader.getSystemResource("weka/filters/data/bolts.arff"));
    m_Instances = loader.getDataSet();
    m_Instances.setClassIndex(m_Instances.numAttributes() - 1);
  }

  /**
   * Does nothing.
   *
   * @return		null
   */
  @Override
  protected Instances getFilteredClassifierData() {
    return null;
  }

  /**
   * Does not generate data for a classifier.
   */
  @Override
  public void testFilteredClassifier() {
  }

  /**
   * Creates a default SortOnAttribute.
   *
   * @return		the default filter
   */
  @Override
  public Filter getFilter() {
    return new SortOnAttribute();
  }

  /**
   * performs the actual test.
   */
  protected void performTest() {
    Instances icopy = new Instances(m_Instances);
    Instances result = null;
    try {
      m_Filter.setInputFormat(icopy);
    }
    catch (Exception ex) {
      ex.printStackTrace();
      fail("Exception thrown on setInputFormat(): \n" + ex.getMessage());
    }
    try {
      result = Filter.useFilter(icopy, m_Filter);
      assertNotNull(result);
    }
    catch (Exception ex) {
      ex.printStackTrace();
      fail("Exception thrown on useFilter(): \n" + ex.getMessage());
    }

    assertEquals("Number of attributes", icopy.numAttributes(), result.numAttributes());
    assertEquals("Number of instances", icopy.numInstances(), result.numInstances());
  }

  /**
   * Test default.
   */
  public void testDefault() {
    m_Filter = getFilter();
    testBuffered();
    performTest();
  }

  /**
   * Returns a test suite.
   *
   * @return		the suite
   */
  public static Test suite() {
    return new TestSuite(SortOnAttributeTest.class);
  }

  /**
   * Runs the test from the commandline.
   *
   * @param args	ignored
   */
  public static void main(String[] args) {
    AdamsTestHelper.setRegressionRoot();
    TestRunner.run(suite());
  }
}
