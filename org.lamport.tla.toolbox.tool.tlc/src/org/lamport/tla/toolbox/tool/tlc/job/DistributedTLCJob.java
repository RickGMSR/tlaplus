package org.lamport.tla.toolbox.tool.tlc.job;

import java.io.File;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunch;
import org.lamport.tla.toolbox.util.ResourceHelper;

import tlc2.tool.distributed.TLCServer;

/**
 * Starts the distributed TLCServer inside the Toolbox 
 */
public class DistributedTLCJob extends TLCProcessJob {

	public DistributedTLCJob(String specName, String modelName, ILaunch launch, int numberOfWorkers) {
		super(specName, modelName, launch, numberOfWorkers);
	}
	
    /**
     * Removes non matching arguments from super class
     * @throws CoreException 
     */
	protected String[] constructProgramArguments() throws CoreException {
		final String userDir = launchDir.getLocation().toOSString();
		final String specFile = ResourceHelper.getModuleName(rootModule);
        
		final String[] args = new String[2];
		args[0] = userDir + File.separator + specFile;
		args[1] = "-tool";
        return args;
	}

	/* (non-Javadoc)
	 * @see org.lamport.tla.toolbox.tool.tlc.job.TLCProcessJob#getMainClass()
	 */
	protected Class getMainClass() {
		return TLCServer.class;
	}
}