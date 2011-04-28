/**
 * Classes for describing "writes" placement
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
package de.uni_stuttgart.iaas.bpel_d.algorithm.infrastructure;

import org.eclipse.bpel.model.BPELExtensibleElement;

import de.uni_stuttgart.iaas.bpel_d.algorithm.analysis.Utility;


/**
 * Relationship Activity <-> In/Out/Temp
 * @author Sebastian Breier
 *
 */
public class Placement {

	/**
	 * The element to be linked
	 */
	private BPELExtensibleElement element;
	
	/**
	 * The placement of the writes set: in, out or temp
	 */
	private InOut place;

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Placement)) {
			return false;
		}
		Placement other = (Placement)obj;
		boolean sameActivity = getElement().equals(other.getElement());
		boolean samePlace = getPlace().equals(other.getPlace());
		return sameActivity && samePlace;
	}
	
	@Override
	public int hashCode() {
		return element.hashCode() + place.hashCode();
	}

	/**
	 * @return the activity
	 */
	public BPELExtensibleElement getElement() {
		return element;
	}

	/**
	 * @return the place
	 */
	public InOut getPlace() {
		return place;
	}
	
	/**
	 * Create a new Placement
	 * @param el
	 * @param place
	 */
	public Placement(BPELExtensibleElement el, InOut place) {
		element = el;
		this.place = place;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Placement: element = " + Utility.dumpEE(getElement()) + ", place = " + place;
	}
	
}