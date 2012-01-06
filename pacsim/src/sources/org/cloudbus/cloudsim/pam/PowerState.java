package org.cloudbus.cloudsim.pam;

public interface PowerState {

	public int getSavedPowerPercentage(int state);
	
	public double calculatePowerConsumption(int duration, int state,int power);
}
