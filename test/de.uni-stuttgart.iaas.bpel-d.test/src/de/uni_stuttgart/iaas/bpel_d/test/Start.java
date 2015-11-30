package de.uni_stuttgart.iaas.bpel_d.test;

import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.grlea.log.SimpleLogger;

import de.uni_stuttgart.iaas.bpel_d.algorithm.analysis.AnalysisResult;


public class Start implements IApplication {

	private final SimpleLogger logger = new SimpleLogger(Start.class);

	@Override
	public Object start(IApplicationContext arg0) throws Exception {
		try {
            AnalysisResult res = TestHelper
                    .Analyze("/de.uni_stuttgart.iaas.bpel_d.test/processes/OrderInfo/OrderingProcess.bpel");
			res.output();
		} catch (Exception e) {
			logger.errorException(e);
		}

		return null;
	}

    @Override
    public void stop() {
        // nothing to do
    }

}
