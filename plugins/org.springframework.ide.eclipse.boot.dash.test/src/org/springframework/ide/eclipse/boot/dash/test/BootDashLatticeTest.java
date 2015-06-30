package org.springframework.ide.eclipse.boot.dash.test;

import java.util.List;

import org.junit.Test;

import io.pivotal.receptor.client.ReceptorClient;
import io.pivotal.receptor.commands.DesiredLRPResponse;

public class BootDashLatticeTest {

	//Quick test that receptor client api is working.
	//Note: that this test requires lattice is running locally. So it may be
	// tricky / impossible to run this test in CI build environment


	@Test public void testGetListOfLRPs() throws Exception {
		ReceptorClient receptor = new ReceptorClient();

		List<DesiredLRPResponse> lrps = receptor.getDesiredLRPs();
		for (DesiredLRPResponse lrp : lrps) {
			System.out.println(lrp.getProcessGuid());
		}
	}


}
