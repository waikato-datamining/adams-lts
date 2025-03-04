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
 * ScriptedClassifierUtils.java
 * Copyright (C) 2025 University of Waikato, Hamilton, New Zealand
 */

package weka.classifiers;

import adams.core.Placeholders;
import adams.core.UniqueIDs;
import adams.core.Utils;
import adams.core.discovery.IntrospectionHelper;
import adams.core.discovery.IntrospectionHelper.IntrospectionContainer;
import adams.core.discovery.PropertyPath.Path;
import adams.core.discovery.PropertyTraversal;
import adams.core.discovery.PropertyTraversal.Observer;
import adams.core.option.UserMode;
import adams.flow.core.Actor;
import adams.gui.core.AbstractAdvancedScript;
import nz.ac.waikato.cms.locator.ClassLocator;

import java.beans.PropertyDescriptor;

/**
 * Helper class for managing/updating scripts of classifiers that
 * implement {@link ScriptedClassifier}.
 *
 * @author fracpete (fracpete at waikato dot ac dot nz)
 */
public class ScriptedClassifierUtils {

  /**
   * Observer that initializes for prediction.
   */
  public static class InitObserver
    implements Observer {

    /** the context to use. */
    protected Actor m_Context;

    /** the number of initializations that took place. */
    protected int m_Initializations;

    /**
     * Initializes the observer.
     *
     * @param context	the context to set
     */
    public InitObserver(Actor context) {
      m_Context         = context;
      m_Initializations = 0;
    }

    /**
     * Presents the current path, descriptor and object to the observer.
     *
     * @param path	the path
     * @param desc	the property descriptor
     * @param parent	the parent object
     * @param child	the child object
     * @return 		true if to continue observing
     */
    @Override
    public boolean observe(Path path, PropertyDescriptor desc, Object parent, Object child) {
      if (child instanceof ScriptedClassifier) {
	((ScriptedClassifier) child).initPrediction(m_Context);
	m_Initializations++;
      }
      return true;
    }

    /**
     * Returns the number of updates.
     *
     * @return		the updates
     */
    public int getInitializations() {
      return m_Initializations;
    }
  }

  /**
   * Initializes any ScriptedClassifier within the specified object.
   *
   * @param obj		the object to traverse
   * @param context	the context to use for initialization
   * @return 		the number of initializations
   */
  public static int initPrediction(Object obj, Actor context) {
    InitObserver observer;
    PropertyTraversal 	traversal;

    observer = new InitObserver(context);
    traversal = new PropertyTraversal();
    traversal.traverse(observer, obj);
    return observer.getInitializations();
  }

  /**
   * Determines the unique ID (suffix/prefix) to use in the script, if the specified placeholder
   * for the unique ID is present.
   *
   * @param script		the script to inspect and update
   * @param placeholder 	the placeholder that triggers the unique ID generation
   * @param prefix 		whether to generate prefix or suffix
   * @param update 		whether to replace the placeholder with the unique ID immediately
   * @return			the generated unique ID, empty string if not required
   */
  public static String determineUniqueID(AbstractAdvancedScript script, String placeholder, boolean prefix, boolean update) {
    String result;

    result = "";
    if (script.getValue().contains(placeholder)) {
      result = "v" + UniqueIDs.nextLong();
      if (prefix)
	result = result + "_";
      else
	result = "_" + result;
      if (update)
	script.setValue(script.getValue().replace(placeholder, result));
    }

    return result;
  }

  /**
   * Expands flow variables and placeholders in the script.
   *
   * @param script	the script to expand
   * @param context 	the flow context
   * @return		the expanded script
   */
  public static <T extends AbstractAdvancedScript> T expand(T script, Actor context) {
    T		result;
    String	scriptStr;

    scriptStr = script.getValue();
    scriptStr = context.getVariables().expand(scriptStr);
    scriptStr = Placeholders.getSingleton().expand(scriptStr);
    result    = (T) script.getClone();
    result.setValue(scriptStr);

    return result;
  }

  /**
   * Updates the placeholders with the specified values for the given property.
   *
   * @param cls			the classifier to update
   * @param desc		the property to update
   * @param placeholders	the placeholders to replace
   * @param values		the replacement values
   * @return			the updated classifier
   * @throws Exception		if updating fails
   */
  protected static ScriptedClassifier expand(ScriptedClassifier cls, PropertyDescriptor desc, String[] placeholders, String[] values) throws Exception {
    Object			obj;
    AbstractAdvancedScript	script;
    String			scriptStr;
    int				i;

    // get current script instance
    obj = desc.getReadMethod().invoke(cls);

    // update script
    if (obj instanceof AbstractAdvancedScript) {
      script    = (AbstractAdvancedScript) obj;
      scriptStr = script.getValue();
      for (i = 0; i < placeholders.length; i++)
	scriptStr = scriptStr.replace(placeholders[i], values[i]);
      script.setValue(scriptStr);
      desc.getWriteMethod().invoke(cls, script);
    }
    else {
      throw new IllegalStateException("Expected property to be of type " + Utils.classToString(AbstractAdvancedScript.class) + " but got instead: " + Utils.classToString(obj));
    }

    return cls;
  }

  /**
   * Replaces the placeholder with the specified value in any {@link AbstractAdvancedScript}
   * property of the classifier.
   *
   * @param cls		the classifier to update
   * @param placeholder	the placeholder to replace
   * @param value	the replacement value
   * @return		the updated classifier
   * @throws Exception	if introspection or updating of scripts fails
   */
  public static ScriptedClassifier expand(ScriptedClassifier cls, String placeholder, String value) throws Exception {
    return expand(cls, new String[]{placeholder}, new String[]{value});
  }

  /**
   * Replaces the placeholder with the specified value in any {@link AbstractAdvancedScript}
   * property of the classifier.
   *
   * @param cls		the classifier to update
   * @param placeholders	the placeholder to replace
   * @param values	the replacement value
   * @return		the updated classifier
   * @throws Exception	if introspection or updating of scripts fails
   */
  public static ScriptedClassifier expand(ScriptedClassifier cls, String[] placeholders, String[] values) throws Exception {
    ScriptedClassifier		result;
    IntrospectionContainer	cont;

    result = cls;

    if (placeholders == null)
      throw new IllegalArgumentException("Placeholders cannot be null!");
    if (values == null)
      throw new IllegalArgumentException("Values cannot be null!");
    if (placeholders.length != values.length)
      throw new IllegalArgumentException("Number of placeholders different from values: " + placeholders.length + " != " + values.length);

    cont = IntrospectionHelper.introspect(cls, UserMode.EXPERT);
    for (PropertyDescriptor desc: cont.properties) {
      if (ClassLocator.matches(AbstractAdvancedScript.class, desc.getPropertyType()))
	result = expand(result, desc, placeholders, values);
    }

    return result;
  }
}
