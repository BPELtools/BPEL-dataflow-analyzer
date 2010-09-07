/**
 * Classes for saving writer state
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
package de.uni_stuttgart.iaas.bpel_d.algorithm.infrastructure;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.bpel.model.ExtensibleElement;

import de.uni_stuttgart.iaas.bpel_d.algorithm.analysis.Utility;


/**
 * The writer state for a certain activity in a certain state
 * @author Sebastian Breier
 *
 */
public class Writes {

	/**
	 * Set of possible writers
	 */
	private Set<ExtensibleElement> poss = new HashSet<ExtensibleElement>();
	
	/**
	 * Set of disabled writers
	 */
	private Set<ExtensibleElement> dis = new HashSet<ExtensibleElement>();
	
	/**
	 * Set of invalid writers
	 */
	private Set<ExtensibleElement> inv = new HashSet<ExtensibleElement>();
	
	/**
	 * May be dead state 
	 */
	private boolean mbd = false;
	
	/**
	 * Placement: Activity + In, Out or temporary
	 */
	private Placement place;
	
	public Writes() {
		
	}
	
	/**
	 * Create a new writes tuple
	 * @param p
	 */
	public Writes(Placement p) {
		place = p;
	}
	
	/**
	 * Create a new non-placed writes tuple
	 */
	public Writes(Set<ExtensibleElement> poss, Set<ExtensibleElement> dis, Set<ExtensibleElement> inv, boolean mbd) {
		place = null;
		this.poss = poss;
		this.dis = dis;
		this.inv = inv;
		this.mbd = mbd;
	}
	
	public Writes(Placement p, Set<ExtensibleElement> poss, Set<ExtensibleElement> dis, Set<ExtensibleElement> inv, boolean mbd) {
		this(poss,dis,inv,mbd);
		place = p;
	}

	public Placement getPlacement() {
		return place;
	}
	
//	/**
//	 * Copy the data from the parent activity
//	 * See DIP-2726 p. 52 l. 6
//	 */
//	public void copyFromParent() {
//		if (place == null) {
//			return;
//		}
//		State myState = State.getInstance();
//		ExtensibleElement parent = Utility.getParent(place.getElement());
//		if (parent == null) {
//			return;
//		}
//		Placement parentPlacement = new Placement(parent, InOut.IN);
//		Writes parentWritesIn = myState.getWrites(parentPlacement);
//		Set<ExtensibleElement> possParent = new HashSet<ExtensibleElement>(parentWritesIn.getPoss());
//		Set<ExtensibleElement> invParent = new HashSet<ExtensibleElement>(parentWritesIn.getInv());
//		boolean mbdParent = parentWritesIn.isMbd();
//		setPoss(possParent);
//		clearDis();
//		setInv(invParent);
//		setMbd(mbdParent);
//	}
//	
	public void copyData(Writes w) {
		this.poss.addAll(w.getPoss());
		this.dis.addAll(w.getDis());
		this.inv.addAll(w.getInv());
		this.mbd = w.isMbd();
	}

	private void clearPoss() {
		poss = new HashSet<ExtensibleElement>();		
	}

	/**
	 * Clear the disabled writers set
	 */
	public void clearDis() {
		setDis(new HashSet<ExtensibleElement>());
	}
	
	/**
	 * Clear the invalid writers set
	 */
	public void clearInv() {
		setInv(new HashSet<ExtensibleElement>());
	}
	
	public void clear() {
		clearPoss();
		clearDis();
		clearInv();
		this.mbd = false;
	}

	/**
	 * Get the possible writers of this writes tuple
	 * @return the poss
	 */
	public Set<ExtensibleElement> getPoss() {
		return poss;
	}

	/**
	 * Get the disabled writers of this writes tuple
	 * @return the dis
	 */
	public Set<ExtensibleElement> getDis() {
		return dis;
	}

	/**
	 * Get the invalid writers of this writes tuple
	 * @return the inv
	 */
	public Set<ExtensibleElement> getInv() {
		return inv;
	}

	/**
	 * Get the "may be dead" state of this writes tuple
	 * @return the mbd
	 */
	public boolean isMbd() {
		return mbd;
	}

	/**
	 * Set the possible writers
	 * @param poss the poss to set
	 */
	private void setPoss(Set<ExtensibleElement> poss) {
		this.poss = poss;
	}

	/**
	 * Set the disabled writers
	 * @param dis the dis to set
	 */
	private void setDis(Set<ExtensibleElement> dis) {
		this.dis = dis;
	}

	/**
	 * Set the invalid writers
	 * @param inv the inv to set
	 */
	private void setInv(Set<ExtensibleElement> inv) {
		this.inv = inv;
	}

	/**
	 * Set the "may be dead" state
	 * @param mbd the mbd to set
	 */
	public void setMbd(boolean mbd) {
		this.mbd = mbd;
	}
	
	/**
	 * Merge the current writers with the given ones
	 * @param p
	 * @param d
	 * @param i
	 * @param mbdState
	 */
	public void mergeWith(Set<ExtensibleElement> p, Set<ExtensibleElement> d, Set<ExtensibleElement> i, boolean mbdState) {
		poss.addAll(p);
		dis.addAll(d);
		inv.addAll(i);
		mbd = mbd || mbdState;
	}
	
	private static String setToString(Set<ExtensibleElement> set) {
		String res;
		if (set.size() == 0)
			return "[]";
		
		Iterator<ExtensibleElement> it = set.iterator();
		ExtensibleElement e = it.next();
		res = "[" + Utility.dumpEE(e);
		
		while (it.hasNext()) {
			res = res + ", " + Utility.dumpEE(it.next());
		}
		
		return res + "]"; 
	}

	@Override
	public String toString() {
		return "Writes @ " + place + ": poss = " + setToString(poss) + ", dis = " + setToString(dis) + ", inv = " + setToString(inv) + ", mbd = " + mbd;
	}
	
	public boolean equals(Object o) {
		if (o instanceof Writes) {
			Writes w = (Writes) o;
			return (
				this.poss.equals(w.getPoss()) &&
				this.dis.equals(w.getDis()) &&
				this.inv.equals(w.getInv()) &&
				(this.mbd == w.mbd)
				);
		} else {
			return false;
		}
	}
	
}