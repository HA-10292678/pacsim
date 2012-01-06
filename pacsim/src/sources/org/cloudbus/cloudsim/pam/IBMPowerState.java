package org.cloudbus.cloudsim.pam;

public class IBMPowerState{
	
	public static final int ON  = 1;
	
	public static final int NAP  = 2;
	
	public static final int SLEEP  = 3;
	
	public static final int WINKLE  = 4;
	
	public static final int OFF  = 5;
		
	public static int savedPowerPercentage;
	
	public static double powerConserved;


	public static int getSavedPowerPercentage(int state)
	{
		if(state==ON){
			savedPowerPercentage=0;
		}
		if(state==NAP){
			savedPowerPercentage=20;
		}
		if(state==SLEEP){
			savedPowerPercentage=40;	
		}
		if(state==WINKLE){
			savedPowerPercentage=80;	
		}
		if(state==OFF){
			savedPowerPercentage=100;
		}
		return savedPowerPercentage;
	}
	
	public static double calculatePowerConservedByPe(int duration, int state,int power,int pes){
		
		powerConserved=(pes*getSavedPowerPercentage(state)*duration*power)/100;	
		return powerConserved;
	}
	
	public static double calculatePowerConservedByChip(int duration, int state,int power, int chips,int pesInChip){
		double actualPower=(pesInChip*chips*power*getSavedPowerPercentage(state)*duration)/100;
		return powerConserved = (actualPower + (actualPower*20)/100);
	}
	
	public static double calculatePowerConservedByHost(int duration, int state,int power, int hosts,int pesInHost){
		double actualChipPower=(pesInHost*hosts*power*getSavedPowerPercentage(state)*duration)/100;
		double actualHostPower=(actualChipPower + (actualChipPower*20)/100);
		return powerConserved = (actualHostPower + (actualHostPower*20)/100);
	}
	
}
