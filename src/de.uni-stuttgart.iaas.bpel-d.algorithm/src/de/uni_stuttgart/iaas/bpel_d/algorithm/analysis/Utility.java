/**
* Utility classes
 * 
 * Copyright 2008 Sebastian Breier
 * Copyright 2009-2010 Yangyang Gao, Oliver Kopp
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
package de.uni_stuttgart.iaas.bpel_d.algorithm.analysis;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.bpel.model.*;
import org.eclipse.bpel.model.Activity;
import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.w3c.dom.Element;

/**
 * Utility functions
 * @author Sebastian Breier, Yangyang Gao, Oliver Kopp
 *
 */
public class Utility {
	
	/**
	 * Find the parent element
	 * @param element
	 */
	public static BPELExtensibleElement getParent(EObject element) {
		if (element == null) {
			return null;
		}
		
		EObject containerObject = element.eContainer();
		if (containerObject == null)
			return null;
		
		// EventHandler FaultHandler TerminationHandler
		
		if (!(containerObject instanceof Activity) &&
			!(containerObject instanceof org.eclipse.bpel.model.Process ) &&
			!(containerObject instanceof OnAlarm) &&
			!(containerObject instanceof OnEvent) &&
			!(containerObject instanceof OnMessage) &&
			!(containerObject instanceof ElseIf) &&
			!(containerObject instanceof Else) &&
			!(containerObject instanceof Catch) &&
			!(containerObject instanceof CatchAll) &&
			!(containerObject instanceof TerminationHandler) ) {
			return getParent(containerObject);
		}
		return (BPELExtensibleElement)containerObject;
	}

	/**
	 * Find the predecessor element
	 * @param element
	 */
	public static BPELExtensibleElement getPredecessor(BPELExtensibleElement element) {
		if (element == null){
			return null;
		}
		EObject containerObject = element.eContainer();
		if(!(containerObject instanceof BPELExtensibleElement)){
			return null;
		}
		return (BPELExtensibleElement)containerObject;
	}
	
	/**
	 * Find the only contained Activity of a given element
	 * @param el
	 * @return
	 */
	public static org.eclipse.bpel.model.Activity getOnlyContainedActivity(BPELExtensibleElement el) {
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
	public static EList<Target> getTargetLinks(BPELExtensibleElement el) {
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
	 * Retrieve the join condition of an BPELExtensibleElement
	 * Might be "null"
	 * @param element
	 * @return
	 */
	public static String getJoinCondition(BPELExtensibleElement element) {
		Targets t = getTargets(element);
		if (t == null) {
			return null;
		}
		Condition c = t.getJoinCondition();
		if (c == null)
			return null;
		
		// TODO check whether it's really "getBody()"
		String res = c.getBody().toString();
		return res;		
	}
	
	public static String getTransitionCondition(Link link) {
		EList<Source> sources = link.getSources();
		Source s = sources.get(0);
		Condition c = s.getTransitionCondition();
		if (c == null)
			return null;
		
		// TODO check whether it's really "getBody()" - see also getJoinCondition
		String res = c.getBody().toString();
		return res;		
	}

	
	/**
	 * Retrieve the targets of an BPELExtensibleElement
	 * @param element
	 * @return
	 */
	private static Targets getTargets(BPELExtensibleElement element) {
		if (!(element instanceof org.eclipse.bpel.model.Activity)) {
			return null;
		}
		org.eclipse.bpel.model.Activity act = (org.eclipse.bpel.model.Activity)element;
		return act.getTargets();
	}
	
	public static EList<Source> getSourceLinks(BPELExtensibleElement el) {
		EList<Source> emptyList = new BasicEList<Source>();
		Sources s = getSources(el);
		if (s == null) {
			return emptyList;
		}
		EList<Source> children = s.getChildren();
		if (children == null) {
			return emptyList;
		}
		return children;
	}


	/*
	 * wie getTagets
	 */
	private static Sources getSources(BPELExtensibleElement flowChild) {
		if (!(flowChild instanceof org.eclipse.bpel.model.Activity)) {
			return null;
		}
		org.eclipse.bpel.model.Activity act = (org.eclipse.bpel.model.Activity)flowChild;
		return act.getSources();
	}
	
	
	/**
	 * Find all directly contained children of an BPELExtensibleElement
	 * @param activity
	 * @return
	 */
	public static Set<BPELExtensibleElement> findChildrenEE(BPELExtensibleElement activity) {
//		System.err.println(">findChildren: activity = " + Utility.dumpEE(activity));
		Set<BPELExtensibleElement> children = new HashSet<BPELExtensibleElement>();
		EList<EObject> containedObjects = activity.eContents();
		for (EObject containedObject: containedObjects) {
			if (containedObject instanceof BPELExtensibleElement) {
				children.add((BPELExtensibleElement)containedObject);
			}
		}
//		System.err.println("<findChildren: children = " + dumpSet(children));
		return children;
	}

	/**
	 * Find all descendants (children and children's children) of an BPELExtensibleElement
	 * Uses a recursive approach
	 * @param activity
	 * @return
	 */
	public static Set<BPELExtensibleElement> findDescendantsEE(BPELExtensibleElement activity) {
//		System.err.println(">findDescendants: activity = " + Utility.dumpEE(activity));
		Set<BPELExtensibleElement> descendants = new HashSet<BPELExtensibleElement>();
		Set<BPELExtensibleElement> children = findChildrenEE(activity);
		descendants.addAll(children);
		for (BPELExtensibleElement child: children) {
			Set<BPELExtensibleElement> moreDescendants = findDescendantsEE(child);
			descendants.addAll(moreDescendants);
		}
//		System.err.println("<findDescendants: descendants = " + dumpSet(descendants));
		return descendants;
	}
	
	/**
	 * Return a string that is a textual representation of the given set of activities
	 * @param activities
	 */
	public static String dumpSet(Set<? extends BPELExtensibleElement> activities) {
		StringWriter writer = new StringWriter();
		for (BPELExtensibleElement activity: activities) {
			writer.append(dumpEE(activity));
			writer.append(", ");
		}
		return writer.toString();
	}
	
	/**
	 * Return a textual representation of the given BPELExtensibleElement
	 * @param activity
	 * @return
	 */
	public static String dumpEE(BPELExtensibleElement activity) {
		if (activity == null)
			return "null";
		if (activity instanceof org.eclipse.bpel.model.Process) {
			return "Process";
		}
		if (activity instanceof org.eclipse.bpel.model.Activity) {
			org.eclipse.bpel.model.Activity act = (org.eclipse.bpel.model.Activity)activity;
			String activityName = act.getName();
			if (activityName != null && !activityName.equals("")) {
				return "Activity " + activityName;
			}
		}
		Element e = activity.getElement(); 
		return e.getNodeName();
	}
	
	/**
	 * Filter a set of ExtensibleElements for only Activities
	 * @param activitiesEE
	 * @return
	 */
	public static Set<Activity> filterActivities(Set<BPELExtensibleElement> activitiesEE) {
		Set<Activity> activitiesAct = new HashSet<Activity>();
		for (BPELExtensibleElement element: activitiesEE) {
			if (element instanceof Activity) {
				activitiesAct.add((Activity)element);
			}
		}
		return activitiesAct;
	}
	
	/**
	 * Find all children of type activity of an BPELExtensibleElement
	 * @param activity
	 * @return
	 */
	public static Set<Activity> findChildrenAct(BPELExtensibleElement activity) {
		return filterActivities(findChildrenEE(activity));
	}
	
	/**
	 * Find all descendants of type activity of an BPELExtensibleElement
	 * @param activity
	 * @return
	 */
	public static Set<Activity> findDescendantsAct(BPELExtensibleElement activity) {
		return filterActivities(findDescendantsEE(activity));
	}
	
	private static void localToScopeEEs(BPELExtensibleElement el,
			Set<org.eclipse.bpel.model.Activity> noScopes,
			Set<org.eclipse.bpel.model.Activity> scopes) {
	
		if (el instanceof Scope) {
			scopes.add((Scope) el);
			return;
		} else if (el instanceof Activity) {
			
		}
		
		EList<EObject> containedObjects = el.eContents();
		for (EObject containedObject: containedObjects) {
			if (containedObject instanceof BPELExtensibleElement) {
				localToScopeEEs((BPELExtensibleElement) containedObject, noScopes, scopes);
			}
		}

	}
	
	/**
	 * 
	 * @param act
	 * @param noScopes - set to store the found descendants local to scope - has to be initialized 
	 * @param scopes - set for storing the descendants local to scope - has to be initialized
	 */
	public static void localToScopeActivities(org.eclipse.bpel.model.Activity act,
			Set<org.eclipse.bpel.model.Activity> noScopes,
			Set<org.eclipse.bpel.model.Scope> scopes) {
		
		if (act instanceof Scope) {
			scopes.add((Scope) act);
			return;
		} 
		
		noScopes.add(act);
		
		if (act instanceof While) {
			localToScopeActivities(((While) act).getActivity(), noScopes, scopes);
		} else if (act instanceof RepeatUntil) {
			localToScopeActivities(((RepeatUntil) act).getActivity(), noScopes, scopes);
		} else if (act instanceof ForEach) {
			localToScopeActivities(((ForEach) act).getActivity(), noScopes, scopes);
		} else if (act instanceof Sequence) {
			Sequence s = (Sequence) act;
			EList<Activity> acts = s.getActivities();
			for (Activity a: acts) {
				localToScopeActivities(a, noScopes, scopes);	
			}
		} else if (act instanceof org.eclipse.bpel.model.Flow) {
			org.eclipse.bpel.model.Flow f = (org.eclipse.bpel.model.Flow) act;
			EList<Activity> acts = f.getActivities();
			for (Activity a: acts) {
				localToScopeActivities(a, noScopes, scopes);	
			}			
		} else if (act instanceof Pick) {
			Pick pick = (Pick) act;
			EList<OnAlarm> alarms = pick.getAlarm();
			for (OnAlarm o: alarms) {
				noScopes.add(o.getActivity());
				localToScopeActivities(o.getActivity(), noScopes, scopes);
			}
			EList<OnMessage> messages = pick.getMessages();
			for (OnMessage m: messages) {
				noScopes.add(m.getActivity());
				localToScopeActivities(m.getActivity(), noScopes, scopes);
			}
		} else {
			// basic activities
			// nothing to do in this case!
			// recursion stopped
		}
	}
	
	public static BPELExtensibleElement getSharedParent(
			BPELExtensibleElement source, BPELExtensibleElement target) {
		// store all parents of the source in a list
		List<BPELExtensibleElement> l = new ArrayList<BPELExtensibleElement>();
		BPELExtensibleElement p = Utility.getParent(source);
		while (!(p instanceof org.eclipse.bpel.model.Process)) {
			l.add(p);
			p = getParent(p);
		}
		l.add(p);
		
		// traverse parents of target
		// first hit is the shared parent
		p = Utility.getParent(target);
		while (!l.contains(p)) {
			p = getParent(p);
		}
		
		return p;
	}
	/**
	 * @param nameSuperElement  
	 * @param nameSubElement
	 * @return true iff nameSubElement is a REAL subelement of nameSuperElement
	 */
	public static boolean subElement(String nameSuperElement, String nameSubElement){
		boolean moreLength = (nameSubElement.length() >= nameSuperElement.length());
		boolean startsWith = (nameSubElement.startsWith(nameSuperElement)); 
		return (moreLength && startsWith);
	}
	
	

}
