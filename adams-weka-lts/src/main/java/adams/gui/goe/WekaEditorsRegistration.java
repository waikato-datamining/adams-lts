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
 * WekaEditorsRegistration.java
 * Copyright (C) 2011-2020 University of Waikato, Hamilton, New Zealand
 */
package adams.gui.goe;


import adams.core.ClassLister;
import adams.core.classmanager.ClassManager;
import weka.core.PluginManager;

import java.beans.PropertyEditor;
import java.beans.PropertyEditorManager;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;

/**
 * Registers first the WEKA GenericObjectEditor editors and the ADAMS ones.
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 */
public class WekaEditorsRegistration
  extends AbstractEditorRegistration {

  /** for serialization. */
  private static final long serialVersionUID = -2908979337117222215L;

  /** property indicating whether to use the Weka editors instead of the Adams ones. */
  public final static String PROPERTY_WEKAEDITORS = "adams.gui.wekaeditors";

  /** whether to use the Weka editors. */
  protected static boolean m_UseWekaEditors = Boolean.getBoolean(PROPERTY_WEKAEDITORS);

  /** whether registration already occurred. */
  protected static boolean m_Registered;

  /**
   * Subclass of {@link weka.gui.GenericObjectEditor} to get access to the
   * class hierarchies.
   *
   * @author  fracpete (fracpete at waikato dot ac dot nz)
   * @version $Revision$
   */
  public static class AccessibleGenericObjectEditor
    extends weka.gui.GenericObjectEditor {

    /**
     * Returns the editor properties.
     *
     * @return		the properties.
     */
    public static Properties getProperties() {
      return EDITOR_PROPERTIES;
    }
  }

  protected static Set<Class> m_AlreadRegistered;
  static {
    m_AlreadRegistered = new HashSet<>();
  }

  /**
   * For getting access to protected members in the package manager.
   */
  public static class AccessiblePluginManager
    extends PluginManager {

    /**
     * Returns the plugins.
     *
     * @return		the plugins
     */
    public static Map<String, Map<String, String>> getPlugins() {
      return PLUGINS;
    }

    /**
     * Returns the resources.
     *
     * @return		the resources
     */
    public static Map<String, Map<String, String>> getResources() {
      return RESOURCES;
    }

    /**
     * Returns the disabled plugins.
     *
     * @return		the disabled plugins
     */
    public static Set<String> getDisabled() {
      return DISABLED;
    }
  }

  /**
   * Returns whether registration already occurred.
   *
   * @return		true if registration already occurred
   */
  protected boolean hasRegistered() {
    return m_Registered;
  }

  /**
   * Reregisters class hierarchies with ADAMS object editors.
   *
   * @param props	the Weka class hierarchies
   */
  protected void registerEditors(Properties props) {
    Class		cls;
    PropertyEditor	editor;
    Class		newEditor;

    for (Object key: props.keySet()) {
      try {
	// skip arrays
	if (key.toString().endsWith("[]"))
	  continue;

	cls = ClassManager.getSingleton().forName("" + key);
	if (m_AlreadRegistered.contains(cls))
	  continue;

	editor    = PropertyEditorManager.findEditor(cls);
	newEditor = null;

	// find replacement
	if (editor instanceof weka.gui.GenericObjectEditor)
	  newEditor = GenericObjectEditor.class;
	else if (editor instanceof weka.gui.FileEditor)
	  newEditor = FileEditor.class;
	else if (editor instanceof weka.gui.ColorEditor)
	  newEditor = ColorEditor.class;

	// register new editor
	if (newEditor != null) {
	  Editors.registerCustomEditor(cls, newEditor);
	  getLogger().info(
	    "Registering " + cls.getName() + ": "
	      + editor.getClass().getName() + " -> " + newEditor.getName());
	}

	m_AlreadRegistered.add(cls);
      }
      catch (Exception e) {
	getLogger().log(Level.SEVERE, "Failed to register editors: " + key, e);
      }
    }
  }

  /**
   * Registers the class hierarchies with ADAMS.
   *
   * @param props	the Weka class hierarchies
   */
  protected void registerHierarchies(Properties props) {
    String	superclass;
    String[]	classes;
    Class	cls;
    List<Class> classList;

    for (Object key: props.keySet()) {
      superclass = "" + key;
      classes    = props.getProperty(superclass).replaceAll(" ", "").split(",");
      classList  = new ArrayList<>();
      for (String clsname: classes) {
	if (clsname.trim().isEmpty())
	  continue;
	try {
	  cls = ClassManager.getSingleton().forName(clsname);
	  classList.add(cls);
	}
	catch (ClassNotFoundException e) {
	  getLogger().warning("Class not found: " + clsname);
	}
	catch (Exception e) {
	  getLogger().log(Level.SEVERE, "Failed to register class hierarchy: " + key, e);
	}
      }
      if (classList.size() > 0) {
        try {
	  ClassLister.getSingleton().addHierarchy(ClassManager.getSingleton().forName(superclass), classList.toArray(new Class[0]));
	  getLogger().info("Registering class hierarchy: " + key);
	}
	catch (Exception e) {
          getLogger().log(Level.SEVERE, "Failed to register class hierarchy: " + key, e);
	}
      }
    }
  }

  /**
   * Reregisters class hierarchies with ADAMS object editors.
   *
   * @param hierarchies	the Weka class hierarchies
   */
  protected void registerEditors(Map<String, Map<String, String>> hierarchies) {
    Class		cls;
    PropertyEditor	editor;
    Class		newEditor;

    for (String superclass: hierarchies.keySet().toArray(new String[0])) {
      try {
	cls = ClassManager.getSingleton().forName(superclass);
	if (m_AlreadRegistered.contains(cls))
	  continue;

	editor    = PropertyEditorManager.findEditor(cls);
	newEditor = null;

	// find replacement
	if (editor instanceof weka.gui.GenericObjectEditor)
	  newEditor = GenericObjectEditor.class;
	else if (editor instanceof weka.gui.FileEditor)
	  newEditor = FileEditor.class;
	else if (editor instanceof weka.gui.ColorEditor)
	  newEditor = ColorEditor.class;

	// register new editor
	if (newEditor != null) {
	  Editors.registerCustomEditor(cls, newEditor);
	  getLogger().info(
	    "Registering " + cls.getName() + ": "
	      + editor.getClass().getName() + " -> " + newEditor.getName());
	}

	m_AlreadRegistered.add(cls);
      }
      catch (Exception e) {
	getLogger().log(Level.SEVERE, "Failed to register editors: " + superclass, e);
      }
    }
  }

  /**
   * Registers the class hierarchies with ADAMS.
   *
   * @param hierarchies	the Weka class hierarchies
   */
  protected void registerHierarchies(Map<String, Map<String, String>> hierarchies) {
    Set<String>	classes;
    Class	cls;
    List<Class>	classList;

    for (String superclass: hierarchies.keySet().toArray(new String[0])) {
      classes   = hierarchies.get(superclass).keySet();
      classList = new ArrayList<>();
      for (String clsname: classes) {
	if (clsname.trim().isEmpty())
	  continue;
	try {
	  cls = ClassManager.getSingleton().forName(clsname);
	  classList.add(cls);
	}
	catch (ClassNotFoundException e) {
	  getLogger().warning("Class not found: " + clsname);
	}
	catch (Exception e) {
	  getLogger().log(Level.SEVERE, "Failed to register class hierarchy: " + superclass, e);
	}
      }
      if (classList.size() > 0) {
        try {
	  ClassLister.getSingleton().addHierarchy(ClassManager.getSingleton().forName(superclass), classList.toArray(new Class[0]));
	  getLogger().info("Registering class hierarchy: " + superclass);
	}
	catch (Exception e) {
          getLogger().log(Level.SEVERE, "Failed to register class hierarchy: " + superclass, e);
	}
      }
    }
  }

  /**
   * Returns whether Weka editors should be used.
   *
   * @return		true if to use Weka editors
   */
  public static boolean useWekaEditors() {
    return m_UseWekaEditors;
  }

  /**
   * Performs the registration of the editors.
   *
   * @return		true if registration successful
   */
  protected boolean doRegister() {
    weka.gui.GenericObjectEditor.determineClasses();
    weka.gui.GenericObjectEditor.registerEditors();
    if (!useWekaEditors()) {
      registerEditors(AccessibleGenericObjectEditor.getProperties());
      registerEditors(AccessiblePluginManager.getPlugins());
    }
    registerHierarchies(AccessibleGenericObjectEditor.getProperties());
    registerHierarchies(AccessiblePluginManager.getPlugins());
    m_Registered = true;
    return true;
  }
}
