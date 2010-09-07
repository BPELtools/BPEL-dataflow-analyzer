/**
 * Analysis of basic activities
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
package de.uni_stuttgart.iaas.bpel_d.algorithm.analysis;


import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.bpel.model.Activity;
import org.eclipse.bpel.model.Assign;
import org.eclipse.bpel.model.Catch;
import org.eclipse.bpel.model.CatchAll;
import org.eclipse.bpel.model.Copy;
import org.eclipse.bpel.model.ElseIf;
import org.eclipse.bpel.model.EventHandler;
import org.eclipse.bpel.model.Expression;
import org.eclipse.bpel.model.ExtensibleElement;
import org.eclipse.bpel.model.FaultHandler;
import org.eclipse.bpel.model.ForEach;
import org.eclipse.bpel.model.FromPart;
import org.eclipse.bpel.model.FromParts;
import org.eclipse.bpel.model.If;
import org.eclipse.bpel.model.Invoke;
import org.eclipse.bpel.model.OnAlarm;
import org.eclipse.bpel.model.OnEvent;
import org.eclipse.bpel.model.OnMessage;
import org.eclipse.bpel.model.PartnerLink;
import org.eclipse.bpel.model.Pick;
import org.eclipse.bpel.model.Query;
import org.eclipse.bpel.model.Receive;
import org.eclipse.bpel.model.Throw;
import org.eclipse.bpel.model.To;
import org.eclipse.bpel.model.ToPart;
import org.eclipse.bpel.model.ToParts;
import org.eclipse.bpel.model.Variable;
import org.eclipse.bpel.model.impl.ToImpl;
import org.eclipse.bpel.model.messageproperties.Property;
import org.eclipse.emf.common.util.EList;
import org.eclipse.wst.wsdl.Part;
import org.grlea.log.SimpleLogger;

import de.uni_stuttgart.iaas.bpel_d.algorithm.infrastructure.InOut;
import de.uni_stuttgart.iaas.bpel_d.algorithm.infrastructure.Placement;
import de.uni_stuttgart.iaas.bpel_d.algorithm.infrastructure.State;
import de.uni_stuttgart.iaas.bpel_d.algorithm.infrastructure.VariableElement;
import de.uni_stuttgart.iaas.bpel_d.algorithm.infrastructure.Writes;

/**
 * Analysis of basic activities
 * @author yangyang Gao
 *
 */
public class Basic {

	private final static SimpleLogger logger = new SimpleLogger(Basic.class);

	/**
	 * Analyze basic activities
	 * See DIP-2726 p. 64 algo 8
	 * @param activity
	 * @param variableElement
	 */
	public static void handleBasicActivity(org.eclipse.bpel.model.Activity activity, String variableElement) {
		logger.entry("doesWriteReceive()");
		logger.debugObject("act", Utility.dumpEE(activity));
		logger.debugObject("ve", variableElement);

		State myState = State.getInstance();
		boolean activityWrites = doesWrite(activity, variableElement);
		Placement basicActivityInPlacement = new Placement(activity, InOut.IN);
		Placement basicActivityOutPlacement = new Placement(activity, InOut.OUT);
		Writes writesIn = myState.getWrites(basicActivityInPlacement);
		Writes writesOut = myState.getWrites(basicActivityOutPlacement);
		if (activityWrites) {
			Writes writesTmp = new Writes(new Placement(activity, InOut.TEMP));
			writesTmp.getPoss().add(activity);
			Writes tmpOut = de.uni_stuttgart.iaas.bpel_d.algorithm.analysis.Activity.overWrite(writesIn, writesTmp);
			writesOut.clear();
			writesOut.copyData(tmpOut);
			
//			System.err.println(activity);
//			System.err.println(tmpOut);
//			System.err.println(writesOut);
//			writesOut = myState.getWrites(basicActivityOutPlacement);
//			System.err.println(writesOut);
		} else {
			writesOut.getPoss().addAll(writesIn.getPoss());
			writesOut.getDis().addAll(writesIn.getDis());
			writesOut.getInv().addAll(writesIn.getInv());
			writesOut.setMbd(writesIn.isMbd());
		}
		
		logger.exit("handleBasicActivity");
		logger.debugObject("writesOut", writesOut);
		de.uni_stuttgart.iaas.bpel_d.algorithm.analysis.Activity.handleSuccessors(activity, variableElement);
	}

	/**
	 * Implementation of w(a, v_e)
	 * See list in DIP-2726 p. 65
	 * Delegates to more specific methods per activity type
	 * @param activity
	 * @param variableElement
	 * @return
	 */
	public static boolean doesWrite(ExtensibleElement ee, String variableElement) {
		logger.entry("doesWrite()");
		logger.debugObject("act", Utility.dumpEE(ee));
		logger.debugObject("ve", variableElement);

		if (ee instanceof Receive) {
			return doesWriteReceive((Receive)ee, variableElement);
		} else if (ee instanceof Assign) {
			return doesWriteAssign((Assign) ee, variableElement);
		} else if(ee instanceof Invoke){
			return doseWriteInvoke((Invoke)ee, variableElement);
		} else if(ee instanceof Throw ){
			return doesWriteThrow((Throw)ee, variableElement);
		} else if(ee instanceof EventHandler){
			return doesWriteEventHandler((EventHandler)ee,variableElement);
		} else if(ee instanceof FaultHandler){
			return doesWriteFaultHandler((FaultHandler)ee,variableElement);
		} else if(ee instanceof Pick){
			return doseWritePick((Pick)ee, variableElement);
		} else if(ee instanceof ForEach){
			return doseWriteForEach((ForEach)ee, variableElement);
		} else if (ee instanceof OnMessage){
			return doseWriteOnMessage((OnMessage)ee, variableElement);
		} else if (ee instanceof OnEvent){
			return doseWriteOnEvent((OnEvent) ee, variableElement);
		} else if (ee instanceof Catch){
			return doseWriteCatch((Catch)ee, variableElement);
		}

		return false;
	}


	private static boolean doseWriteCatch(Catch ee, String variableElement) {
		Catch c = (Catch) ee;
		Variable v = c.getFaultVariable();
		if(v != null ){
			if(Utility.subElement(v.getName(),variableElement))
			return true;
		}
		return false;
	}

	private static boolean doseWriteOnEvent(OnEvent ee, String variableElement) {
		OnEvent oe = (OnEvent) ee;
		Variable v = oe.getVariable();
		if(v != null){
			if(Utility.subElement(v.getName(),variableElement))
			return true;
			FromParts fps = oe.getFromParts();
			if (fps != null) {
				for (FromPart fp : fps.getChildren()) {
					v = fp.getToVariable();
					if(Utility.subElement(v.getName(),variableElement))
					return true;
					}
				}
		}
		return false;
	}

	private static boolean doseWriteOnMessage(OnMessage ee, String variableElement) {
		OnMessage om = (OnMessage) ee;
		Variable v = om.getVariable();
		if(v != null){
			if(Utility.subElement(v.getName(),variableElement))
			return true;
			FromParts fps = om.getFromParts();
			if (fps != null) {
				for (FromPart fp : fps.getChildren()) {
					v = fp.getToVariable();
					if(Utility.subElement(v.getName(),variableElement))
					return true;
					}
				}
		}
		return false;
	}

	private static boolean doseWriteForEach(ForEach ee, String variableElement) {
		ForEach fe = (ForEach) ee;
		Variable v = fe.getCounterName();
		if(v != null){
			if( v.getName().equals(variableElement));
			return true;
		}
		return false;
	}

	private static boolean doseWritePick(Pick ee, String variableElement) {
		Pick p = (Pick) ee;
		EList<OnMessage> om = p.getMessages();
		for(OnMessage o : om){
			Variable v = o.getVariable();
			if( v != null) {
			if(Utility.subElement(v.getName(),variableElement))
			return true;
			}
			FromParts fps = o.getFromParts();
			if (fps != null) {
				for (FromPart fp : fps.getChildren()) {
					v = fp.getToVariable();
					if(Utility.subElement(v.getName(),variableElement))
					return true;
					}
				}
		}
		return false;
	}

	private static boolean doesWriteFaultHandler(FaultHandler ee,
			String variableElement) {
		FaultHandler fh = (FaultHandler) ee;
		EList<Catch> f = fh.getCatch();
		for(Catch c : f){
			Variable v = c.getFaultVariable();
			if(v != null){
				if(Utility.subElement(v.getName(),variableElement))
					return true;
			}
		}
		return false;
	}

	private static boolean doesWriteEventHandler(EventHandler ee,
			String variableElement) {
		EList<OnEvent> e = ee.getEvents();
		for(OnEvent ev : e){
			Variable v = ev.getVariable();
			if(v != null){
				if(Utility.subElement(v.getName(),variableElement))
					return true;
			}
			FromParts fps = ev.getFromParts();
			if (fps != null) {
				for (FromPart fp : fps.getChildren()) {
					v = fp.getToVariable();
					if(Utility.subElement(v.getName(),variableElement))
					return true;
					}
				}
		
		}
		return false;
	}

	private static boolean doesWriteThrow(Throw ee, String variableElement) {
		org.eclipse.bpel.model.Variable throwVar = ee.getFaultVariable();
		if( throwVar != null){
			if(Utility.subElement(throwVar.getName(),variableElement))
				return true;
		}
		return false;
	}

	private static boolean doseWriteInvoke(Invoke ee, String variableElement) {
		logger.entry("doesWriteInvoke()");
		logger.debugObject("act", Utility.dumpEE(ee));
		logger.debugObject("ve", variableElement);

		org.eclipse.bpel.model.Variable invokeVar = ee.getOutputVariable();
		if ( invokeVar!=null) {
			if(Utility.subElement(invokeVar.getName(),variableElement)) {
				logger.exit("doesWriteInvoke()");
				return true;
			}
		}
		FromParts fps = ee.getFromParts();
		if (fps != null) {
		for (FromPart fp : fps.getChildren()) {
			org.eclipse.bpel.model.Variable v = fp.getToVariable();
				if (v != null) {
					if(Utility.subElement(v.getName(),variableElement)) {
						logger.exit("doesWriteInvoke()");
						return true;
					}
				}
			}
		}
		ToParts tps = ee.getToParts();
		if (tps != null) {
			for (ToPart tp : tps.getChildren()) {
				org.eclipse.bpel.model.Variable v = tp.getFromVariable();
				if (v != null) {
					if (Utility.subElement(v.getName(),variableElement)) {
						logger.exit("doesWriteInvoke()");
						return true;
					}
				}
			}
		}
		logger.exit("doesWriteInvoke()");
		return false;
	}

	private static boolean doesWriteAssign(Assign ee, String variableElement) {
		EList<Copy> cp = ee.getCopy();
		for (Copy c: cp) {
			To t = c.getTo();
			org.eclipse.bpel.model.Variable var = t.getVariable();
			if (var != null) {
				Property pr = t.getProperty();
				if(pr != null){
					// TODO: Quickhack, property should be converted to XPath statement						
					String name = pr.getName();
					if(Utility.subElement(name,variableElement))
						return true;
				} else {
					String name = var.getName();
					
					// TODO: the commented code is the right one, but t.getPart() /always/ returns null
					// Thus, a Quickhack is provided - changed visibility of toImpl.partName, since to.getPart() was always null, even if partName was != null
					Part p = t.getPart();
					if (p != null) {
						name = name + "." + p.getName();
					}
//					ToImpl toImpl = (org.eclipse.bpel.model.impl.ToImpl) t;
//					if (toImpl.partName != null) {
//						name = name + "." + toImpl.partName;
//					}

					Query q = t.getQuery();
					if (q != null) {
						String query = q.getValue();
						// Annahme: query == /a/b/c ohne Variable davor
						String n = name + "/" + query;
						if (Utility.subElement(n, variableElement))
							return true;
					} else {
						// no query given, directly check name. 
						// name contains either variable or variable.part
						if (Utility.subElement(name, variableElement))
							return true;
					}
					
				}
			}
			PartnerLink pl = t.getPartnerLink();
			if (pl != null) {
				String name = pl.getName();
				if(Utility.subElement(name,variableElement))
					return true;
			}
			Expression ex = t.getExpression();
			if ((ex != null) && (ex.getBody() != null)) {
				// TODO: Quickhack, getting too much variables - check whether this is OK with the constraints of the paper
				String name = ex.getBody().toString();
				if(Utility.subElement(name,variableElement))
					return true;
			}
			//return true;
		}
		
		return false;
	}

	/**
	 * Implementation of w(a, v_e) for receive activities
	 * See list in DIP-2726 p. 65
	 * @see #doesWrite(Activity, VariableElement)
	 * @param activity
	 * @param variableElement
	 * @return
	 */
	private static boolean doesWriteReceive(Receive activity,
			String variableElement) {
		logger.entry("doesWriteReceive()");
		logger.debugObject("act", Utility.dumpEE(activity));
		logger.debugObject("ve", variableElement);

		org.eclipse.bpel.model.Variable receiveVar = activity.getVariable();
		if (receiveVar != null) {
			if (Utility.subElement(receiveVar.getName(), variableElement)) {
				return true;
			}
		}
		FromParts fromParts = activity.getFromParts();
		if (fromParts != null) {
			for (FromPart fromPart : fromParts.getChildren()) {
				org.eclipse.bpel.model.Variable var = fromPart.getToVariable();
				if(var != null){
				if(Utility.subElement(var.getName(),variableElement)) {
					return true;
				}
				}
			}
		}
		
		logger.exit("doesWriteReceive()");
		return false;
	}
	
}
