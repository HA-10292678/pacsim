package org.cloudbus.cloudsim.pam;

import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.provisioners.PeProvisioner;

public class PAMPe extends Pe{
	
	private int state;

	public PAMPe(int id, PeProvisioner peProvisioner,int state) {
		super(id, peProvisioner);

		setState(state);
	}
	public PAMPe(int id, PeProvisioner peProvisioner) {
		super(id, peProvisioner);

		//setState(state);
	}
	public void setState(int state) {
		this.state = state;
		
	}

	public int getState() {
		return state;
	}

}
