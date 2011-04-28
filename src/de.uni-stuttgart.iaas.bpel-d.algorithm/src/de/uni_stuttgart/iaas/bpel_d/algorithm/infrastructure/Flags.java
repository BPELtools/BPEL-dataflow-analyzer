/**
 * Classes describing activity flags used in analysis
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

/**
 * Activity <-> Flag relation
 * @author Sebastian Breier
 *
 */
public class Flags {

	/**
	 * The linked BPEL element
	 */
	private BPELExtensibleElement element;
	
	/**
	 * Flag for "analysis started"
	 */
	private boolean started = false;
	
	/**
	 * Flag for "analysis ended"
	 */
	private boolean finished = false;
	
	/**
	 * Set the activity to be "finished" with analysis
	 * See DIP-2726 p. 29
	 * @param state
	 */
	public void setFinished(boolean state) {
		finished = state;
	}
	
	/**
	 * Create a new Flags set for the given element
	 * @param act
	 */
	public Flags(BPELExtensibleElement element) {
		this.element = element;
	}

	/**
	 * Return the "started" state of an activity
	 * @return
	 */
	public boolean isStarted() {
		return started;
	}

	/**
	 * Return the "finished" state of an activity
	 * @return
	 */
	public boolean isFinished() {
		return finished;
	}

	/**
	 * Set the "started" flag
	 * @param started
	 */
	public void setStarted(boolean started) {
		this.started = started;
	}

}
