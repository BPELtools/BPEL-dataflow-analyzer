/**
 * Classes describing analysis state
 * 
 * Copyright 2008 Sebastian Breier
 * Copyright 2009-2010 Oliver Kopp
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
package infrastructure;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.bpel.model.Activity;
import org.eclipse.bpel.model.ExtensibleElement;

/**
 * The current state of analysis
 * Instances of this object can be passed around to analysis methods to contain analysis data
 * This is a Singleton to ease access
 * 
 * @author Sebastian Breier
 *
 */
public class State {

	/**
	 * Singleton instance
	 */
	private static State instance = null;
	
	/**
	 * All writer states for all activity placements
	 */
	private Map<Placement, Writes> writes = new HashMap<Placement, Writes>();
	
	/**
	 * All flags (started, finished) for all activities
	 */
	private Map<ExtensibleElement, Flags> flags = new HashMap<ExtensibleElement, Flags>();
	
	/**
	 * All variables to check
	 */
	//private Map<String, VariableElement> vars = new HashMap<String, VariableElement>();
	
	/**
	 * Returns the INTERNAL data structure to store all writes
	 * NO copy is made
	 * Use it at the END of the analysis ONLY!!!
	 */
	public Map<Placement, Writes> getAllWrites() {
		return writes;
	}
	
	/**
	 * Return the Singleton instance or create one and return it
	 * @return
	 */
	public static State getInstance() {
		if (instance == null) {
			instance = new State();
		}
		return instance;
	}
	
	/**
	 * Clear the state by deleting the State instance reference
	 */
//	public static void clearState() {
//		instance = null;
//	}
	
	/**
	 * Clear the writes sets
	 */
	public void clearWrites() {
		writes = new HashMap<Placement, Writes>();
	}
	
	/**
	 * Clear all flags of all activities
	 */
	public void clearFlags() {
		flags = new HashMap<ExtensibleElement, Flags>();
	}
	
	/**
	 * Set the "finished" state of a given activity
	 * @param act
	 * @param state
	 */
	public void setFinished(Activity act, boolean state) {
		Flags actFlags = retrieveFlags(act);
		actFlags.setFinished(state);
	}
	
	/**
	 * Retrieve an activity's flags
	 * If they are not yet set, create a Flag set
	 * @param act
	 * @return
	 */
	private Flags retrieveFlags(ExtensibleElement el) {
		if (flags.containsKey(el)) {
			return flags.get(el);
		} else {
			Flags f = new Flags(el);
			flags.put(el, f);
			return f; 
		}
	}
	
	/**
	 * Check if an activity is in "finished" state
	 * @param act
	 * @return
	 */
	public boolean isFinished(ExtensibleElement el) {
		return retrieveFlags(el).isFinished();
	}
	
	/**
	 * Check if an activity is in "started" state
	 * @param act
	 * @return
	 */
	public boolean isStarted(ExtensibleElement el) {
		return retrieveFlags(el).isStarted();
	}
	
	/**
	 * Set the "started" flag of a given element
	 * @param el
	 * @param state
	 */
	public void setStarted(ExtensibleElement el, boolean state) {
		retrieveFlags(el).setStarted(state);
	}
	
	/**
	 * Create a new writes tuple at the given placement
	 * @param p
	 */
	public void newWrites(Placement p) {
		writes.put(p, new Writes(p));
	}
	
	/**
	 * Get a writes tuple for a specific placement
	 * @param p
	 * @return
	 */
	public Writes getWrites(Placement p) {
		if (!writes.containsKey(p)) {
			Writes w = new Writes(p);
			writes.put(p, w);
			return w;
		} else {
			return writes.get(p);
		}
	}

}
