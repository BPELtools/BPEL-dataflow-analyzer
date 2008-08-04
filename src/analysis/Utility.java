/**
 * Utility classes
 * 
 * Copyright 2008 Sebastian Breier
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package analysis;

import java.io.StringWriter;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.bpel.model.Activity;
import org.eclipse.bpel.model.Condition;
import org.eclipse.bpel.model.ExtensibleElement;
import org.eclipse.bpel.model.Target;
import org.eclipse.bpel.model.Targets;
import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;

/**
 * Utility functions
 * @author Sebastian Breier
 *
 */
public class Utility {
	/**
	 * Find the parent element
	 * @param element
	 */
	public static ExtensibleElement getParent(ExtensibleElement element) {
		if (element == null) {
			return null;
		}
		EObject containerObject = element.eContainer();
		if (!(containerObject instanceof ExtensibleElement)) {
			return null;
		}
		return (ExtensibleElement)containerObject;
	}
	
	/**
	 * Find the only contained Activity of a given element
	 * @param el
	 * @return
	 */
	public static org.eclipse.bpel.model.Activity getOnlyContainedActivity(ExtensibleElement el) {
		if (el == null) {
			return null;
		}
		for (EObject containedObject: el.eContents()) {
			if (!(containedObject instanceof org.eclipse.bpel.model.Activity)) {
				continue;
			}
			return (org.eclipse.bpel.model.Activity)containedObject;
		}
		return null;
	}
	
	/**
	 * Return an EList of all incoming links of the activity
	 * If any error occurs, this will return an empty list, but not null
	 * @param el
	 * @return
	 */
	public static EList<Target> getTargetLinks(ExtensibleElement el) {
		EList<Target> emptyList = new BasicEList<Target>();
		Targets t = getTargets(el);
		if (t == null) {
			return emptyList;
		}
		EList<Target> children = t.getChildren();
		if (children == null) {
			return emptyList;
		}
		return children;
	}
	
	/**
	 * Retrieve the join condition of an ExtensibleElement
	 * Might be "null"
	 * @param element
	 * @return
	 */
	public static Condition getJoinCondition(ExtensibleElement element) {
		Targets t = getTargets(element);
		if (t == null) {
			return null;
		}
		return t.getJoinCondition();
	}
	
	/**
	 * Retrieve the targets of an ExtensibleElement
	 * @param element
	 * @return
	 */
	private static Targets getTargets(ExtensibleElement element) {
		if (!(element instanceof org.eclipse.bpel.model.Activity)) {
			return null;
		}
		org.eclipse.bpel.model.Activity act = (org.eclipse.bpel.model.Activity)element;
		return act.getTargets();
	}

	/**
	 * Find all directly contained children of an ExtensibleElement
	 * @param activity
	 * @return
	 */
	public static Set<ExtensibleElement> findChildrenEE(ExtensibleElement activity) {
//		System.err.println(">findChildren: activity = " + Utility.dumpEE(activity));
		Set<ExtensibleElement> children = new HashSet<ExtensibleElement>();
		EList<EObject> containedObjects = activity.eContents();
		for (EObject containedObject: containedObjects) {
			if (containedObject instanceof ExtensibleElement) {
				children.add((ExtensibleElement)containedObject);
			}
		}
//		System.err.println("<findChildren: children = " + dumpSet(children));
		return children;
	}

	/**
	 * Find all descendants (children and children's children) of an ExtensibleElement
	 * Uses a recursive approach
	 * @param activity
	 * @return
	 */
	public static Set<ExtensibleElement> findDescendantsEE(ExtensibleElement activity) {
//		System.err.println(">findDescendants: activity = " + Utility.dumpEE(activity));
		Set<ExtensibleElement> descendants = new HashSet<ExtensibleElement>();
		Set<ExtensibleElement> children = findChildrenEE(activity);
		descendants.addAll(children);
		for (ExtensibleElement child: children) {
			Set<ExtensibleElement> moreDescendants = findDescendantsEE(child);
			descendants.addAll(moreDescendants);
		}
//		System.err.println("<findDescendants: descendants = " + dumpSet(descendants));
		return descendants;
	}
	
	/**
	 * Return a string that is a textual representation of the given set of activities
	 * @param activities
	 */
	public static String dumpSet(Set<? extends ExtensibleElement> activities) {
		StringWriter writer = new StringWriter();
		for (ExtensibleElement activity: activities) {
			writer.append(dumpEE(activity));
			writer.append(", ");
		}
		return writer.toString();
	}
	
	/**
	 * Return a textual representation of the given ExtensibleElement
	 * @param activity
	 * @return
	 */
	public static String dumpEE(ExtensibleElement activity) {
		if (activity instanceof org.eclipse.bpel.model.Process) {
			return "Process";
		}
		if (activity instanceof Activity) {
			Activity act = (Activity)activity;
			String activityName = act.getName();
			if (activityName != null && !activityName.equals("")) {
				return "Activity " + activityName;
			}
		}
		return activity.getElement().getNodeName();
	}
	
	/**
	 * Filter a set of ExtensibleElements for only Activities
	 * @param activitiesEE
	 * @return
	 */
	public static Set<Activity> filterActivities(Set<ExtensibleElement> activitiesEE) {
		Set<Activity> activitiesAct = new HashSet<Activity>();
		for (ExtensibleElement element: activitiesEE) {
			if (element instanceof Activity) {
				activitiesAct.add((Activity)element);
			}
		}
		return activitiesAct;
	}
	
	/**
	 * Find all children of type activity of an ExtensibleElement
	 * @param activity
	 * @return
	 */
	public static Set<Activity> findChildrenAct(ExtensibleElement activity) {
		return filterActivities(findChildrenEE(activity));
	}
	
	/**
	 * Find all descendants of type activity of an ExtensibleElement
	 * @param activity
	 * @return
	 */
	public static Set<Activity> findDescendantsAct(ExtensibleElement activity) {
		return filterActivities(findDescendantsEE(activity));
	}

}
