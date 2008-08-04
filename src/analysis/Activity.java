/**
 * Classes for general Activity handling
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
import infrastructure.Variable;
import infrastructure.Writes;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.bpel.model.Assign;
import org.eclipse.bpel.model.Condition;
import org.eclipse.bpel.model.Empty;
import org.eclipse.bpel.model.Exit;
import org.eclipse.bpel.model.ExtensibleElement;
import org.eclipse.bpel.model.ExtensionActivity;
import org.eclipse.bpel.model.Flow;
import org.eclipse.bpel.model.Invoke;
import org.eclipse.bpel.model.Link;
import org.eclipse.bpel.model.Receive;
import org.eclipse.bpel.model.Reply;
import org.eclipse.bpel.model.Rethrow;
import org.eclipse.bpel.model.Target;
import org.eclipse.bpel.model.Throw;
import org.eclipse.bpel.model.Validate;
import org.eclipse.bpel.model.Wait;
import org.eclipse.emf.common.util.EList;

/**
 * Class for HandleActivity and HandleSuccessors
 * @author Sebastian Breier
 *
 */
public class Activity {

	/**
	 * Set A_basic of basic activities
	 */
	private static final Class<?>[] BASIC_ACTIVITIES = {
			Assign.class, Empty.class, Exit.class, ExtensionActivity.class,
			Invoke.class, Receive.class, Reply.class, Rethrow.class,
			Throw.class, Validate.class, Wait.class };
	
	/**
	 * Analyze activities
	 * See DIP-2726 p. 49
	 * @param act
	 * @param ve
	 */
	public static void handleActivity(ExtensibleElement act, Variable ve) {
		System.err.println(">handleActivity: act = " + Utility.dumpEE(act) + ", ve = " + ve);
		State myState = State.getInstance();
		boolean parentHandled = isParentHandled(act);
		boolean allLinksHandled = areAllLinksHandled(act);
		// FIXME: Sequence not implemented yet
		if (!parentHandled || !allLinksHandled) {
			return;
		}
		myState.setStarted(act, true);
		if (act instanceof org.eclipse.bpel.model.Process) {
			Placement p = new Placement(act, InOut.IN);
			myState.newWrites(p);
		} else {
			Placement p = new Placement(act, InOut.IN);
			myState.newWrites(p);
			mergeData(act, ve);
		}
		if (isBasicActivity(act)) {
			Basic.handleBasicActivity((org.eclipse.bpel.model.Activity)act, ve);
		} else if (act instanceof org.eclipse.bpel.model.Process) {
			org.eclipse.bpel.model.Activity contained = Utility.getOnlyContainedActivity(act);
			handleActivity(contained, ve);
		} else if (act instanceof Flow) {
			Flow flowActivity = (Flow)act;
			analysis.Flow.HandleFlow(flowActivity, ve);
		}
	}
	
	/**
	 * Check if an activity is a basic type by checking against the BASIC_ACTIVITIES array
	 * @param act
	 * @return
	 */
	private static boolean isBasicActivity(ExtensibleElement act) {
//		System.err.println(">isBasicActivity: act = " + Utility.dumpEE(act));
		Class<?>[] possibleActivityTypes = act.getClass().getInterfaces();
		List<Class<?>> basicClasses = Arrays.asList(BASIC_ACTIVITIES);
		for (Class<?> possibleActivityType: possibleActivityTypes) {
			if (basicClasses.contains(possibleActivityType)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * MergeData algorithm
	 * See DIP-2726 p. 52
	 * @param act
	 * @param ve
	 */
	private static void mergeData(ExtensibleElement act, Variable ve) {
		// FIXME: Seq not implemented
//		System.err.println(">mergeData: act = " + Utility.dumpEE(act) + ", ve = " + ve);
		State myState = State.getInstance();
		Writes w = myState.getWrites(new Placement(act, InOut.IN));
		w.copyFromParent();
		Set<ExtensibleElement> pWriters = new HashSet<ExtensibleElement>();
		Set<ExtensibleElement> dWriters = new HashSet<ExtensibleElement>();
		Set<ExtensibleElement> iWriters = new HashSet<ExtensibleElement>();
		boolean dState;
		EList<Target> linksIn = Utility.getTargetLinks(act);
		if ((hasOnlyOneIncomingLink(act) && !negatesLinkStatus(act)) || jcIsLogicalAnd(act)) {
			for (Target t: linksIn) {
				Link l = t.getLink();
				Placement p = new Placement(l, InOut.OUT);
				Writes linkWritesOut = myState.getWrites(p);
				pWriters.addAll(linkWritesOut.getPoss());
				dWriters.addAll(linkWritesOut.getDis());
			}
		} else {
			for (Target t: linksIn) {
				Link l = t.getLink();
				Placement p = new Placement(l, InOut.OUT);
				Writes linkWritesOut = myState.getWrites(p);
				pWriters.addAll(linkWritesOut.getPoss());
				pWriters.addAll(linkWritesOut.getDis());
			}
			Placement p = new Placement(linksIn.get(0), InOut.OUT);
			Set<ExtensibleElement> writers = myState.getWrites(p).getDis();
			for (ExtensibleElement writer: writers) {
				boolean disOnAllLinks = true;
				for (Target t: linksIn) {
					Link l = t.getLink();
					Placement p2 = new Placement(l, InOut.OUT);
					Set<ExtensibleElement> dis = myState.getWrites(p2).getDis();
					if (!dis.contains(writer)) {
						disOnAllLinks = false;
					}
				}
				pWriters.remove(writer);
				dWriters.add(writer);
			}
		}
		for (Target t: linksIn) {
			Link l = t.getLink();
			Placement p = new Placement(l, InOut.OUT);
			iWriters.addAll(myState.getWrites(p).getInv());
		}
		dState = determineMbdFromLinks(act);
		Placement p = new Placement(act, InOut.IN);
		Writes writesInCurrent = myState.getWrites(p);
		writesInCurrent.mergeWith(pWriters, dWriters, iWriters, dState);
		purgeDuplicatesOR(writesInCurrent);
//		System.err.println("<mergeData: writesInCurrent = " + writesInCurrent);
	}

	/**
	 * Purge duplications in Writes
	 * inv > dis > poss
	 * See DIP-2726 p. 59
	 * @param writesInCurrent
	 */
	private static void purgeDuplicatesOR(Writes writesInCurrent) {
		Set<ExtensibleElement> poss, dis, inv;
		poss = writesInCurrent.getPoss();
		dis = writesInCurrent.getDis();
		inv = writesInCurrent.getInv();
		poss.removeAll(inv);
		dis.removeAll(inv);
		poss.removeAll(dis);
	}

	/**
	 * Determine the mbd state from incoming links
	 * @param act
	 * @return
	 */
	private static boolean determineMbdFromLinks(ExtensibleElement element) {
		System.err.println(">determineMbdFromLinks STUB: element = " + Utility.dumpEE(element));
		return false;
	}

	/**
	 * Check if join condition is a logical AND over all incoming links
	 * FIXME: STUB!
	 * @param element
	 * @return
	 */
	private static boolean jcIsLogicalAnd(ExtensibleElement element) {
		// FIXME: No idea what conditions/expressions look like right now
//		System.err.println(">jcIsLogicalAnd STUB: element: " + element.getElement().getNodeName());
		if (Utility.getJoinCondition(element) == null) {
//			System.err.println("<jcIsLogicalAnd STUB: true");
			return true;
		}
//		System.err.println("<jcIsLogicalAnd STUB: false");
		return false;
	}

	/**
	 * Check if the only incoming link is negated
	 * @param el
	 * @return
	 */
	private static boolean negatesLinkStatus(ExtensibleElement el) {
		// FIXME: No idea what conditions/expressions look like right now
		System.err.println(">negatesLinkStatus STUB: el = " + Utility.dumpEE(el));
		Condition jc = Utility.getJoinCondition(el);
		System.err.println(jc);
		return false;
	}

	/**
	 * Check if act has only one incoming link
	 * @param act
	 * @return
	 */
	private static boolean hasOnlyOneIncomingLink(ExtensibleElement act) {
		EList<Target> linksIn = Utility.getTargetLinks(act);
		return linksIn.size() == 1;
	}

	/**
	 * Check if all incoming links have already been handled
	 * @param act
	 * @return
	 */
	private static boolean areAllLinksHandled(ExtensibleElement el) {
//		System.err.println(">areAllLinksHandled: el: " + el.getElement().getNodeName());
		EList<Target> targetList = Utility.getTargetLinks(el);
		if (targetList.size() == 0) {
			return true;
		}
		for (Target t: targetList) {
			org.eclipse.bpel.model.Activity targetActivity = t.getActivity();
			if (!State.getInstance().isFinished(targetActivity)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Check if the parent activity has already been started with analysis
	 * @param act
	 * @return
	 */
	private static boolean isParentHandled(ExtensibleElement act) {
//		System.err.println(">isParentHandled: " + act.getElement().getNodeName());
		ExtensibleElement parent = Utility.getParent(act);
		if (parent == null || State.getInstance().isStarted(parent)) {
			return true;
		}
		return false;
	}

	/**
	 * Handle outgoing links, successors, parents after activity handling completed
	 * See DIP-2726 p. 51
	 * @param act
	 * @param ve
	 */
	public static void handleSuccessors(ExtensibleElement act, Variable ve) {
		System.err.println(">handleSuccessors STUB: act = " + Utility.dumpEE(act) + ", ve = " + ve);
		// FIXME
	}

	/**
	 * Method "overWrite"
	 * Update writesIn tuple with data from writesTmp tuple
	 * Returns a new tuple having an updated state
	 * See DIP-2726 p. 56 algo 4
	 * @param writesIn
	 * @param writesTmp
	 * @return
	 */
	public static Writes overWrite(Writes writesIn, Writes writesTmp) {
		System.err.println(">overWrite STUB: writesIn = " + writesIn + ", writesTmp = " + writesTmp);
		// FIXME
		Set<ExtensibleElement> emptySet = Collections.emptySet();
		return new Writes(emptySet, emptySet, emptySet, false);
	}
}
