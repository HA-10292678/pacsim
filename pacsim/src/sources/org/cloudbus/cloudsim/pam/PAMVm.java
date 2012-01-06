package org.cloudbus.cloudsim.pam;

import org.cloudbus.cloudsim.CloudletScheduler;
import org.cloudbus.cloudsim.Vm;

/**
*
* @author		Srinivasan.S
* @version		CS 1.0
*/

public class PAMVm extends Vm{
	
	
	private int duration;
	
	private PAMHost PAMHost;
	
	public PAMVm(int id, int userId, double mips, int pesNumber, int ram, long bw, long size,String vmm, CloudletScheduler cloudletScheduler,int duration) {
		
		super(id,userId,mips,pesNumber,ram,bw,size,vmm,cloudletScheduler);

		setDuration(duration);
	}
	


	public void setDuration(int duration) {
		this.duration = duration;
	}


	public int getDuration() {
		return duration;
	}



	public void setPAMHost(PAMHost pAMHost) {
		PAMHost = pAMHost;
	}



	public PAMHost getPAMHost() {
		return PAMHost;
	}



}
