package analysis;

import javax.xml.namespace.QName;
import javax.xml.xpath.XPathVariableResolver;

import java.util.HashMap;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
/**
 * Code for jcContains
 * @author yangyang Gao
 *
 */


public class JoinVariableResolver implements XPathVariableResolver {
	
	private HashMap<String, Boolean> var2value = new HashMap<String, Boolean>();
	private boolean overflow;
	
	public void init(String jc){
		var2value.clear();
		overflow = false;
		
		Pattern pattern = Pattern.compile("\\$[0-9a-zA-Z_]+");
		Matcher matcher = pattern.matcher(jc);
		while (matcher.find()) {
			String var = jc.substring(matcher.start()+1, matcher.end());
			if(!var2value.containsKey(var)){
				var2value.put(var, new Boolean(false) );
			}
			
		}
	}

	@Override
	public Object resolveVariable(QName variableName) {
		String var = variableName.toString();
		Boolean value = var2value.get(var);
		if (value == null) {
			// fallback
			System.out.println("Something wrent wrong: We didn't find all variables during init");
			System.out.println(variableName.toString());
			return false;
		} else {
			return value;
		}
	}
	public void next(){
		Iterator<String> iterator = var2value.keySet().iterator();
		String var = null;
		Boolean value = false;
		
		// !value = digit is 0, that means, it was increased in the last step, therefore a "local overflow" happened and the next digit has to be increased
        while (!value && iterator.hasNext()) {
			var = iterator.next();
			value = var2value.get(var);
			if (!value) {
				value = true;
			} else {
				value = false;
			}
			var2value.put(var, value);
		}

		if (!iterator.hasNext() && (!value)) {
			// overflow happened 1....1 was changed to 0.....0
			overflow = true;
		}
	}
	
	public boolean overflow(){
		return overflow;
	}

	/**
	 * Set all variable values to true
	 */
	public void setToMaxValue() {
		Iterator<String> iterator = var2value.keySet().iterator();
        while (iterator.hasNext()) {
			String var = iterator.next();
			var2value.put(var, true);
		}
	}

	public boolean isMaxValue() {
		Iterator<String> iterator = var2value.keySet().iterator();
        while (iterator.hasNext()) {
			String var = iterator.next();
			if (!var2value.get(var)) {
				// if one zero is found - it is not the max value
				return false;
			}
		}
        return true;
	}
}
