/**
 * Negation Variable Resolver
 * 
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
package de.uni_stuttgart.iaas.bpel_d.algorithm.analysis;

import java.util.Iterator;

import javax.xml.namespace.QName;
import javax.xml.xpath.XPathVariableResolver;

import org.eclipse.bpel.model.Link;
import org.eclipse.bpel.model.Target;
import org.eclipse.emf.common.util.EList;
import org.grlea.log.SimpleLogger;

import de.uni_stuttgart.iaas.bpel_d.algorithm.infrastructure.*;


/**
 * Code for jcContains
 * @author yangyang Gao, Oliver Kopp
 *
 */
public class NegationVariableResolver implements XPathVariableResolver {

	private final SimpleLogger logger = new SimpleLogger(NegationVariableResolver.class);
	
	private org.eclipse.bpel.model.Activity activity;
	
	public NegationVariableResolver(org.eclipse.bpel.model.Activity a) {
		this.activity = a;
	}
	
	@Override
	public Object resolveVariable(QName variableName) {

		// convert QName to ExtensibleElement
		
		// search for link
		// TODO room for speedup improvement here --> use hashmap to hash found values
		EList<Target> targets= activity.getTargets().getChildren();
		Link l = null;
		boolean found = false;
		Iterator<Target> it = targets.iterator();
		while (!found && it.hasNext() ){
			l = it.next().getLink();
			if (l.getName().equals(variableName.toString())) {
				found = true;
			}
		}
		if (!found) {
			logger.error("sorry, no link found");
			return false;
		}
			
		Writes w = State.getInstance().getWrites(new Placement(l, InOut.OUT));
		return !w.isMbd();
	}

}
