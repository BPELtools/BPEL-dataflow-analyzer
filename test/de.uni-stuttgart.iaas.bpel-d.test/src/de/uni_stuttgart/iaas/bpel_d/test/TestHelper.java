package de.uni_stuttgart.iaas.bpel_d.test;

import org.eclipse.bpel.model.resource.BPELResource;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.Resource.Factory;

import de.uni_stuttgart.iaas.bpel_d.algorithm.analysis.AnalysisResult;

public class TestHelper {

    /**
     * Loads the given BPEL process and analyzes it using the analysis algorithm
     *
     * @throws Exception if either loading goes wrong or Process.analyzeProcessModel(process) throws an exception
     */
    public static AnalysisResult Analyze(String location) throws Exception {
        // Currently, there is only one real test (JoinConditionTests)
        // The BPEL files themselves have to be loaded manually and the result inspected manually, too.
        URI uri = URI.createPlatformPluginURI(location, false);

        // old code to try loading resources
        // java.net.URL url = this.getBundle().getEntry("");
        //uri = URI.createFileURI("D:\\workspaces\\BPEL-D\\de.uni-stuttgart.iaas.bpel-d.test\\processes\\bpeltest\\testAss.bpel");

        Factory fac = Resource.Factory.Registry.INSTANCE.getFactory(uri);
        BPELResource bpel_resource = (BPELResource) fac.createResource(uri);
        bpel_resource.load(null);
        org.eclipse.bpel.model.Process process = bpel_resource.getProcess();
        return de.uni_stuttgart.iaas.bpel_d.algorithm.analysis.Process.analyzeProcessModel(process);
    }

}
