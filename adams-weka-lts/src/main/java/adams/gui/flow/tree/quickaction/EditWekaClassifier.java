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
 * EditWekaClassifier.java
 * Copyright (C) 2024 University of Waikato, Hamilton, New Zealand
 */

package adams.gui.flow.tree.quickaction;

import weka.classifiers.Classifier;
import weka.classifiers.rules.ZeroR;

/**
 * Lets the user edit the Weka classifier.
 *
 * @author fracpete (fracpete at waikato dot ac dot nz)
 */
public class EditWekaClassifier
  extends AbstractGOEQuickAction {

  private static final long serialVersionUID = -6455846796708144253L;

  /**
   * Returns the property name to look for.
   *
   * @return		the name
   */
  @Override
  protected String getPropertyName() {
    return "classifier";
  }

  /**
   * The abstract superclass to use.
   *
   * @return		the superclass
   */
  @Override
  protected Class getSuperclass() {
    return Classifier.class;
  }

  /**
   * Returns the default object to use.
   *
   * @return		the default object
   */
  @Override
  protected Object getDefaultObject() {
    return new ZeroR();
  }

  /**
   * Returns the description of the class used in errors/undo points.
   *
   * @return		the description
   */
  @Override
  protected String getClassDescription() {
    return "classifier";
  }
}
