import infrastructure.InOut;
import infrastructure.Placement;
import infrastructure.State;
import infrastructure.VariableElement;
import infrastructure.Writes;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.eclipse.bpel.model.BPELPlugin;
import org.eclipse.bpel.model.resource.BPELResourceFactoryImpl;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;

/**
 * Data flow analyzer startup code
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

/**
 * The analyzer application
 * @author Sebastian Breier
 *
 */
public class Analyzer {

	/**
	 * Start analysis of a given model
	 * @param args
	 */
	public static void main(String[] args) {
		init();
		//org.eclipse.bpel.model.Process process = loadProcess("C:\\Dokumente und Einstellungen\\gao\\workspace\\test\\bpeltest\\testEL.bpel");
		//org.eclipse.bpel.model.Process process = loadProcess("C:\\Dokumente und Einstellungen\\gao\\workspace\\test\\PickProcess\\PickProcess\\PickProcess.bpel");
		//org.eclipse.bpel.model.Process process = loadProcess("C:\\Dokumente und Einstellungen\\gao\\workspace\\test\\Example1\\MotivatingExample.bpel");
		org.eclipse.bpel.model.Process process = loadProcess("C:\\Dokumente und Einstellungen\\gao\\workspace\\test\\Linktest\\test3.bpel");
		//org.eclipse.bpel.model.Process process = loadProcess("C:\\Dokumente und Einstellungen\\gao\\workspace\\test\\ScopeTest\\CPtest.bpel");
		//org.eclipse.bpel.model.Process process = loadProcess("C:\\Dokumente und Einstellungen\\gao\\workspace\\test\\throwTest\\test5.bpel");
		//org.eclipse.bpel.model.Process process = loadProcess("C:\\Dokumente und Einstellungen\\gao\\workspace\\test\\DynamicInvoke\\DynamicInvokeProcess\\DynamicInvokeProcess.bpel");
		State.clearState();
		
//		State.getInstance().dumpVariables();
		analysis.Process.analyzeProcessModel(process);
		
		//TODO: output State as BPEL-D?!
	}

	/**
	 * Read a line of text from the given reader
	 * @param reader
	 * @return
	 */
	private static String readLine(BufferedReader reader) {
		try {
			return reader.readLine();
		} catch (IOException exception) {
			return null;
		}
	}

	/**
	 * Create a new BufferedReader that will read from a file name
	 * @param fileName
	 * @return
	 */
	private static BufferedReader getBufferedReader(String fileName) {
		try {
			return new BufferedReader(new FileReader(fileName));
		} catch (FileNotFoundException exception) {
			return null;
		}
	}

	/**
	 * Initialize the data flow analysis by setting up Eclipse BPEL
	 */
	private static void init() {
		BPELPlugin bpelPlugin = new BPELPlugin();
		Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("*", new BPELResourceFactoryImpl());
	}

	/**
	 * Load a BPEL process from a fileName and return the loaded process
	 * @param fileName
	 * @return
	 */
	private static org.eclipse.bpel.model.Process loadProcess(String fileName) {
		ResourceSet resourceSet = new ResourceSetImpl();
		URI uri = URI.createFileURI(fileName);
		Resource resource = resourceSet.getResource(uri, true);
		return (org.eclipse.bpel.model.Process)resource.getContents().get(0);
	}
	
}
