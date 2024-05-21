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
 * PullUpClassifier.java
 * Copyright (C) 2024 University of Waikato, Hamilton, NZ
 */

package adams.gui.goe.popupmenu;

import adams.gui.goe.GenericObjectEditorPopupMenu;
import weka.classifiers.Classifier;
import weka.classifiers.SingleClassifierEnhancer;

import javax.swing.JComponent;
import javax.swing.JMenuItem;
import java.awt.event.ActionEvent;
import java.beans.PropertyEditor;

/**
 * For pulling up classifiers from SingleClassifierEnhancer wrappers.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 */
public class PullUpClassifier
  extends AbstractGenericObjectEditorPopupMenuCustomizer {

  private static final long serialVersionUID = 7573235626494111377L;

  /**
   * The name used for sorting.
   *
   * @return		the name
   */
  @Override
  public String getName() {
    return "Pull up";
  }

  /**
   * Customizes the GOE popup menu.
   *
   * @param menu	the menu to customize
   * @param editor	the current editor
   * @param comp	the GUI context
   */
  @Override
  protected boolean handles(GenericObjectEditorPopupMenu menu, PropertyEditor editor, JComponent comp) {
    return (editor.getValue() instanceof SingleClassifierEnhancer);
  }

  /**
   * Customizes the GOE popup menu.
   *
   * @param menu	the menu to customize
   * @param editor	the current editor
   * @param comp	the GUI context
   */
  @Override
  protected void doCustomize(GenericObjectEditorPopupMenu menu, final PropertyEditor editor, JComponent comp) {
    JMenuItem		item;

    item = new JMenuItem(getName());
    item.addActionListener((ActionEvent e) -> {
      SingleClassifierEnhancer wrapper = (SingleClassifierEnhancer) editor.getValue();
      Classifier base = wrapper.getClassifier();
      editor.setValue(base);
    });
    menu.add(item);
  }
}
