/**
 * Flow analysis
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

import infrastructure.Variable;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.bpel.model.ExtensibleElement;
import org.eclipse.bpel.model.Link;
import org.eclipse.bpel.model.Source;
import org.eclipse.bpel.model.Target;
import org.eclipse.emf.common.util.EList;

/**
 * Flow analysis
 * HandleFlow / HandleEndOfFlow
 * @author Sebastian Breier
 *
 */
public class Flow {
	
	/**
	 * See DIP-2726 p. 65 algo 9
	 * Analysis a Flow container activity
	 * @param element
	 * @param variableElement
	 */
	public static void HandleFlow(org.eclipse.bpel.model.Flow flowActivity, Variable variableElement) {
//		System.err.println(">HandleFlow: element = " + Utility.dumpEE(flowActivity) + ", variableElement = " + variableElement);
		Set<org.eclipse.bpel.model.Activity> roots = findRoots(flowActivity);
		for (org.eclipse.bpel.model.Activity containedActivity: roots) {
			Activity.handleActivity((ExtensibleElement)containedActivity, variableElement);
		}
	}
	
	/**
	 * Find all the contained root activities of a flow activity
	 * See DIP-2726 p. 65 algo 9 l. 2
	 * @param flowActivity
	 * @return
	 */
	private static Set<org.eclipse.bpel.model.Activity> findRoots(org.eclipse.bpel.model.Flow flowActivity) {
//		System.err.println(">findRoots: flowActivity = " + Utility.dumpEE(flowActivity));
		Set<org.eclipse.bpel.model.Activity> flowChildren = Utility.findChildrenAct((ExtensibleElement)flowActivity);
		Set<org.eclipse.bpel.model.Activity> flowDescendants = Utility.findDescendantsAct((ExtensibleElement)flowActivity);
		Set<org.eclipse.bpel.model.Activity> roots = new HashSet<org.eclipse.bpel.model.Activity>();
		for (org.eclipse.bpel.model.Activity flowChild: flowChildren) {
			boolean sourceInFlow = false;
			EList<Target> targets = Utility.getTargetLinks(flowChild);
			for (Target target: targets) {
				Link link = target.getLink();
				EList<Source> linkSources = link.getSources();
				for (Source linkSource: linkSources) {
					org.eclipse.bpel.model.Activity sourceActivity = linkSource.getActivity();
					if (flowDescendants.contains(sourceActivity)) {
						sourceInFlow = true;
					}
				}
			}
			if (!sourceInFlow) {
				roots.add(flowChild);
			}
		}
//		System.err.println("<findRoots: roots = " + Utility.dumpSet(roots));
		return roots;
	}

	/**
	 * Analysis of the end of a Flow container activity
	 * See DIP-2726 p. 66 algo 10
	 * @param flowElement
	 * @param variableElement
	 */
	public static void HandleEndOfFlow(org.eclipse.bpel.model.Flow flowActivity, Variable variableElement) {
		System.err.println(">HandleEndOfFlow STUB: element = " + Utility.dumpEE(flowActivity) + ", variableElement = " + variableElement);
	}
	
}
