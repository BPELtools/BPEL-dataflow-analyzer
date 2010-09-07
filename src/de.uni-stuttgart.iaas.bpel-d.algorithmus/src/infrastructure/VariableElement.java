/**
 * Classes for BPEL variable handling
 * 
 * Copyright 2008 Sebastian Breier
 * Copyright 2010 Oiver Kopp
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

import java.util.HashSet;
import java.util.Set;

/**
 * One BPEL variable with refs to sub-elements
 * @author Sebastian Breier, Oliver Kopp
 *
 */
public class VariableElement {

	/**
	 * All subelements of this variable
	 */
	//private Set<Variable> sub = new HashSet<Variable>();
	
	/**
	 * The variable name
	 */
	private String name;
	
	/**
	 * Add a subelement to this variable
	 * @param sub
	 */
//	public void addSubElement(Variable subVar) {
//		if (sub == null) {
//			return;
//		}
//		sub.add(subVar);
//	}
	
	/**
	 * Create a new Variable with specified names
	 * @param name
	 */
	public VariableElement(String name) {
		this.name = name;
	}

//	@Override
//	public String toString() {
//		return "Name = " + name + ", sub = " + sub;
//	}

	/**
	 * Return the name of the variable
	 * @return the name
	 */
//	public String getName() {
//		return name;
//	}
	
	
}
