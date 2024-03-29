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
 * Merge.java
 * Copyright (C) 2016-2022 University of Waikato, Hamilton, NZ
 */

package adams.gui.tools.wekainvestigator.datatable.action;

import adams.core.ObjectCopyHelper;
import adams.flow.core.Token;
import adams.flow.transformer.WekaInstancesMerge;
import adams.flow.transformer.WekaMergeInstancesActor;
import adams.gui.event.WekaInvestigatorDataEvent;
import adams.gui.goe.GenericObjectEditorDialog;
import adams.gui.tools.wekainvestigator.data.DataContainer;
import adams.gui.tools.wekainvestigator.data.MemoryContainer;
import weka.core.Instances;

import java.awt.Dialog.ModalityType;
import java.awt.event.ActionEvent;

/**
 * Merges the selected datasets (side-by-side).
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 * @see Append
 */
public class Merge
  extends AbstractEditableDataTableAction {

  private static final long serialVersionUID = -8374323161691034031L;

  /** the last merge setup. */
  protected WekaMergeInstancesActor m_LastSetup;

  /**
   * Instantiates the action.
   */
  public Merge() {
    super();
    setName("Merge");
    setIcon("merge.png");
    setAsynchronous(true);
  }

  /**
   * Invoked when an action occurs.
   *
   * @param e		the event
   */
  @Override
  protected void doActionPerformed(ActionEvent e) {
    WekaMergeInstancesActor 	merge;
    GenericObjectEditorDialog	dialog;
    DataContainer[]		conts;
    Instances[]			data;
    int				i;
    Token 			token;
    String			msg;
    MemoryContainer		cont;

    if (m_LastSetup == null)
      merge = new WekaInstancesMerge();
    else
      merge = ObjectCopyHelper.copyObject(m_LastSetup);
    if (getOwner().getParentDialog() != null)
      dialog = new GenericObjectEditorDialog(getOwner().getParentDialog(), ModalityType.DOCUMENT_MODAL);
    else
      dialog = new GenericObjectEditorDialog(getOwner().getParentFrame(), true);
    dialog.setTitle("Configure merge");
    dialog.setUISettingsPrefix(WekaMergeInstancesActor.class);
    dialog.getGOEEditor().setCanChangeClassInDialog(true);
    dialog.getGOEEditor().setClassType(WekaMergeInstancesActor.class);
    dialog.setCurrent(merge);
    dialog.pack();
    dialog.setLocationRelativeTo(getOwner());
    dialog.setVisible(true);
    if (dialog.getResult() != GenericObjectEditorDialog.APPROVE_OPTION) {
      dialog.dispose();
      logMessage("Merge cancelled!");
      return;
    }
    merge       = (WekaMergeInstancesActor) dialog.getCurrent();
    m_LastSetup = ObjectCopyHelper.copyObject(merge);
    logMessage("Merge setup: " + merge.toCommandLine());
    dialog.dispose();

    // collect data
    conts = getSelectedData();
    data  = new Instances[conts.length];
    msg   = "";
    for (i = 0; i < conts.length; i++) {
      if (i > 0)
	msg += ", ";
      data[i] = conts[i].getData();
      msg += conts[i].getID() + "/" + conts[i].getData().relationName();
    }
    logMessage("Merging: " + msg);

    // transform
    token = new Token(data);
    msg   = merge.setUp();
    if (msg == null) {
      merge.input(token);
      msg = merge.execute();
      if (msg == null) {
	token = merge.output();
	cont = new MemoryContainer((Instances) token.getPayload());
	getData().add(cont);
	fireDataChange(new WekaInvestigatorDataEvent(getOwner().getOwner(), WekaInvestigatorDataEvent.ROWS_ADDED, getData().size() - 1));
      }
    }
    merge.destroy();

    if (msg != null)
      logError(msg, "Failed to merge datasets");
    else
      logMessage("Merge successful!");
  }

  /**
   * Updates the action.
   */
  @Override
  public void update() {
    setEnabled(!isBusy() && getTable().getSelectedRowCount() > 1);
  }
}
