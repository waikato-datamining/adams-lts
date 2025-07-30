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

/**
 * AbstractAdamsClassifierTest.java
 * Copyright (C) 2011-2016 University of Waikato, Hamilton, New Zealand
 */

package weka.classifiers;

import adams.env.Environment;
import weka.test.AdamsTestHelper;

/**
 * Abstract test for classifiers within the ADAMS framework.
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 * @version $Revision$
 */
public abstract class AbstractAdamsClassifierTest
  extends AbstractClassifierTest {
  
  static {
    AdamsTestHelper.setRegressionRoot();
  }

  /**
   * Initializes the test.
   *
   * @param name	the name of the test
   */
  public AbstractAdamsClassifierTest(String name) {
    super(name);
    setUpEnvironment();
  }

  /**
   * Sets up the environment.
   */
  protected void setUpEnvironment() {
    Environment.setEnvironmentClass(Environment.class);
    System.setProperty("weka.test.maventest", "true");
  }

  /**
   * configures the CheckClassifier instance used throughout the tests
   *
   * @return the fully configured CheckClassifier instance used for testing
   */
  @Override
  protected CheckClassifier getTester() {
    CheckClassifier result;

    // Only difference from AbstractClassifierTest.getTester() is
    // that we are using the ADAMS CheckClassifier override
    result = new CheckAdamsClassifier();

    result.setSilent(true);
    result.setClassifier(m_Classifier);
    result.setNumInstances(20);
    result.setDebug(DEBUG);
    result.setPostProcessor(getPostProcessor());

    return result;
  }
}
