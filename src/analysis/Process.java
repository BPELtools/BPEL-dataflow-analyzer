/**
 * Analysis of Process elements
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

import infrastructure.State;
import infrastructure.Variable;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.bpel.model.Activity;
import org.eclipse.bpel.model.ExtensibleElement;
import org.eclipse.emf.ecore.EObject;

/**
 * Code for analysis of Process elements
 * @author Sebastian Breier
 *
 */
public class Process {

	/**
	 * Start analysis on the given process model
	 * Cleans up earlier analysis runs, then starts analysis
	 * See p. 63 of DIP-2726
	 * @param process
	 */
	public static void analyzeProcessModel(org.eclipse.bpel.model.Process process) {
		State myState = State.getInstance();
		findAllVariables(process);
		Collection<Variable> vars = myState.getVariables();
		Set<Activity> allActivities = findAllActivities(process);
		// FIXME Debug run
//		for (Variable var: vars) {
		Variable var = vars.toArray(new Variable[]{})[0];
			myState.clearWrites();
			myState.clearFlags();
			analysis.Activity.handleActivity(process, var);
			System.err.println("--------------------");
//		}
	}

	/**
	 * Find all activities contained in a given element
	 * @param process
	 * @return
	 */
	private static Set<Activity> findAllActivities(ExtensibleElement element) {
//		System.err.println(">findAllActivities: " + element);
		Set<Activity> allActs = new HashSet<Activity>();
		if (element instanceof Activity) {
			allActs.add((Activity)element);
		}
		for (EObject object: element.eContents()) {
			if (!(object instanceof ExtensibleElement)) {
				continue;
			}
			ExtensibleElement contentsElement = (ExtensibleElement)object;
			Set<Activity> allSubActs = findAllActivities(contentsElement);
			allActs.addAll(allSubActs);
		}
		return allActs;
	}

	/**
	 * Find all variables of the process and save them in State
	 * Does a depth search over the BPEL tree
     * TODO: This approach fails because the WSDL can't be loaded, and I have no idea why
     * Ideally, this should find all variable parts in the WSDL
     * Currently, nothing happens.
     * Variables have to be written to State beforehand
	 */
	private static void findAllVariables(ExtensibleElement element) {
//		for (EObject object: element.eContents()) {
//			if (!(object instanceof ExtensibleElement)) {
//				continue;
//			}
//			ExtensibleElement contentsElement = (ExtensibleElement)object;
//			if (contentsElement instanceof Variable) {
//				Variable variable = (Variable)contentsElement;
//				Message message = variable.getMessageType();
//			} else {
//				findAllVariables(contentsElement);
//			}
//		}
	}
	
}
