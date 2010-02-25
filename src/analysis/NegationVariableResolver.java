package analysis;

import java.util.Iterator;

import javax.xml.namespace.QName;
import javax.xml.xpath.XPathVariableResolver;

import org.eclipse.bpel.model.Link;
import org.eclipse.bpel.model.Target;
import org.eclipse.emf.common.util.EList;

import infrastructure.*;

/**
 * Code for jcContains
 * @author yangyang Gao
 *
 */

public class NegationVariableResolver implements XPathVariableResolver {

	org.eclipse.bpel.model.Activity activity;
	
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
			System.err.println("sorry, no link found");
			return false;
		}
			
		Writes w = State.getInstance().getWrites(new Placement(l, InOut.OUT));
		return !w.isMbd();
	}

}
