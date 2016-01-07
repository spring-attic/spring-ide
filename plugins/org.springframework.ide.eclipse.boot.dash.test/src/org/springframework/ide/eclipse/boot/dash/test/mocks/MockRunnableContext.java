package org.springframework.ide.eclipse.boot.dash.test.mocks;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.junit.Assert;

public class MockRunnableContext implements IRunnableContext {

	@Override
	public void run(boolean fork, boolean cancelable, IRunnableWithProgress runnable) throws InvocationTargetException, InterruptedException {
		Assert.assertFalse("forking not yet supported by MockRunnableContext", fork);
		runnable.run(new NullProgressMonitor());
	}

}
