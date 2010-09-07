package de.uni_stuttgart.iaas.bpel_d.algorithm.analysis;


import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.eclipse.bpel.model.Activity;

import de.uni_stuttgart.iaas.bpel_d.algorithm.infrastructure.InOut;
import de.uni_stuttgart.iaas.bpel_d.algorithm.infrastructure.Placement;
import de.uni_stuttgart.iaas.bpel_d.algorithm.infrastructure.Writes;

public class AnalysisResult {

	private Set<Activity> allActivities;
	private Collection<String> allVariableElementNames;
	private HashMap<String, Map<Placement, Writes>> resultData;

	public AnalysisResult(Set<Activity> allActivities,
			Collection<String> allVariableElementNames,
			HashMap<String, Map<Placement, Writes>> resultData) {
		this.allActivities = allActivities;
		this.allVariableElementNames = allVariableElementNames;
		this.resultData = resultData;
	}

	public Set<Activity> getAllActivities() {
		return allActivities;
	}
	
	public Collection<String> getAllVariableElementNames() {
		return allVariableElementNames;
	}
	
	public HashMap<String, Map<Placement, Writes>> getResultData() {
		return resultData;
	}
	
	/**
	 * Dumps the current contents to the console
	 */
	public void output() {
		SortedSet<String> sortedVars = new TreeSet<String>();
		sortedVars.addAll(allVariableElementNames);
		//SortedSet<Activity> sortedActivities = new TreeSet<Activity>();
		//sortedActivities.addAll(allActivities);
		Set<Activity> anonymousActivities = new HashSet<Activity>();
		HashMap<String,Activity> mapActivities = new HashMap<String,Activity>();
		for (Activity act: allActivities) {
			String name = act.getName();
			if (name != null)
				mapActivities.put(act.getName(), act);
			else
				anonymousActivities.add(act);
		}
		SortedSet<String> sortedActivityNames = new TreeSet<String>();
		sortedActivityNames.addAll(mapActivities.keySet());
		
		for (String name : sortedActivityNames) {
			System.out.println("==============================");
			Activity act = mapActivities.get(name);
			System.out.println(Utility.dumpEE(act));
			for (String var: sortedVars) {
				System.out.println(var);
				outPutVar(var, act, resultData);
			}
		}
		System.out.println("did not output...");
		for (Activity act: anonymousActivities) {
			System.out.println(Utility.dumpEE(act));
		}
		
		System.out.println();
		System.out.println("==================");
		for(String var: sortedVars) {
			System.out.println("==================");
			System.out.print("var: ");
			System.out.println(var);
			for (String name: sortedActivityNames) {
				Activity act = mapActivities.get(name);
				System.out.println(Utility.dumpEE(act));
				outPutVar(var, act, resultData);
			}
		}
	}

	private void outPutVar(String var, Activity act,
			HashMap<String, Map<Placement, Writes>> resultData) {
		Map<Placement, Writes> curMap = resultData.get(var);
		Writes writesIn = curMap.get(new Placement(act, InOut.IN));
		if (writesIn == null) {
			System.out.println("!! not handled !!");
		} else {
			System.out.print("in:");
			System.out.println(writesIn.toString());
			Writes writesOut = curMap.get(new Placement(act, InOut.OUT));
			System.out.print("out:");
			System.out.println(writesOut.toString());
		}
		System.out.println("-----------------------------------");
	}		
	
}
