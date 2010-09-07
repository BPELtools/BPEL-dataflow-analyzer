package de.uni_stuttgart.iaas.bpel_d.test;

import java.net.URL;

import org.eclipse.bpel.model.resource.BPELResource;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.Resource.Factory;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.common.util.URI;
import org.grlea.log.SimpleLogger;

import de.uni_stuttgart.iaas.bpel_d.algorithm.analysis.AnalysisResult;


public class Start implements IApplication {

	private final SimpleLogger logger = new SimpleLogger(Start.class);
	
	@Override
	public Object start(IApplicationContext arg0) throws Exception {
		try {
			// URL url = this.getBundle().getEntry("");
			URI uri = URI.createPlatformPluginURI("/de.uni_stuttgart.iaas.bpel_d.test/processes/bpeltest/testAss.bpel",	false);
			//uri = URI.createFileURI("D:\\workspaces\\BPEL-D\\de.uni-stuttgart.iaas.bpel-d.test\\processes\\bpeltest\\testAss.bpel");

			Factory fac = Resource.Factory.Registry.INSTANCE.getFactory(uri);
			BPELResource bpel_resource = (BPELResource) fac.createResource(uri);			
			bpel_resource.load(null);
			org.eclipse.bpel.model.Process process = bpel_resource.getProcess();

			AnalysisResult res = de.uni_stuttgart.iaas.bpel_d.algorithm.analysis.Process.analyzeProcessModel(process);

			res.output();
		} catch (Exception e) {
			logger.errorException(e);
		}

		return null;
	}

	@Override
	public void stop() {
	}

}
