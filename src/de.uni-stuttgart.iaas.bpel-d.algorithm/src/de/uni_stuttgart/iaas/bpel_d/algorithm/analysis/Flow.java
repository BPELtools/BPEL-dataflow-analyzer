/**
 * Flow analysis
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


import java.util.HashSet;
import java.util.Set;

import org.eclipse.bpel.model.BPELExtensibleElement;
import org.eclipse.bpel.model.Link;
import org.eclipse.bpel.model.Source;
import org.eclipse.bpel.model.Target;
import org.eclipse.emf.common.util.EList;
import org.grlea.log.SimpleLogger;

import de.uni_stuttgart.iaas.bpel_d.algorithm.infrastructure.InOut;
import de.uni_stuttgart.iaas.bpel_d.algorithm.infrastructure.Placement;
import de.uni_stuttgart.iaas.bpel_d.algorithm.infrastructure.State;
import de.uni_stuttgart.iaas.bpel_d.algorithm.infrastructure.VariableElement;
import de.uni_stuttgart.iaas.bpel_d.algorithm.infrastructure.Writes;

/**
 * Flow analysis
 * HandleFlow / HandleEndOfFlow
 */
public class Flow {
	
	private final static SimpleLogger logger = new SimpleLogger(Flow.class);

	/**
	 * See DIP-2726 p. 65 algo 9
	 * Analysis a Flow container activity
	 * @param element
	 * @param variableElement
	 */
	public static void HandleFlow(org.eclipse.bpel.model.Flow flowActivity, String variableElement) {
//		System.err.println(">HandleFlow: element = " + Utility.dumpEE(flowActivity) + ", variableElement = " + variableElement);
		Set<org.eclipse.bpel.model.Activity> roots = findRoots(flowActivity);
		for (org.eclipse.bpel.model.Activity containedActivity: roots) {
			Activity.handleActivity((BPELExtensibleElement)containedActivity, variableElement);
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
		Set<org.eclipse.bpel.model.Activity> flowChildren = Utility.findChildrenAct((BPELExtensibleElement)flowActivity);
		Set<org.eclipse.bpel.model.Activity> flowDescendants = Utility.findDescendantsAct((BPELExtensibleElement)flowActivity);
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
	public static void HandleEndOfFlow(org.eclipse.bpel.model.Flow flowActivity, String variableElement) {
		logger.entry("HandleEndOfFlow()");
		logger.debugObject("act", Utility.dumpEE(flowActivity));
		logger.debugObject("ve", variableElement);

		Set<org.eclipse.bpel.model.Activity> leaves = findLeaves(flowActivity);
		boolean allFinished = true;
		for (org.eclipse.bpel.model.Activity leaf : leaves) {
			if (!State.getInstance().isFinished(leaf)) {
				allFinished = false;
				logger.exit("HandleEndOfFlow() - nothing to do yet.");
				return;
			}
		}
		if (allFinished) {
			State state = State.getInstance();
			Writes writesOut = State.getInstance().getWrites(new Placement(flowActivity, InOut.OUT));
			
			// line 4
			for (org.eclipse.bpel.model.Activity leaf : leaves) {
				Writes leafData = state.getWrites(new Placement(leaf, InOut.OUT));
				writesOut.copyData(leafData);
			}
			
			Writes writesIn = state.getWrites(new Placement(flowActivity, InOut.IN));
			Writes writesTmp = de.uni_stuttgart.iaas.bpel_d.algorithm.analysis.Activity.overWrite(writesIn, writesOut);
			writesOut.copyData(writesTmp);
			
			Activity.handleSuccessors(flowActivity, variableElement);
		}
		
		logger.exit("HandleEndOfFlow()");
		logger.debugObject("act", Utility.dumpEE(flowActivity));
	}
	
	private static Set<org.eclipse.bpel.model.Activity> findLeaves(org.eclipse.bpel.model.Flow flowActivity) {
//		System.err.println(">findLeaves: flowActivity = " + Utility.dumpEE(flowActivity));
		Set<org.eclipse.bpel.model.Activity> flowChildren = Utility.findChildrenAct((BPELExtensibleElement)flowActivity);
		Set<org.eclipse.bpel.model.Activity> flowDescendants = Utility.findDescendantsAct((BPELExtensibleElement)flowActivity);
		Set<org.eclipse.bpel.model.Activity> leaves = new HashSet<org.eclipse.bpel.model.Activity>();
		for (org.eclipse.bpel.model.Activity flowChild: flowChildren) {
			boolean sourceInFlow = false;
			EList<Source> sources = Utility.getSourceLinks(flowChild);
			for (Source source: sources) {
				Link link = source.getLink();
				EList<Target> linkTargets = link.getTargets();
				for (Target linkTarget: linkTargets) {
					org.eclipse.bpel.model.Activity targetActivity = linkTarget.getActivity();
					if (flowDescendants.contains(targetActivity)) {
						sourceInFlow = true;
						break;
					}
				}
			}
			if (!sourceInFlow) {
				leaves.add(flowChild);
			}
		}
		return leaves;
	}
}
