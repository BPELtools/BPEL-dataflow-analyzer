package de.uni_stuttgart.iaas.bpel_d;
import infrastructure.InOut;
import infrastructure.Placement;
import infrastructure.State;
import infrastructure.VariableElement;
import infrastructure.Writes;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;

import org.eclipse.bpel.model.BPELPlugin;
import org.eclipse.bpel.model.resource.BPELResource;
import org.eclipse.bpel.model.resource.BPELResourceFactoryImpl;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.Resource.Factory;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.grlea.log.DebugLevel;
import org.grlea.log.SimpleLogger;

import analysis.AnalysisResult;

/**
 * Data flow analyzer startup code
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

/**
 * The analyzer application
 * @author Sebastian Breier
 *
 */
public class Analyzer implements IApplication {

	private final SimpleLogger logger = new SimpleLogger(Analyzer.class);
	
	/**
	 * Start analysis of a given model
	 * 
	 * This is a dirty hack! It does not rely on the OSGi
	 * 
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		init();
		//org.eclipse.bpel.model.Process process = loadProcess("C:\\Dokumente und Einstellungen\\gao\\workspace\\test\\bpeltest\\testEL.bpel");
		//org.eclipse.bpel.model.Process process = loadProcess("C:\\Dokumente und Einstellungen\\gao\\workspace\\test\\PickProcess\\PickProcess\\PickProcess.bpel");
		//org.eclipse.bpel.model.Process process = loadProcess("C:\\Dokumente und Einstellungen\\gao\\workspace\\test\\Example1\\MotivatingExample.bpel");
		org.eclipse.bpel.model.Process process = loadProcess("C:\\Dokumente und Einstellungen\\gao\\workspace\\test\\Linktest\\test3.bpel");
		//org.eclipse.bpel.model.Process process = loadProcess("C:\\Dokumente und Einstellungen\\gao\\workspace\\test\\ScopeTest\\CPtest.bpel");
		//org.eclipse.bpel.model.Process process = loadProcess("C:\\Dokumente und Einstellungen\\gao\\workspace\\test\\throwTest\\test5.bpel");
		//org.eclipse.bpel.model.Process process = loadProcess("C:\\Dokumente und Einstellungen\\gao\\workspace\\test\\DynamicInvoke\\DynamicInvokeProcess\\DynamicInvokeProcess.bpel");
//		State.clearState();
		
//		State.getInstance().dumpVariables();
		AnalysisResult res = analysis.Process.analyzeProcessModel(process);
		res.output();
		
		//TODO: output State as BPEL-D?!
	}

	/**
	 * Initialize the data flow analysis by setting up Eclipse BPEL
	 * 
	 * Only useful if used outside of the Eclipse environment
	 */
	private static void init() {
		BPELPlugin bpelPlugin = new BPELPlugin();
		// see http://wiki.eclipse.org/EMF-FAQ#How_do_I_use_EMF_in_standalone_applications_.28such_as_an_ordinary_main.29.3F
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

	/**
	 * Untested method for a command line call of the tool
	 */
	@Override
	public Object start(IApplicationContext arg0) throws Exception {
		try {
			if (!logger.wouldLog(DebugLevel.L1_FATAL)) {
				System.err.println("The logger is not logging fatal errors. Please put simplelog.properties at the right location and enable logging.");
			}

			Map args = arg0.getArguments();
			if (!args.containsKey("--file")) {
				System.err
						.println("No file given. Please use --file <filename>");
				return null;
			}

			String fn = (String) args.get("--file");

			URI uri = URI.createFileURI(fn);
			Factory fac = Resource.Factory.Registry.INSTANCE.getFactory(uri);
			BPELResource bpel_resource = (BPELResource) fac.createResource(uri);			
			bpel_resource.load(null);
			org.eclipse.bpel.model.Process process = bpel_resource.getProcess();

			AnalysisResult res = analysis.Process.analyzeProcessModel(process);

			res.output();

			return null;
		} catch (Exception e) {
			logger.errorException(e);
			return null;
		}
	}

	@Override
	public void stop() {
	}
	
}
