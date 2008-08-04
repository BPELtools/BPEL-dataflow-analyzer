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
package analysis;

import infrastructure.InOut;
import infrastructure.Placement;
import infrastructure.State;
import infrastructure.Variable;
import infrastructure.Writes;

import java.util.Collections;
import java.util.Set;

import org.eclipse.bpel.model.Activity;
import org.eclipse.bpel.model.ExtensibleElement;
import org.eclipse.bpel.model.FromPart;
import org.eclipse.bpel.model.Receive;
import org.eclipse.bpel.model.To;
import org.eclipse.emf.common.util.EList;

/**
 * Analysis of basic activities
 * @author Sebastian Breier
 *
 */
public class Basic {

	/**
	 * Analyze basic activities
	 * See DIP-2726 p. 64 algo 8
	 * @param activity
	 * @param variableElement
	 */
	public static void handleBasicActivity(org.eclipse.bpel.model.Activity activity, Variable variableElement) {
		System.err.println(">handleBasicActivity: activity = " + Utility.dumpEE(activity) + ", ve = " + variableElement);
		State myState = State.getInstance();
		boolean activityWrites = doesWrite(activity, variableElement);
		Placement basicActivityInPlacement = new Placement(activity, InOut.IN);
		Placement basicActivityOutPlacement = new Placement(activity, InOut.OUT);
		Writes writesIn = myState.getWrites(basicActivityInPlacement);
		Set<ExtensibleElement> emptySet = Collections.emptySet();
		Writes writesTmp = new Writes(Collections.singleton((ExtensibleElement)activity), emptySet, emptySet, false);
		Writes writesOut = myState.getWrites(basicActivityOutPlacement);
		if (activityWrites) {
			writesOut = analysis.Activity.overWrite(writesIn, writesTmp);
		} else {
			writesOut.getPoss().addAll(writesIn.getPoss());
			writesOut.getDis().addAll(writesIn.getDis());
			writesOut.getInv().addAll(writesIn.getInv());
			writesOut.setMbd(writesIn.isMbd());
		}
		myState.setFinished(activity, true);
		System.err.println("<handleBasicActivity: writesOut = " + writesOut);
		analysis.Activity.handleSuccessors(activity, variableElement);
	}

	/**
	 * Implementation of w(a, v_e)
	 * See list in DIP-2726 p. 65
	 * Delegates to more specific methods per activity type
	 * @param activity
	 * @param variableElement
	 * @return
	 */
	private static boolean doesWrite(Activity activity, Variable variableElement) {
		System.err.println(">doesWrite: activity = " + Utility.dumpEE(activity) + ", variableElement = " + variableElement);
		if (activity instanceof Receive) {
			return doesWriteReceive((Receive)activity, variableElement);
		}
		//FIXME More Activity types
		return false;
	}

	/**
	 * Implementation of w(a, v_e) for receive activities
	 * See list in DIP-2726 p. 65
	 * @see #doesWrite(Activity, Variable)
	 * @param activity
	 * @param variableElement
	 * @return
	 */
	private static boolean doesWriteReceive(Receive activity,
			Variable variableElement) {
		System.err.println(">doesWriteReceive: activity = " + Utility.dumpEE(activity) + ", variableElement = " + variableElement);
		org.eclipse.bpel.model.Variable receiveVar = activity.getVariable();
		if (receiveVar.getName().equals(variableElement.getName())) {
			return true;
		}
		EList<FromPart> fromParts = activity.getFromPart();
		for (FromPart fromPart: fromParts) {
			To toVariable = fromPart.getTo();
			org.eclipse.bpel.model.Variable var = toVariable.getVariable();
			if (var.getName().equals(variableElement.getName())) {
				return true;
			}
		}
		return false;
	}
	
}
