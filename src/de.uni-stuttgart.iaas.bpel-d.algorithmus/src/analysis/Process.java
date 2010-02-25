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

import infrastructure.InOut;
import infrastructure.Placement;
import infrastructure.State;
import infrastructure.VariableElement;
import infrastructure.Writes;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.eclipse.bpel.model.Activity;
import org.eclipse.bpel.model.Assign;
import org.eclipse.bpel.model.Catch;
import org.eclipse.bpel.model.Copy;
import org.eclipse.bpel.model.EventHandler;
import org.eclipse.bpel.model.Expression;
import org.eclipse.bpel.model.ExtensibleElement;
import org.eclipse.bpel.model.FaultHandler;
import org.eclipse.bpel.model.ForEach;
import org.eclipse.bpel.model.From;
import org.eclipse.bpel.model.FromPart;
import org.eclipse.bpel.model.FromParts;
import org.eclipse.bpel.model.Invoke;
import org.eclipse.bpel.model.OnAlarm;
import org.eclipse.bpel.model.OnEvent;
import org.eclipse.bpel.model.OnMessage;
import org.eclipse.bpel.model.PartnerLink;
import org.eclipse.bpel.model.Pick;
import org.eclipse.bpel.model.Query;
import org.eclipse.bpel.model.Receive;
import org.eclipse.bpel.model.Reply;
import org.eclipse.bpel.model.Scope;
import org.eclipse.bpel.model.TerminationHandler;
import org.eclipse.bpel.model.Throw;
import org.eclipse.bpel.model.To;
import org.eclipse.bpel.model.ToPart;
import org.eclipse.bpel.model.ToParts;
import org.eclipse.bpel.model.Variable;
import org.eclipse.bpel.model.impl.ToImpl;
import org.eclipse.bpel.model.messageproperties.Property;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.wst.wsdl.Part;

/**
 * Code for analysis of Process elements
 * @author yangyang Gao
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
		//line 2
		Set<Activity> allActivities = findAllActivities(process);		
		Collection<String> vars = findAllVariablesWrittenTo(allActivities);
		HashMap<String, Map<Placement, Writes>> resultData = new HashMap<String, Map<Placement, Writes>>();
		
		// FIXME Debug run
		for (String var: vars) {
			// fetch first variable from array for debug run
			//String var = (String) vars.toArray()[0];
//			String var = "orderInfo";
			myState.clearWrites();
			myState.clearFlags();
			analysis.Activity.handleActivity(process, var);
			System.err.print("res");
			System.err.println(myState.getWrites(new Placement(process, InOut.OUT)));
			System.err.println("--------------------");
			
			resultData.put(var, myState.getAllWrites());
		}
		
		outputResult(allActivities, vars, resultData);
	}

	private static void outputResult(Set<Activity> allActivities, Collection<String> allVars, HashMap<String, Map<Placement, Writes>> resultData) {
		SortedSet<String> sortedVars = new TreeSet<String>();
		sortedVars.addAll(allVars);
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
			System.err.println("==============================");
			Activity act = mapActivities.get(name);
			System.err.println(Utility.dumpEE(act));
			for (String var: sortedVars) {
				System.err.println(var);
				outPutVar(var, act, resultData);
			}
		}
		System.err.println("did not output...");
		for (Activity act: anonymousActivities) {
			System.err.println(Utility.dumpEE(act));
		}
		
		System.err.println();
		System.err.println("==================");
		for(String var: sortedVars) {
			System.err.println("==================");
			System.err.print("var: ");
			System.err.println(var);
			for (String name: sortedActivityNames) {
				Activity act = mapActivities.get(name);
				System.err.println(Utility.dumpEE(act));
				outPutVar(var, act, resultData);
			}
		}
	}

		private static void outPutVar(String var, Activity act, HashMap<String, Map<Placement, Writes>> resultData) {
			Map<Placement, Writes> curMap = resultData.get(var);
			Writes writesIn = curMap.get(new Placement(act, InOut.IN));
			if (writesIn == null) {
				System.err.println("!! not handled !!");
			} else {
				System.err.print("in:");
				System.err.println(writesIn.toString());
				Writes writesOut = curMap.get(new Placement(act, InOut.OUT));
				System.err.print("out:");
				System.err.println(writesOut.toString());
			}
			System.err.println("-----------------------------------");
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
	private static Collection<String> findAllVariablesWrittenTo(Collection<Activity> allActivities) {

		Set<String> res = new HashSet<String>();

		for (Activity act : allActivities) {
			if (act instanceof Assign) {
				Assign ass = (Assign) act;
				Boolean val = ass.getValidate();
				EList<Copy> a = ass.getCopy();
				for (Copy cp : a) {
					// From does not have to be treated, since we only care
					// about WRITTEN variables

					To t = cp.getTo();
					assert (t != null);

					org.eclipse.bpel.model.Variable var = t.getVariable();
					if (var != null) {
						Property pr = t.getProperty();
						if (pr != null) {
							// TODO: Quickhack, property should be converted to
							// XPath statement
							res.add(pr.getName());
						} else {
							// TODO: Quickhack, since t.getPart() is always null
							ToImpl toImpl = (ToImpl) t;
							String name;
							if (toImpl.partName != null) {
								name = var.getName() + "." + toImpl.partName;
							} else {
								name = var.getName();
							}
							// Part p = t.getPart();
							// if (p != null) {
							// name = var.getName() + "." + p.getName();
							// }
							Query q = t.getQuery();
							if (q != null) {
								String query = q.getValue();
								// Annahme: query == /a/b/c ohne Variable davor
								res.add(name + query);
							} else {
								res.add(name);
							}
						}
					}
					PartnerLink pl = t.getPartnerLink();
					if (pl != null) {
						res.add(pl.getName());
					}
					Expression ex = t.getExpression();
					if ((ex != null) && (ex.getBody() != null)){
						// TODO: Quickhack, getting too much variables - check
						// whether this is OK with the constraints of the paper
						res.add(ex.getBody().toString());
					}

				}
			} else if (act instanceof Invoke) {
				Invoke inv = (Invoke) act;
				Variable v = inv.getOutputVariable();
				if (v != null) {
					res.add(v.getName());
				}
				FromParts fps = inv.getFromParts();
				if (fps != null) {
					for (FromPart fp : fps.getChildren()) {
						v = fp.getToVariable();
						if (v != null) {
							res.add(v.getName());
						}
					}
				}
			} else if (act instanceof Receive) {
				Receive rec = (Receive) act;
				Variable r = rec.getVariable();
				if (r != null) {
					res.add(r.getName());
				}
				FromParts fps = rec.getFromParts();
				if (fps != null) {
					for (FromPart fp : fps.getChildren()) {
						r = fp.getToVariable();
						if (r != null) {
							res.add(r.getName());
						}
					}
				}
			} else if (act instanceof Throw) {
				Throw thr = (Throw) act;
				Variable t = thr.getFaultVariable();
				if (t != null) {
					res.add(t.getName());
				}
			} else if (act instanceof Pick) {
				Pick p = (Pick) act;
				EList<OnMessage> om = p.getMessages();
				for (OnMessage o : om) {
					Variable v = o.getVariable();
					if (v != null) {
						res.add(v.getName());
					}
					FromParts fps = o.getFromParts();
					if (fps != null) {
						for (FromPart fp : fps.getChildren()) {
							v = fp.getToVariable();
							if (v != null) {
								res.add(v.getName());
							}
						}
					}
				}
			} else if (act instanceof Scope) {
				Scope s = (Scope) act;
				EventHandler eh = s.getEventHandlers();
				if (eh != null) {
					EList<OnEvent> e = eh.getEvents();
					for (OnEvent ev : e) {
						Variable v = ev.getVariable();
						if (v != null) {
							res.add(v.getName());
						}
						FromParts fps = ev.getFromParts();
						if (fps != null) {
							for (FromPart fp : fps.getChildren()) {
								v = fp.getToVariable();
								if (v != null) {
									res.add(v.getName());
								}
							}
						}
					}
				}
				FaultHandler fh = s.getFaultHandlers();
				if (fh != null) {
					for (Catch c : fh.getCatch()) {
						Variable v = c.getFaultVariable();
						if (v != null) {
							res.add(v.getName());
						}
					}
					// CatchAll not needed, since BPEL doesn't allow a faultVariable there
				}
				// Terminationhandler and Compensationhandler to not write for themselves
			} else if (act instanceof ForEach) {
				ForEach fe = (ForEach) act;
				Variable v = fe.getCounterName();
				if (v != null) {
					res.add(v.getName());
				}
			}
		}
		return res;
	}
	
}
