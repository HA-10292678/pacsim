package org.cloudbus.cloudsim.pam;

import java.util.List;

import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Vm;


public class PAMPeList  {
	
	/**
	 * Gets MIPS Rating for a specified Pe ID.
	 *
	 * @param id    the Pe ID
	 * @param list the pe list
	 *
	 * @return the MIPS rating if exists, otherwise returns -1
	 *
	 * @pre id >= 0
	 * @post $none
	 */
	
	
    public static <T extends PAMPe> PAMPe getById(List<? extends PAMPe> list, int id) {
    	for (PAMPe pe : list) {
    		if (pe.getId() == id) {
    			return pe;
    		}
		}
    	return null;
    }

	
    
    /**
	 * Gets MIPS Rating for a specified Pe ID.
	 *
	 * @param id    the Pe ID
	 * @param peList the pe list
	 *
	 * @return the MIPS rating if exists, otherwise returns -1
	 *
	 * @pre id >= 0
	 * @post $none
	 */
	public static <T extends PAMPe> int getMips(List<PAMPe> peList, int id) {
		PAMPe pe = getById(peList, id);
		if (pe != null) {
			return pe.getMips();
		}
		return -1;
	}

    /**
     * Gets total MIPS Rating for all PEs.
     *
     * @param peList the pe list
     *
     * @return the total MIPS Rating
     *
     * @pre $none
     * @post $none
     */
    public static <T extends PAMPe> int getTotalMips(List<PAMPe> peList) {
    	int totalMips = 0;
    	for (PAMPe pe : peList) {
    		totalMips += pe.getMips();
		}
    	return totalMips;
    }


    /**
     * Gets the max utilization among by all PEs.
     *
     * @param peList the pe list
     *
     * @return the utilization
     */
    public static <T extends PAMPe> double getMaxUtilization(List<PAMPe> peList) {
    	double maxUtilization = 0;
    	for (PAMPe pe : peList) {
    		double utilization = pe.getPeProvisioner().getUtilization();
    		if (utilization > maxUtilization) {
    			maxUtilization = utilization;
    		}
    	}
    	return maxUtilization;
    }

	/**
	 * Gets the max utilization among by all PEs
	 * allocated to the VM.
	 *
	 * @param vm the vm
	 * @param peList the pe list
	 *
	 * @return the utilization
	 */
	public static <T extends PAMPe> double getMaxUtilizationAmongVmsPes(List<PAMPe> peList, Vm vm) {
		double maxUtilization = 0;
		for (PAMPe pe : peList) {
			if (pe.getPeProvisioner().getAllocatedMipsForVm(vm) == null) {
				continue;
			}
			double utilization = pe.getPeProvisioner().getUtilization();
			if (utilization > maxUtilization) {
				maxUtilization = utilization;
			}
		}
		return maxUtilization;
	}

    /**
     * Gets a Pe ID which is FREE.
     *
     * @param peList the pe list
     *
     * @return a Pe ID if it is FREE, otherwise returns -1
     *
     * @pre $none
     * @post $none
     */
    public static <T extends PAMPe> PAMPe getFreePe(List<PAMPe> peList) {
    	for (PAMPe pe : peList) {
    		if (pe.getStatus() == PAMPe.FREE) {
    			return pe;
    		}
		}
    	return null;
    }

    /**
     * Gets the number of <tt>FREE</tt> or non-busy Pe.
     *
     * @param peList the pe list
     *
     * @return number of Pe
     *
     * @pre $none
     * @post $result >= 0
     */
    public static <T extends PAMPe> int getFreePesNumber(List<PAMPe> peList) {
        int cnt = 0;
    	for (PAMPe pe : peList) {
    		if (pe.getStatus() == PAMPe.FREE) {
    			cnt++;
    		}
		}
    	return cnt;
    }

    /**
     * Sets the Pe status.
     *
     * @param status   Pe status, either <tt>Pe.FREE</tt> or <tt>Pe.BUSY</tt>
     * @param id the id
     * @param peList the pe list
     *
     * @return <tt>true</tt> if the Pe status has been changed, <tt>false</tt>
     * otherwise (Pe id might not be exist)
     *
     * @pre peID >= 0
     * @post $none
     */
    public static <T extends PAMPe> boolean setPeStatus(List<PAMPe> peList, int id, int status) {
		PAMPe pe = getById(peList, id);
		if (pe != null) {
			pe.setStatus(status);
			return true;
		}
		return false;
    }

    /**
     * Gets the number of <tt>BUSY</tt> Pe.
     *
     * @param peList the pe list
     *
     * @return number of Pe
     *
     * @pre $none
     * @post $result >= 0
     */
    public static <T extends PAMPe> int getBusyPesNumber(List<PAMPe> peList){
        int cnt = 0;
    	for (PAMPe pe : peList) {
    		if (pe.getStatus() == PAMPe.BUSY) {
    			cnt++;
    		}
		}
    	return cnt;
    }
    //states
    public static <T extends PAMPe> int getPesInOn(List<PAMPe> peList){
        int cnt = 0;
    	for (PAMPe pe : peList) {
    		if (pe.getState()== IBMPowerState.ON) {
    			cnt++;
    		}
		}
    	return cnt;
    }

    public static <T extends PAMPe> int getPesInNap(List<PAMPe> peList){
        int cnt = 0;
    	for (PAMPe pe : peList) {
    		if (pe.getState()== IBMPowerState.NAP) {
    			cnt++;
    		}
		}
    	return cnt;
    }
    public static <T extends PAMPe> int getPesInSleep(List<PAMPe> peList){
        int cnt = 0;
    	for (PAMPe pe : peList) {
    		if (pe.getState()== IBMPowerState.SLEEP) {
    			cnt++;
    		}
		}
    	return cnt;
    }
    public static <T extends PAMPe> int getPesInWinkle(List<PAMPe> peList){
        int cnt = 0;
    	for (PAMPe pe : peList) {
    		if (pe.getState()== IBMPowerState.WINKLE) {
    			cnt++;
    		}
		}
    	return cnt;
    }
    public static <T extends PAMPe> int getPesInOff(List<PAMPe> peList){
        int cnt = 0;
    	for (PAMPe pe : peList) {
    		if (pe.getState()== IBMPowerState.OFF) {
    			cnt++;
    		}
		}
    	return cnt;
    }
    
 
    
	public static void setState(List<PAMPe> peList,int state) {
		for (PAMPe pe : peList) {
			pe.setState(state);
		}
		
	}
    /**
     * Sets the status of PEs of this machine to FAILED.
     * NOTE: <tt>resName</tt> and <tt>machineID</tt> are used for debugging
     * purposes, which is <b>ON</b> by default.
     * Use {@link #setStatusFailed(boolean)} if you do not want
     * this information.
     *
     * @param resName   the name of the resource
     * @param hostId the id of this machine
     * @param failed      the new value for the "failed" parameter
     */
    public static <T extends PAMPe> void setStatusFailed(List<PAMPe> peList, String resName, int chipId, boolean failed) {
        String status = null;
        if (failed) {
           status = "FAILED";
        } else {
           status = "WORKING";
    	}

        Log.printLine(resName + " - Machine: " + chipId + " is " + status);

        setStatusFailed(peList, failed);
    }

    /**
     * Sets the status of PEs of this machine to FAILED.
     *
     * @param failed      the new value for the "failed" parameter
     * @param peList the pe list
     */
    public static <T extends PAMPe> void setStatusFailed(List<PAMPe> peList, boolean failed) {
        // a loop to set the status of all the PEs in this machine
    	for (PAMPe pe : peList) {
            if (failed) {
            	pe.setStatus(PAMPe.FAILED);
            } else {
            	pe.setStatus(PAMPe.FREE);
            }
		}
    }

	public static void setStatus(List<PAMPe> peList,int status) {
		for (PAMPe pe : peList) {
			pe.setStatus(status);
		}
		
	}


}
