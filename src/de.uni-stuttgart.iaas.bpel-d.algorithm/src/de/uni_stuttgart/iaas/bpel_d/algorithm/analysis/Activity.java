/**
 * Classes for general Activity handling
 * 
 * Copyright 2008 Sebastian Breier
 * Copyright 2009,2010 Yangyang Gao, Oliver Kopp
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


import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.eclipse.bpel.model.*;
import org.eclipse.bpel.model.Flow;
import org.eclipse.bpel.model.Process;
import org.eclipse.emf.common.util.EList;
import org.grlea.log.SimpleLogger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import de.uni_stuttgart.iaas.bpel.model.utilities.Utility;
import de.uni_stuttgart.iaas.bpel_d.algorithm.analysis.JoinVariableResolver;
import de.uni_stuttgart.iaas.bpel_d.algorithm.infrastructure.InOut;
import de.uni_stuttgart.iaas.bpel_d.algorithm.infrastructure.Placement;
import de.uni_stuttgart.iaas.bpel_d.algorithm.infrastructure.State;
import de.uni_stuttgart.iaas.bpel_d.algorithm.infrastructure.VariableElement;
import de.uni_stuttgart.iaas.bpel_d.algorithm.infrastructure.Writes;

/**
 * Class for HandleActivity and HandleSuccessors
 * @author yangyang Gao
 *
 */
public class Activity {

	private final static SimpleLogger logger = new SimpleLogger(Activity.class);

	/**
	 * Set A_basic of basic activities
	 */
	private static final Class<?>[] BASIC_ACTIVITIES = {
			Assign.class, Empty.class, Exit.class, ExtensionActivity.class,
			Invoke.class, Receive.class, Reply.class, Rethrow.class,
			Throw.class, Validate.class, Wait.class };
	private static BPELExtensibleElement eventActivity;
	
	/**
	 * Analyze activities
	 * See DIP-2726 p. 49
	 * @param act - !! has to be of type "BPELExtensibleElement", since "process" is NOT an activity
	 * @param ve
	 */
	public static void handleActivity(BPELExtensibleElement act, String ve) {
		logger.entry("handleActivity()");
		logger.debugObject("act", Utility.dumpEE(act));
		logger.debugObject("ve", ve);
		State myState = State.getInstance();
		if (act instanceof org.eclipse.bpel.model.Activity) {
			org.eclipse.bpel.model.Activity a = (org.eclipse.bpel.model.Activity) act;
			boolean parentHandled = isParentHandled(a);
			boolean allLinksHandled = areAllLinksHandled(a);
			boolean sequenceHandled = preSequenceHandled(a);
			if (!parentHandled || !allLinksHandled || !sequenceHandled) {
				logger.exit("handleActivity() - nothing to do yet.");
				return;
			}
		} else {
			if (!(act instanceof Process)) {
				logger.error("handleActivity called with an activity not being a real activity");
			}
		}
		myState.setStarted(act, true);
		if (act instanceof org.eclipse.bpel.model.Process) {
			// nothing todo here, everything has been done before
		} else {
			mergeData((org.eclipse.bpel.model.Activity) act, ve);
		}
		if (isBasicActivity(act)) {
			Basic.handleBasicActivity((org.eclipse.bpel.model.Activity)act, ve);			
		}
		//line 20 to 21
		else if (act instanceof Flow){
			de.uni_stuttgart.iaas.bpel_d.algorithm.analysis.Flow.HandleFlow((Flow)act, ve);
		}
		// line 22 to 23
		else if(act instanceof Scope){
			org.eclipse.bpel.model.Activity contained = Utility.getOnlyContainedActivity(act);
			handleActivity(contained, ve);
		}
		// line 24 to 25
		else if((act instanceof While)||(act instanceof RepeatUntil))
		{
			org.eclipse.bpel.model.Activity contained = Utility.getOnlyContainedActivity(act);
			handleActivity(contained, ve);
		}
		// line 26 to 28
		else if (act instanceof org.eclipse.bpel.model.Process) {
			org.eclipse.bpel.model.Activity contained = Utility.getOnlyContainedActivity(act);
			handleActivity(contained, ve);
			Writes writesOutAct = myState.getWrites(new Placement(act,InOut.OUT));
			Writes writesOutContained = myState.getWrites(new Placement(contained,InOut.OUT));
			writesOutAct.copyData(writesOutContained);
		} 
		//line 29 to 31
		else if (act instanceof Sequence){
			Sequence seq = (Sequence) act;
			org.eclipse.bpel.model.Activity contained = seq.getActivities().get(0);
			handleActivity(contained, ve);
		}
		//line 32 to 33
		else if (act instanceof CompensateScope){
			handleCompensateScope((CompensateScope)act, ve);
		}
		//line 34 to 35
		else if (act instanceof Pick){
			handlePick((Pick)act, ve);
		}
		//line 36 to 38
		else if (act instanceof ForEach){
			ForEach fe = (ForEach) act;
			handleActivity(fe.getActivity(), ve);
		}
		//line 39 to 40
		else if (act instanceof If){
			handleIf((If)act, ve);
		}
		logger.exit("handleActivity()");
		logger.debugObject("act", Utility.dumpEE(act));
		logger.debugObject("res", myState.getWrites(new Placement(act, InOut.OUT)));
	}
	
	/**
	 * Check if an activity is a basic type by checking against the BASIC_ACTIVITIES array
	 * @param act
	 * @return
	 */
	private static boolean isBasicActivity(BPELExtensibleElement act) {
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
	private static void mergeData(org.eclipse.bpel.model.Activity act, String ve) {
		logger.entry("mergeData()");
		logger.debugObject("act", Utility.dumpEE(act));
		logger.debugObject("ve", ve);

		State myState = State.getInstance();

		Writes writesInAct = myState.getWrites(new Placement(act, InOut.IN));
		
		// line 4 to 9
		BPELExtensibleElement parent = Utility.getParent(act);
		BPELExtensibleElement pred = null;
		
		if ( (parent instanceof Sequence) ) {
			// line 8
			Sequence s = (Sequence) parent;
			EList<org.eclipse.bpel.model.Activity> activities = s.getActivities();
			int index = activities.indexOf(act);
			if (index == 0) {
				// current activity is first activity -> parent data has to be copied (line 6)
			} else {
				pred = activities.get(index-1);
	            writesInAct.clear();
	            
	            Writes writesOutPreActivity = myState.getWrites(new Placement(pred, InOut.OUT));
	            logger.debugObject("pred", pred);
	            logger.debugObject("writesOutPreActivity", writesOutPreActivity);
				writesInAct.getPoss().addAll(writesOutPreActivity.getPoss());
				writesInAct.getPoss().addAll(writesOutPreActivity.getDis());
				writesInAct.getInv().addAll(writesOutPreActivity.getInv());
				writesInAct.setMbd(false);
	            
			}
		}

		// line 6
		if (pred == null) {
			// reached if activity not in a sequence or activity is first activity in a sequence
            writesInAct.clear();
            Writes writesInParent = myState.getWrites(new Placement(parent, InOut.IN));
            
			writesInAct.getPoss().addAll(writesInParent.getPoss());
			writesInAct.getInv().addAll(writesInParent.getInv());
			writesInAct.setMbd(false);
           
		}
		
		Set<BPELExtensibleElement> pWriters = new HashSet<BPELExtensibleElement>();
		Set<BPELExtensibleElement> dWriters = new HashSet<BPELExtensibleElement>();
		Set<BPELExtensibleElement> iWriters = new HashSet<BPELExtensibleElement>();
		boolean dState;

		// line 10 to 25
		EList<Target> linksIn = Utility.getTargetLinks(act);
		if (linksIn.size() > 0) {
			if ((hasOnlyOneIncomingLink(act) && !negatesLinkStatus(act))
					|| jcIsLogicalAnd((org.eclipse.bpel.model.Activity) act)) {
				for (Target t : linksIn) {
					Link l = t.getLink();
					Placement p = new Placement(l, InOut.OUT);
					Writes linkWritesOut = myState.getWrites(p);
					pWriters.addAll(linkWritesOut.getPoss());
					dWriters.addAll(linkWritesOut.getDis());
				}
			} else {
				// collect possible writers
				for (Target t : linksIn) {
					Link l = t.getLink();
					Placement p = new Placement(l, InOut.OUT);
					Writes linkWritesOut = myState.getWrites(p);
					pWriters.addAll(linkWritesOut.getPoss());
					pWriters.addAll(linkWritesOut.getDis());
				}
				
				// collect disabled writers
				// --> remove "disabled writes on all paths" from pos and add to dis
				Placement p = new Placement(linksIn.get(0), InOut.OUT);
				Set<BPELExtensibleElement> writers = myState.getWrites(p).getDis();
				for (BPELExtensibleElement writer : writers) {
					boolean disOnAllLinks = true;
					for (Target t : linksIn) {
						Link l = t.getLink();
						Placement p2 = new Placement(l, InOut.OUT);
						Set<BPELExtensibleElement> dis = myState.getWrites(p2)
								.getDis();
						if (!dis.contains(writer)) {
							disOnAllLinks = false;
						}
					}
					pWriters.remove(writer);
					dWriters.add(writer);
				}
			}
			for (Target t : linksIn) {
				Link l = t.getLink();
				Placement p = new Placement(l, InOut.OUT);
				iWriters.addAll(myState.getWrites(p).getInv());
			}
		}
		
		dState = determineMbdFromLinks(act);
		writesInAct.mergeWith(pWriters, dWriters, iWriters, dState);
		purgeDuplicatesOR(writesInAct);
		
		logger.exit("mergeData");
		logger.debugObject("writesInCurrent", writesInAct);
	}

	/**
	 * Purge duplications in Writes
	 * inv > dis > poss
	 * See DIP-2726 p. 59
	 * @param writesInCurrent
	 */
	private static void purgeDuplicatesOR(Writes writesOutCurrent) {
		Set<BPELExtensibleElement> poss, dis, inv;
		poss = writesOutCurrent.getPoss();
		dis = writesOutCurrent.getDis();
		inv = writesOutCurrent.getInv();
		poss.removeAll(inv);
		dis.removeAll(inv);
		poss.removeAll(dis);
	}
	/**
	 * purge duplication in Writes
	 * poss > dis > inv
	 * see DIP-2726 p.61
	 * @param writeInCurrent
	 */
	private static void purgeDuplicateXOR(Writes writesOutCurrent) {
		Set<BPELExtensibleElement> inv, dis, poss;
		inv = writesOutCurrent.getInv();
		dis = writesOutCurrent.getDis();
		poss = writesOutCurrent.getPoss();
		inv.removeAll(poss);
		dis.removeAll(poss);
		inv.removeAll(dis);
		
	}

	/**
	 * See DIP-2726 p. 52, line 22
	 * Determine the mbd state from incoming links
	 * @param act
	 * @return
	 */
	private static boolean determineMbdFromLinks(org.eclipse.bpel.model.Activity activity) {
		//3 teile für MegativLinkState.
		
		String jc = Utility.getJoinCondition(activity);
		
		// an activity without any incoming link may not be dead due to links
		// (but due to its enclosing structured activity. This is handled in a different method)
		EList<Target> targets = Utility.getTargetLinks(activity);
		if (targets.isEmpty())
			return false;
		
		if (jc == null) {
			// Default: OR-join, that means: last case of line 22
			// interpret join condition with negative mbd
			// or is true if at least one "parameter" is true
			// e.g. 1 or 0 == 1
			// not mbd turns the OR over all negated mbds to true
			// this result has to be negated again
			// that means, only ONE (not mbd) turns the whole thing to true
			//
			// if one link is NOT dead, the result is also NOT dead
			for (Target t: targets) {
				Writes w = State.getInstance().getWrites(new Placement(t.getLink(), InOut.OUT));
				if (!w.isMbd())
					return false;
			}
			// no link with mbd==false found -> activity may be dead
			return true;
		}
		
		//JoinVariableResolver jvr = new JoinVariableResolver();
		if (jcIsAlwaysTrue(jc)) {
			//case (i)
			return false;
		} else if (jcContainsNegations(jc)) {
			// case (ii)
			return true;
		} else {
			// case (iii)
			//Writes w = State.getInstance().getWrites(new Placement(activity,InOut.OUT));

			DocumentBuilder db;
			try {
				db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			} catch (ParserConfigurationException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
				return false;
			}
			Document d = db.newDocument();
			Element el = d.createElement("dummy");
			XPathFactory factory = XPathFactory.newInstance();
			XPath xPath = factory.newXPath();
			xPath.setXPathVariableResolver(new NegationVariableResolver(activity));
			XPathExpression xPathExpression;
			try {
				xPathExpression = xPath.compile(jc);
				return !(Boolean) xPathExpression.evaluate(el, XPathConstants.BOOLEAN);
			} catch (Exception e) {
				// Quickhack for dealing with exceptions
				e.printStackTrace();
				return false;
			}
		}
	}

	private static boolean jcContainsNegations(String jc) {
		if (jc == null) return false;
		// straight-forward hack, but should work in most cases
		//   works not, if jc uses XPath-Functions (e.g. $l1 could not($l2)), but this is ILLEGAL according to BPEL spec
		//   BPEL spec intends to allow logical expressions only (page 55) --> i.e. AND, OR, NOT
		return jc.toLowerCase().contains("not");
	}

	private static boolean jcIsAlwaysTrue(String jc) {
		if (jc==null) return false;
		XPathFactory factory = XPathFactory.newInstance();
		JoinVariableResolver jvr = new JoinVariableResolver();
		try {
			jvr.init(jc);

			DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document d = db.newDocument();
			Element el = d.createElement("dummy");
			XPath xPath = factory.newXPath();
			xPath.setXPathVariableResolver(jvr);

			XPathExpression xPathExpression;
			xPathExpression = xPath.compile(jc);
			while(!jvr.overflow() && ((Boolean) xPathExpression.evaluate(el, XPathConstants.BOOLEAN))) {
				jvr.next();			
			}
			
			// if an overflow happened, the whole Wertetabelle has been checked and EACH row returned true
			//   --> return true
			// otherwise --> return false
			return (jvr.overflow());
		} catch (Exception e) {
			e.printStackTrace();
			// TODO: quickhack
			return false;
		}
	}

	/**
	 * Check if join condition is a logical AND over all incoming links
     *
	 * @param activity
	 */
	private static boolean jcIsLogicalAnd(org.eclipse.bpel.model.Activity activity) {
		JoinVariableResolver jvr = new JoinVariableResolver();
		XPathFactory factory = XPathFactory.newInstance();
		try {
			String jc = Utility.getJoinCondition(activity);
			if (jc==null) return false;
			jvr.init(jc);

			DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document d = db.newDocument();
			Element el = d.createElement("dummy");
			XPath xPath = factory.newXPath();
			xPath.setXPathVariableResolver(jvr);

			XPathExpression xPathExpression;
				xPathExpression = xPath.compile(jc);
				while(!jvr.overflow() && (!(Boolean) xPathExpression.evaluate(el, XPathConstants.BOOLEAN))) {
					jvr.next();			
				}
				
				if (jvr.isMaxValue()) {
					// result depends on the evaluation of the expression under the condition that each variable is "true"
					// true and true and ... and true = true
					boolean result = (Boolean) xPathExpression.evaluate(el, XPathConstants.BOOLEAN);
					return result;
				} else {
					return false;
				}
			} catch (Exception e) {
				e.printStackTrace();
				// TODO: quickhack
				return false;
			}
	}
	
	/**
	 * Check whether the given join condition negates the incoming link
	 *  
	 * @pre activity has only one incoming link
	 * @note public because a JUnit test tests this method
	 */
	public static boolean negatesLinkStatus(String jc) {
		JoinVariableResolver jvr = new JoinVariableResolver();
		XPathFactory factory = XPathFactory.newInstance();
		try {
			if (jc==null) return false;
			jvr.init(jc);

			DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document d = db.newDocument();
			Element el = d.createElement("dummy");
			XPath xPath = factory.newXPath();
			xPath.setXPathVariableResolver(jvr);

			XPathExpression xPathExpression;
			xPathExpression = xPath.compile(jc);
			if (!(Boolean) xPathExpression.evaluate(el, XPathConstants.BOOLEAN)) {
				return false;
			}
			jvr.next();
			if (!jvr.isMaxValue()) {
				logger.error("activity has more than one variable in the join condition");
			}

			if ((Boolean) xPathExpression.evaluate(el, XPathConstants.BOOLEAN)) {
				return false;
			} else {
				return true;
			}
		} catch (Exception e) {
			logger.errorException(e);
			// TODO: quickhack
			return false;
		}
	}

	/**
	 * Check if the ONLY incoming link is negated 
	 * precondition: activity has only ONE incoming link
	 * 
	 * @param activity
	 */
	private static boolean negatesLinkStatus(org.eclipse.bpel.model.Activity activity) {
		logger.entry("negatesLinkStatus()");
		logger.debugObject("act", Utility.dumpEE(activity));
		String jc = Utility.getJoinCondition(activity);
		logger.exit("negatesLinkStatus()");
		return negatesLinkStatus(jc);
	}
		

	/**
	 * Check if act has only one incoming link
	 * @param act
	 * @return
	 */
	private static boolean hasOnlyOneIncomingLink(BPELExtensibleElement act) {
		EList<Target> linksIn = Utility.getTargetLinks(act);
		return linksIn.size() == 1;
	}

	/**
	 * Check if all incoming links have already been handled
	 * @param act
	 * @return
	 */
	private static boolean areAllLinksHandled(org.eclipse.bpel.model.Activity el) {
//		System.err.println(">areAllLinksHandled: el: " + el.getElement().getNodeName());
		EList<Target> targetList = Utility.getTargetLinks(el);
		if (targetList.size() == 0) {
			return true;
		}
		for (Target t: targetList) {
			org.eclipse.bpel.model.Activity sourceActivity = t.getLink().getSources().get(0).getActivity();
			if (!State.getInstance().isFinished(sourceActivity)) {
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
	private static boolean isParentHandled(BPELExtensibleElement act) {
//		System.err.println(">isParentHandled: " + act.getElement().getNodeName());
		BPELExtensibleElement parent = Utility.getParent(act);
		
		// BÖSER hack, eigentlich müsste Utility.getParent(act) konsistent zu dieser Abfrage sein und der Code überall entsprechend angepasst...
		
		if ((parent instanceof OnAlarm) ||
			(parent instanceof OnMessage) || 
			(parent instanceof ElseIf) ||
			(parent instanceof Else)) {
			parent = Utility.getParent(parent);
			if (parent instanceof EventHandler) {
				// hack, since onAlarm is used by both, pick and eventhandler
				// eventhandler can have no incoming links and are started only during scope handling
				return true;
			}
		}
		
		if (parent == null || State.getInstance().isStarted(parent) ||
			parent instanceof OnEvent ||
			parent instanceof Catch ||
			parent instanceof CatchAll ||
			parent instanceof CompensationHandler ||
			parent instanceof TerminationHandler) {
			return true;
		} else {
			return false;
		}
			
	}
	/**
	 * Check if the sequence activity has already been started with analysis
	 * @param
	 * @return true, if 
	 *   * act is not in a sequence or
	 *   * the first activity of the sequence or
	 *   * in a sequence and the predecessor has been handled
	 * false: otherwise
	 * 
	 */
	private static boolean preSequenceHandled(BPELExtensibleElement act) {
//		System.err.println(">isSequenceHandled: " + act.getElement().getNodeName());
		BPELExtensibleElement parent = Utility.getParent(act);
		if ( parent instanceof Sequence ) {
			Sequence s = (Sequence) parent;
			EList<org.eclipse.bpel.model.Activity> activities = s.getActivities();
			int index = activities.indexOf(act);
			if (index == 0) {
				// no action necessary, since Algorithm 1, page 49 checks handling of parent
				// we have to return true to enable the check by the other conditions
				return true;
			} else {
				// element is 2nd or later element --> check predecessor
				org.eclipse.bpel.model.Activity pred = activities.get(index-1);
				return State.getInstance().isFinished(pred);
			}
		} else {
			// a not nested in a sequence, just return true
			return true;
		}
	}
	/**
	 * Handle outgoing links, successors, parents after activity handling completed
	 * See DIP-2726 p. 51
	 * @param act
	 * @param variableElement
	 */
	public static void handleSuccessors(BPELExtensibleElement act, String variableElement) {
		logger.entry("handleSuccessors()");
		logger.debugObject("act", Utility.dumpEE(act));
		logger.debugObject("ve", variableElement);

		State state = State.getInstance();
		if (act instanceof org.eclipse.bpel.model.Activity) {
			state.setFinished((org.eclipse.bpel.model.Activity) act, true);
		}
		BPELExtensibleElement parent = Utility.getParent(act);

		// lines 5 to 7
		if ( parent instanceof Sequence ) {
			Sequence s = (Sequence) parent;
			EList<org.eclipse.bpel.model.Activity> activities = s.getActivities();
			int index = activities.indexOf(act);
			if (index == activities.size()-1) {
				// no action necessary, since current activity is the last activity of the sequnce
			} else {
				// element has a successor --> handle successor
				org.eclipse.bpel.model.Activity succ = activities.get(index+1);
				handleActivity(succ, variableElement);
			}
		}
		// line 8 to 9
		//parent = Utility.getParent(act);
		BPELExtensibleElement parentOfParent;
		parentOfParent = Utility.getParent(parent);

		// line 10 to 11
		if (parent instanceof Flow) {
			Flow flowActivity = (Flow) parent;
			de.uni_stuttgart.iaas.bpel_d.algorithm.analysis.Flow.HandleEndOfFlow(flowActivity, variableElement);
		}
		// line 12 to 13
		else if (parent instanceof Sequence) {
			Sequence s = (Sequence) parent;
			EList<org.eclipse.bpel.model.Activity> activities = s.getActivities();
			int index = activities.indexOf(act);
			if (index == activities.size() - 1) {
				// current activity is the last activity of the sequence
				handleEndOfSequence((Sequence) parent, variableElement);
			}

		}
		// line 14 to 15
		else if(parent instanceof Scope){
			logger.entry("Scope");
			logger.debug("Assuming: Main activity of scope is finished");
			// code should only be reached, if main activity of scope is finished (and not in other cases)
			// See page 76
			// thus, after the handling of the handlers, the successors of the scope should be handled
			handleScopeHandlers((Scope)parent, variableElement);
			//handleSuccessors(parent, variableElement); --> called by handleEndOfScope
			handleEndOfScope((Scope) parent, variableElement);
			logger.exit("Scope");
		}
		// line 16 to 17 were wrong
		// line 18 to 19
		else if((parent instanceof While)||(parent instanceof RepeatUntil)){
			handleEndOfLoop((org.eclipse.bpel.model.Activity)parent, variableElement);
		}
		// line 20 to 21
		else if(parentOfParent instanceof Pick){
			handleEndOfPick((Pick)parentOfParent, variableElement);
		}
		//line 22 to 23
		else if(parent instanceof ForEach){
			ForEach fe = (ForEach) parent;
			if(fe.getParallel()){
				Writes writesOutParent = state.getWrites(new Placement(parent,InOut.OUT));
				Writes writesOutActivity = state.getWrites(new Placement(act,InOut.OUT));
				writesOutParent.copyData(writesOutActivity);
			}
			else{
				handleEndOfLoop((org.eclipse.bpel.model.Activity)parent, variableElement);
			}
		}
		// line 28 to 29
		else if(parent instanceof If){
			handleEndOfIf((If)parent, variableElement);
		}
		// line 30 to 31
		else if((parentOfParent instanceof If) && !(parent instanceof If)){
			handleEndOfIf((If)parentOfParent, variableElement);
//		} else {
//			throw new IllegalStateException("No adequate parent found");
		}

		// FIX of Algorithm
		// If links are followed first, handleEndOfFlow proceeds more than once
		//   e.g. flow: (a -> b), then b and a both call handleEndOfFlow with a and b marked as finished
		//                        --> allLeaves of the flow are finished --> handleEndOfFlow proceeds
		// lines 2 to 4
		EList <Source> linksOut = Utility.getSourceLinks(act);
		for(Source l: linksOut){
			handleLink(l.getLink(), variableElement);
		}		

		logger.exit("handleSuccessors");
	}
	
	private static void handleScopeHandlers(org.eclipse.bpel.model.Scope scopeActivity, String variableElement) {
		handleFaultHandlers(scopeActivity, variableElement);
		handleEventHandlers(scopeActivity, variableElement);
		handleTerminationHandler(scopeActivity, variableElement);
		
	}
	
	private static void handleTerminationHandler(org.eclipse.bpel.model.Scope scopeActivity, String ve) {
		org.eclipse.bpel.model.Activity mainActivity = scopeActivity.getActivity();

		Set<org.eclipse.bpel.model.Activity> noScopes = new HashSet<org.eclipse.bpel.model.Activity>();
		Set<org.eclipse.bpel.model.Scope> scopes = new HashSet<org.eclipse.bpel.model.Scope>();
		Utility.localToScopeActivities(mainActivity, noScopes, scopes);
		
		Writes writesTmp;
		writesTmp = new Writes(new Placement(scopeActivity, InOut.TEMP));
		
		//lines 3 to 5
		for (org.eclipse.bpel.model.Activity a : noScopes) {
			State state = State.getInstance();
			Writes writesIn = state.getWrites(new Placement(a,InOut.IN));
			Writes writesOut = state.getWrites(new Placement(a, InOut.OUT));
			
			//TODO implement or DIP p82;
			
			writesTmp.copyData(writesIn);
			writesTmp.copyData(writesOut);
						
		}
		
		// lines 6 to 9
		for (org.eclipse.bpel.model.Scope a : scopes) {
			State state = State.getInstance();
			Writes writesOut = state.getWrites(new Placement(a, InOut.OUT));
			writesTmp.copyData(writesOut);
		}

		// line 10
		writesTmp.getInv().addAll(writesTmp.getPoss());
		writesTmp.setMbd(false);
		writesTmp.getDis().clear();
		
		// line 11
		purgeDuplicateXOR(writesTmp);
		
		TerminationHandler th = scopeActivity.getTerminationHandler();
		if (th != null) {
			org.eclipse.bpel.model.Activity a = th.getActivity();
			if (a != null) {
				Writes w = State.getInstance().getWrites(new Placement(th, InOut.IN));
				w.clear();
				w.copyData(writesTmp);
				handleActivity(a, ve);
			}
		}
	}

	private static void handleEventHandlers(org.eclipse.bpel.model.Scope scopeActivity, String ve) {
		org.eclipse.bpel.model.Activity mainActivity = scopeActivity.getActivity();
				
		Writes writesTmp;
		writesTmp = new Writes(new Placement(scopeActivity, InOut.TEMP));
		
		//lines 3 to 5
		Set<org.eclipse.bpel.model.Activity> desc;
		desc = Utility.findDescendantsAct(mainActivity);
		desc.add(mainActivity);
		for(org.eclipse.bpel.model.Activity a: desc){
			State state = State.getInstance();
			Writes writesIn = state.getWrites(new Placement(a,InOut.IN));
			Writes writesOut = state.getWrites(new Placement(a, InOut.OUT));
			writesTmp.copyData(writesIn);
			writesTmp.copyData(writesOut);
		}
		
		//line 6
		writesTmp.getInv().addAll(writesTmp.getPoss());
		writesTmp.setMbd(false);
		writesTmp.getDis().clear();
		
		//line 7
		purgeDuplicateXOR(writesTmp);
		
		//line 8 to 12
		
		EventHandler eh = scopeActivity.getEventHandlers();
		if (eh != null) {
			for (OnAlarm o : eh.getAlarm()) {
				if (o != null) {
					org.eclipse.bpel.model.Activity a = o.getActivity();
					Writes w = State.getInstance().getWrites(new Placement(o, InOut.IN));
					w.clear();
					w.copyData(writesTmp);
					handleActivity(a, ve);
				}
			}
			for (OnEvent o : eh.getEvents()) {
				if (o != null) {
					org.eclipse.bpel.model.Activity a = o.getActivity();
					Writes w = State.getInstance().getWrites(new Placement(o, InOut.IN));
					w.clear();
					
					if (de.uni_stuttgart.iaas.bpel_d.algorithm.analysis.Basic.doesWrite(o,ve)) {
						// we assume that the returned Writes is (emptyset, emptyset, emptyset, false)
						Writes writesTmpBranch = State.getInstance().getWrites(new Placement(o, InOut.TEMP));
						writesTmpBranch.getPoss().add(o);
						writesTmpBranch = overWrite(writesTmp, writesTmpBranch);
						w.copyData(writesTmpBranch);
					} else {
						w.copyData(writesTmp);
					}
					
					handleActivity(a, ve);
				}
			}
		}
	}
	
	

	private static void handleFaultHandlers(org.eclipse.bpel.model.Scope scopeActivity, String variableElement) {
		org.eclipse.bpel.model.Activity mainActivity = scopeActivity.getActivity();

		Set<org.eclipse.bpel.model.Activity> noScopes = new HashSet<org.eclipse.bpel.model.Activity>();
		Set<org.eclipse.bpel.model.Scope> scopes = new HashSet<org.eclipse.bpel.model.Scope>();
		Utility.localToScopeActivities(mainActivity, noScopes, scopes);
		
		Writes writesTmp;
		writesTmp = new Writes(new Placement(scopeActivity, InOut.TEMP));

		State state = State.getInstance();
		
		// lines 3 to 5
		for (org.eclipse.bpel.model.Activity a : noScopes) {
			Writes writesIn = state.getWrites(new Placement(a,InOut.IN));
			Writes writesOut = state.getWrites(new Placement(a, InOut.OUT));  
			writesTmp.copyData(writesIn);
			writesTmp.copyData(writesOut);
		}
		
		// lines 6 to 9
		for (org.eclipse.bpel.model.Scope a : scopes) {
			TerminationHandler th = a.getTerminationHandler();
			if(th != null){
			org.eclipse.bpel.model.Activity ta = th.getActivity();
			
			Writes w = state.getWrites(new Placement(ta, InOut.OUT));
			writesTmp.copyData(w);
			}
		}

		// line 10
		writesTmp.getInv().addAll(writesTmp.getPoss());
		writesTmp.setMbd(false);
		writesTmp.getDis().clear();
		
		
		// line 11
		purgeDuplicateXOR(writesTmp);
		
		// lines 12 to 16
		FaultHandler fh = scopeActivity.getFaultHandlers();
		if (fh != null) {
			for (Catch c : fh.getCatch()) {
				org.eclipse.bpel.model.Activity a = c.getActivity();
				Writes w = State.getInstance().getWrites(
						new Placement(c, InOut.IN));
				w.clear();
				
				if (de.uni_stuttgart.iaas.bpel_d.algorithm.analysis.Basic.doesWrite(c,variableElement)) {
					// we assume that the returned Writes is (emptyset, emptyset, emptyset, false)
					Writes writesTmpBranch = state.getWrites(new Placement(c, InOut.TEMP));
					writesTmpBranch.getPoss().add(c);
					writesTmpBranch = overWrite(writesTmp, writesTmpBranch);
					w.copyData(writesTmpBranch);
				} else {
					w.copyData(writesTmp);
				}
				
				//State.getInstance().setStarted(c, true);
				handleActivity(a, variableElement);
			}

			CatchAll c = fh.getCatchAll();
			if (c != null) {
				org.eclipse.bpel.model.Activity a = c.getActivity();
				Writes w = State.getInstance().getWrites(
						new Placement(c, InOut.IN));
				w.clear();
				w.copyData(writesTmp);
				//State.getInstance().setStarted(c, true);
				handleActivity(a, variableElement);
			}
		}
	}

	/**
	 * S. 67, Algorithm 11
	 * @param link - the link to handle
	 * @param ve - the current variable element
	 */
	private static void handleLink(Link link, String variableElement) {
		org.eclipse.bpel.model.Activity source;
		org.eclipse.bpel.model.Activity target;
		EList<Source> sources = link.getSources();
		// length of sources should be 1
		source = sources.get(0).getActivity();
		target = link.getTargets().get(0).getActivity();
		
		logger.entry("handleLink()");
		logger.debugObject("name", link.getName());
		logger.debugObject("source", Utility.dumpEE(source));
		logger.debugObject("target", Utility.dumpEE(target));
				
		BPELExtensibleElement sourceParent = Utility.getParent(source);
		BPELExtensibleElement sharedParent = Utility.getSharedParent(source, target);

		State state = State.getInstance();
		
		Writes writesIn = State.getInstance().getWrites(new Placement(link, InOut.IN));
		Writes writesOut = State.getInstance().getWrites(new Placement(link, InOut.OUT));
		
		Writes writesSourceAct = state.getWrites(new Placement(source,InOut.OUT)); 
		writesIn.copyData(writesSourceAct);
		writesOut.copyData(writesIn);
		
		if (sharedParent != sourceParent) {
			BPELExtensibleElement parent = sourceParent;
			while(parent != sharedParent){
				Writes writesTemp = overWriteNoEndDeadPath(state.getWrites(new Placement(parent, InOut.IN)),
												   writesOut);
				writesOut.clear();
				writesOut.copyData(writesTemp);
				parent = Utility.getParent(parent);
			}
		}
		
		// TODO check whether jcIsAlwaysTrue really works for transition conditions
		// problem might be custom XPath functions, arithmetics, ...
		// Next version should check for the absence of functions / expressions / ...
		String tc = Utility.getTransitionCondition(link);
		boolean alwaysTrue = (tc == null);
		alwaysTrue = alwaysTrue || (tc == "");
		alwaysTrue = alwaysTrue || (tc == "true()");
		
		Boolean mbd = writesOut.isMbd() || !alwaysTrue;
		writesOut.setMbd(mbd);
		
		handleActivity(target, variableElement);		

		logger.exit("handleLink()");
		logger.debugObject("name", link.getName());
		logger.debugObject("source", Utility.dumpEE(source));
		logger.debugObject("target", Utility.dumpEE(target));
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
		logger.entry("overWrite()");
		logger.debugObject("writesIn", writesIn);
		logger.debugObject("writesTmp", writesTmp);
		
		//Placement placement = new Placement(writesIn.getPlacement().getElement(), InOut.OUT);
		Writes writesOut = new Writes();
		if(writesTmp.getPoss().isEmpty()){
			writesOut.copyData(writesIn);
		}
		else if( writesIn.isMbd() ){
			writesOut.getPoss().addAll(writesTmp.getPoss());
			writesOut.getPoss().addAll(writesTmp.getDis());
			writesOut.getDis().addAll(writesIn.getDis());
			writesOut.getDis().addAll(writesIn.getPoss());
			writesOut.getInv().addAll(writesIn.getInv());
			writesOut.getInv().addAll(writesTmp.getInv());
			Set<BPELExtensibleElement> tmp = new HashSet<BPELExtensibleElement>();
			tmp.addAll(writesTmp.getPoss());
			tmp.addAll(writesTmp.getDis());
			Set<BPELExtensibleElement> writesInPoss = writesIn.getPoss(); 
			tmp.retainAll(writesInPoss);
			writesOut.setMbd(!tmp.isEmpty());
			
		}
		else { // writesOut is false in this case
			writesOut.getPoss().addAll(writesTmp.getPoss());
			writesOut.getPoss().addAll(writesTmp.getDis());
			writesOut.getDis().addAll(writesIn.getDis());
			writesOut.getInv().addAll(writesIn.getPoss());
			writesOut.getInv().addAll(writesIn.getInv());
			writesOut.getInv().addAll(writesTmp.getInv());
			writesOut.setMbd(false);	
			
		}
		purgeDuplicateXOR(writesOut);
		
		logger.exit("overWrite()");
		return writesOut;
	}
	/**
	 * Method "overWriteNoEnDeadPath"
	 * Update writesIn tuple with data from writesTmp tuple
	 * Returns a new tuple having an updated state
	 * See DIP-2726 p. 69 algo 4
	 * @param writesIn
	 * @param writesTmp
	 * @return
	 */
	public static Writes overWriteNoEndDeadPath(Writes writesIn, Writes writesTmp) {
		logger.entry("overWriteNoEndDeadPath()");
		logger.debugObject("writesIn", writesIn);
		logger.debugObject("writesTmp", writesTmp);

		//Placement placement = new Placement(writesIn.getPlacement().getElement(), InOut.OUT);
		Writes writesOut = new Writes();
		if(writesTmp.getPoss().isEmpty()){
			writesOut.copyData(writesIn);
		} else if( writesIn.isMbd() ){
			writesOut.getPoss().addAll(writesTmp.getPoss());
			writesOut.getDis().addAll(writesIn.getDis());
			writesOut.getDis().addAll(writesIn.getPoss());
			writesOut.getDis().addAll(writesTmp.getDis());
			writesOut.getInv().addAll(writesIn.getInv());
			writesOut.getInv().addAll(writesTmp.getInv());
			Set<BPELExtensibleElement> tmp = new HashSet<BPELExtensibleElement>();
			tmp.retainAll(writesTmp.getPoss());
			tmp.retainAll(writesIn.getPoss());
			writesOut.setMbd(!tmp.isEmpty());
			
		} else { // writesIn.isMbd() is false
			writesOut.getPoss().addAll(writesTmp.getPoss());
			writesOut.getDis().addAll(writesIn.getDis());
			writesOut.getDis().addAll(writesTmp.getDis());
			writesOut.getInv().addAll(writesIn.getInv());
			writesOut.getInv().addAll(writesTmp.getInv());
			writesOut.getInv().addAll(writesIn.getPoss());
			writesOut.setMbd(false);	
		}
		purgeDuplicateXOR(writesOut);
		
		logger.exit("overWriteNoEndDeadPath()");
		return writesOut;
	}
	/*
	 * @param succ
	 * @param ve
	 */
	public static void handleEndOfSequence(Sequence sequence, String ve){
		EList<org.eclipse.bpel.model.Activity> activities = sequence.getActivities();
		org.eclipse.bpel.model.Activity lastActivity = activities.get(activities.size() - 1);

		State state = State.getInstance();
		Writes writesOut = state.getWrites(new Placement(sequence, InOut.OUT));
		Writes writesOutLastActivity = state.getWrites(new Placement(lastActivity, InOut.OUT));
		
		writesOut.clear();
		writesOut.copyData(writesOutLastActivity);

		Writes newWrites = overWrite(state.getWrites(new Placement(sequence, InOut.IN)), writesOut);
		writesOut.clear();
		writesOut.copyData(newWrites);

		handleSuccessors(sequence, ve);
	}
	/*
	 * @param loop
	 * @param ve
	 */
	
	public static void handleEndOfLoop(org.eclipse.bpel.model.Activity loop, String ve){
		org.eclipse.bpel.model.Activity child;
		if (loop instanceof ForEach) {
			child = ((ForEach) loop).getActivity();
		} else if (loop instanceof RepeatUntil) {
			child = ((RepeatUntil) loop).getActivity();
		} else {
			assert (loop instanceof While);
			child = ((While) loop).getActivity();
		}
		
		State state = State.getInstance();
		
		Writes writesInLoop = state.getWrites(new Placement(loop, InOut.IN));
		Writes writesOutChild = state.getWrites(new Placement(child, InOut.OUT));
		
		Writes writesOutTmp = overWrite(writesInLoop, writesOutChild);
		Writes writesOutLoop = state.getWrites(new Placement(loop, InOut.OUT));
		writesOutLoop.clear();
		writesOutLoop.copyData(writesOutTmp);
	
		
		if (writesInLoop.equals(writesOutLoop)) {
			handleSuccessors(loop,ve);
		}
		else{
			writesInLoop.clear();
			writesInLoop.copyData(writesOutLoop);
	
			// clear all started and finished flags of all child activities
			Set<org.eclipse.bpel.model.Activity> desc;
			desc = Utility.findDescendantsAct(loop);
			for(org.eclipse.bpel.model.Activity a: desc){
				state.setStarted(a, false);
				state.setFinished(a, false);
			}
				
			handleActivity(child, ve);
		}
	}
	/*
	 * @param pick
	 * @param ve
	 */
	public static void handlePick(org.eclipse.bpel.model.Pick pickActivity, String ve){
	
		State state = State.getInstance();
		Set<org.eclipse.bpel.model.BPELExtensibleElement> branches = new HashSet<org.eclipse.bpel.model.BPELExtensibleElement>();
				
		for (OnMessage m: pickActivity.getMessages()) {
			branches.add(m);
		}
		for (OnAlarm a: pickActivity.getAlarm()) {
			branches.add(a);
		}
		//line 4	
		Writes WritesInPickActivity = state.getWrites(new Placement(pickActivity, InOut.IN));
		for (org.eclipse.bpel.model.BPELExtensibleElement e: branches) {
			
			Writes writesInBranch = state.getWrites(new Placement(e, InOut.IN));
            writesInBranch.clear();
            
			writesInBranch.getPoss().addAll(WritesInPickActivity.getPoss());
			writesInBranch.getInv().addAll(WritesInPickActivity.getInv());
			writesInBranch.setMbd(false);
			
			if (de.uni_stuttgart.iaas.bpel_d.algorithm.analysis.Basic.doesWrite(e,ve)) {
				// we assume that the returned Writes is (emptyset, emptyset, emptyset, false)
				Writes writesTmpBranch = state.getWrites(new Placement(e, InOut.TEMP));
				writesTmpBranch.getPoss().add(e);
				writesTmpBranch = overWrite(writesInBranch, writesTmpBranch);
				writesInBranch.clear();
				writesInBranch.copyData(writesTmpBranch);
			}
			if (e instanceof OnAlarm) {
				handleActivity(((OnAlarm) e).getActivity(), ve);
			} else {
				assert(e instanceof OnMessage);
				handleActivity(((OnMessage) e).getActivity(), ve);
			}
		}
	}
	
	/*
	 * @param pick
	 * @param ve
	 */
	public static void handleEndOfPick(org.eclipse.bpel.model.Pick pickActivity, String ve){
		State state = State.getInstance();
		Set<BPELExtensibleElement> branchActivities = new HashSet<BPELExtensibleElement>();
				
		for (OnMessage m: pickActivity.getMessages()) {
			branchActivities.add(m.getActivity());
		}
		for (OnAlarm a: pickActivity.getAlarm()) {
			branchActivities.add(a.getActivity());
		}
		boolean allFinished = true;
		for (BPELExtensibleElement e: branchActivities) {
			if (!State.getInstance().isFinished(e)) {
				allFinished = false;
				break;
				}
		}
		Writes writesInPick = state.getWrites(new Placement(pickActivity, InOut.IN));
		Writes writesOutPick = state.getWrites(new Placement(pickActivity, InOut.OUT));
		if (allFinished) {					
			// line 3
			for (BPELExtensibleElement e: branchActivities) {
					Writes writesOutTmp = state.getWrites(new Placement(e, InOut.OUT));
					writesOutPick.copyData(writesOutTmp);
			}
			// line 4
			Writes writesTmp;
			writesTmp = overWrite(writesInPick, writesOutPick);
			writesInPick.clear();
			writesInPick.copyData(writesTmp);
			handleSuccessors(pickActivity, ve);
		}	
	}
	/*
	 * @param if
	 * @param ve
	 */
	public static void handleIf(org.eclipse.bpel.model.If ifActivity, String ve){
		org.eclipse.bpel.model.Activity mainActivity = ifActivity.getActivity();
		State state = State.getInstance();
		Writes writesInIf = state.getWrites(new Placement(ifActivity, InOut.IN));		
		handleActivity(mainActivity, ve);
		
		for(ElseIf e: ifActivity.getElseIf()){
			Writes writesInElse = state.getWrites(new Placement(e, InOut.IN));
			writesInElse.clear();
			writesInElse.copyData(writesInIf);
			handleActivity(e.getActivity(), ve);			
		}
		
		Else e = ifActivity.getElse();
		if(e !=null){
		Writes writesInElse = state.getWrites(new Placement(e, InOut.IN));
		writesInElse.clear();
		writesInElse.copyData(writesInIf);
		handleActivity(e.getActivity(), ve);
		}
			
	}
	/*
	 * @param if
	 * @param ve
	 */
	public static void handleEndOfIf(org.eclipse.bpel.model.If ifActivity, String ve){
		Set<BPELExtensibleElement> allElement = new HashSet<BPELExtensibleElement>();
		org.eclipse.bpel.model.Activity mainActivity = ifActivity.getActivity();
		allElement.add(mainActivity);

		for (ElseIf ei: ifActivity.getElseIf()) {
			allElement.add(ei.getActivity());
		}
		
		Else e = ifActivity.getElse();
		if(e!=null){
		allElement.add(e.getActivity());
		}
		State state = State.getInstance();

		// line 3
		boolean allFinished = true;
		for (BPELExtensibleElement ee: allElement) {
			if (!State.getInstance().isFinished(ee)) {
				allFinished = false;
				break;
				}
		}	
		
		if (allFinished) {	
			Writes writesOutIf = state.getWrites(new Placement(ifActivity, InOut.OUT));	
			Writes writesInIf = state.getWrites(new Placement(ifActivity, InOut.IN));	

			// line 4
			for (BPELExtensibleElement ee: allElement) {
				Writes writesOutTmp = state.getWrites(new Placement(ee, InOut.OUT));
				writesOutIf.copyData(writesOutTmp);
			}
			// line 6
			Writes writesOutTmpIf = overWrite(writesInIf, writesOutIf);
			writesOutIf.clear();
			writesOutIf.copyData(writesOutTmpIf);
			handleSuccessors(ifActivity, ve);
		}
	}
	
	/**
	 * @param Scope
	 * @param ve
	 */
	public static void handleEndOfScope(org.eclipse.bpel.model.Scope scopeActivity, String ve){
	    //line 2
		org.eclipse.bpel.model.Activity mainActivity = scopeActivity.getActivity();

		if (!State.getInstance().isFinished(mainActivity))
			return;
		
		Set<BPELExtensibleElement> allElements = new HashSet<BPELExtensibleElement>();
		
		allElements.add(mainActivity);
		
		//line 3
		EventHandler eh = scopeActivity.getEventHandlers();
		if (eh != null) {
			for (OnAlarm a : eh.getAlarm()) {
				allElements.add(a.getActivity());
				assert(State.getInstance().isFinished(eventActivity));
			}
			for (OnEvent e : eh.getEvents()) {
				allElements.add(e.getActivity());
				assert(State.getInstance().isFinished(eventActivity));
			}
		}
		
		//line 4
		FaultHandler fh = scopeActivity.getFaultHandlers();
		if (fh != null) {
			for (Catch c : fh.getCatch()) {
				allElements.add(c.getActivity());
				assert(State.getInstance().isFinished(c.getActivity()));
			}
			CatchAll ca = fh.getCatchAll();
			if (ca != null) {
				allElements.add(ca.getActivity());
				assert(State.getInstance().isFinished(ca.getActivity()));
			}
		}
		
		//line 5
		TerminationHandler th = scopeActivity.getTerminationHandler();
		if (th != null) {
			assert(State.getInstance().isFinished(th.getActivity()));
			allElements.add(th.getActivity());
		}
		
		// line 6 to 9
		// execution reaches this point if all are finished - otherwise one of the assertions broke
		Writes writesInScope = State.getInstance().getWrites(new Placement(scopeActivity, InOut.IN));
		Writes writesOutScope = State.getInstance().getWrites(new Placement(scopeActivity, InOut.OUT));
		
		for (BPELExtensibleElement e: allElements) {
			Writes writesOut = State.getInstance().getWrites(new Placement(e, InOut.OUT));
			writesOutScope.copyData(writesOut);
		}

		// overwrite
		Writes writesOutTmpScope = overWrite(writesInScope, writesOutScope);
		writesOutScope.clear();
		writesOutScope.copyData(writesOutTmpScope);
		
		handleSuccessors(scopeActivity, ve);
	}
	/*
	 * @param comp
	 * @param ve
	 */
	public static void handleCompensateScope(CompensateScope compActivity, String ve){
		// line 8 to 9;
		
		org.eclipse.bpel.model.Scope scopeActivity = (Scope) compActivity.getTarget();
		if(scopeActivity != null){
		CompensationHandler ch = scopeActivity.getCompensationHandler();
		org.eclipse.bpel.model.Activity compensationActivity = ch.getActivity();
		State state = State.getInstance();
		// line 10	
		Writes WritesOutScope = state.getWrites(new Placement(scopeActivity, InOut.OUT));
		Writes writesInCompElement = state.getWrites(new Placement(ch, InOut.IN));
	    writesInCompElement.clear();
	            
		writesInCompElement.getPoss().addAll(WritesOutScope.getPoss());
		writesInCompElement.getInv().addAll(WritesOutScope.getInv());
		writesInCompElement.setMbd(false);
		handleActivity(compensationActivity, ve);
		// line 12
		Writes WritesInComp = state.getWrites(new Placement(compActivity, InOut.IN));
		Writes WritesOutCompElement = state.getWrites(new Placement(compensationActivity, InOut.OUT));
		Writes writesInTmp = overWrite(WritesInComp, WritesOutCompElement);
		WritesInComp.clear();
		WritesInComp.copyData(writesInTmp);				
		//line 19		
		Writes WritesOutComp = state.getWrites(new Placement(compActivity, InOut.OUT));
		WritesOutComp.copyData(WritesInComp);
		handleSuccessors(compActivity, ve);
		}
	}

}
