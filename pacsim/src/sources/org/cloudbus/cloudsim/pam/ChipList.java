package org.cloudbus.cloudsim.pam;

import java.util.List;

import org.cloudbus.cloudsim.Log;



public class ChipList {

	/**
	 * Gets the Machine object for a particular ID.
	 *
	 * @param id    the chip ID
	 * @param chipList the chip list
	 *
	 * @return the Machine object or <tt>null</tt> if no machine exists
	 *
	 * @see gridsim.Machine
	 * @pre id >= 0
	 * @post $none
	 */
    public static <T extends Chip> T getById(List<T> chipList, int id) {
        for (T chip : chipList) {
    		if (chip.getId() == id) {
    			return chip;
    		}
		}
        return null;
    }

    /**
     * Gets the total number of PEs for a host.
     *
     * @param chipList the chip list
     *
     * @return number of PEs
     *
     * @pre $none
     * @post $result >= 0
     */
    public static <T extends Chip> int getPesNumber(List<T> chipList) {
        int pesNumber = 0;
        for (T chip : chipList) {
    		pesNumber += chip.getPeList().size();
		}
        return pesNumber;
    }

    /**
     * Gets the total number of <tt>FREE</tt> or non-busy PEs for all chips in a host.
     *
     * @param chipList the chip list
     *
     * @return number of PEs
     *
     * @pre $none
     * @post $result >= 0
     */
    @SuppressWarnings("unchecked")
	public static <T extends Chip> int getFreePesNumber(List<T> chipList) {
        int freePesNumber = 0;
        for (T chip : chipList) {
        	freePesNumber += PAMPeList.getFreePesNumber((List<PAMPe>) chip.getPeList());
		}
        return freePesNumber;
    }

    /**
     * Gets the total number of <tt>BUSY</tt> PEs for all chips in a host.
     *
     * @param chipList the chip list
     *
     * @return number of PEs
     *
     * @pre $none
     * @post $result >= 0
     */
    @SuppressWarnings("unchecked")
	public static <T extends Chip> int getBusyPesNumber(List<T> chipList) {
        int busyPesNumber = 0;
        for (T chip : chipList) {
        	busyPesNumber += PAMPeList.getBusyPesNumber((List<PAMPe>) chip.getPeList());
		}
        return busyPesNumber;
    }
    
    
    //states
    
    @SuppressWarnings("unchecked")
	public static <T extends Chip> int getPesInOn(List<T> chipList){
        int pesInOn = 0;
        for (T chip : chipList) {
        	pesInOn  += PAMPeList.getPesInOn((List<PAMPe>) chip.getPeList());
		}
        return pesInOn ;
    }
    @SuppressWarnings("unchecked")
	public static <T extends Chip> int getPesInNap(List<T> chipList){
        int pesInNap = 0;
        for (T chip : chipList) {
        	pesInNap += PAMPeList.getPesInNap((List<PAMPe>) chip.getPeList());
		}
        return pesInNap;
    }

    @SuppressWarnings("unchecked")
	public static <T extends Chip> int getPesInSleep(List<T> chipList){
        int pesInSleep = 0;
        for (T chip : chipList) {
        	pesInSleep += PAMPeList.getPesInSleep((List<PAMPe>) chip.getPeList());
		}
        return pesInSleep;
    }
    @SuppressWarnings("unchecked")
	public static <T extends Chip> int getPesInWinkle(List<T> chipList){
        int pesInWinkle = 0;
        for (T chip : chipList) {
        	pesInWinkle += PAMPeList.getPesInWinkle((List<PAMPe>) chip.getPeList());
		}
        return pesInWinkle;
    }
    
	@SuppressWarnings("unchecked")
	public static <T extends Chip> int getPesInOff(List<T> chipList){
        int pesInOff = 0;
        for (T chip : chipList) {
        	pesInOff += PAMPeList.getPesInOff((List<PAMPe>) chip.getPeList());
		}
        return pesInOff;
    }

	public static void setState(List<Chip> chipList,int state) {
		for (Chip chip : chipList) {
			chip.setState(state);
		}
		
	}

    /**
     * Gets a chip with free Pe.
     *
     * @param chipList the chip list
     *
     * @return a chip object or <tt>null</tt> if not found
     *
     * @pre $none
     * @post $none
     */
    public static <T extends Chip> T getChipWithFreePe(List<T> chipList) {
        return getChipWithFreePe(chipList, 1);
    }

    /**
     * Gets a Chip with a specified number of free Pe.
     *
     * @param pesNumber the pes number
     * @param hostList the host list
     *
     * @return a chip object or <tt>null</tt> if not found
     *
     * @pre $none
     * @post $none
     */
    @SuppressWarnings("unchecked")
	public static <T extends Chip> T getChipWithFreePe(List<T> chipList, int pesNumber) {
        for (T chip : chipList) {
        	if (PAMPeList.getFreePesNumber((List<PAMPe>) chip.getPeList()) >= pesNumber) {
        		return chip;
        	}
		}
        return null;
    }

    /**
     * Sets the particular Pe status on a Chip.
     *
     * @param status   Pe status, either <tt>Pe.FREE</tt> or <tt>Pe.BUSY</tt>
     * @param chipId the chip id
     * @param peId the pe id
     * @param chipList the chip list
     *
     * @return <tt>true</tt> if the Pe status has changed, <tt>false</tt>
     * otherwise (Chip id or Pe id might not be exist)
     *
     * @pre chipID >= 0
     * @pre peID >= 0
     * @post $none
     */
    public static <T extends Chip> boolean setPeStatus(List<T> chipList, int status, int chipId, int peId) {
        T chip = getById(chipList, chipId);
        if (chip == null) {
            return false;
        }
        return chip.setPeStatus(peId, status);
    }
    

	/**
	 * Gets MIPS Rating for a specified Chip ID.
	 *
	 * @param id    the Pe ID
	 * @param peList the pe list
	 *
	 * @return the MIPS rating if exists, otherwise returns -1
	 *
	 * @pre id >= 0
	 * @post $none
	 */
	@SuppressWarnings("unchecked")
	public static <T extends Chip> int getMips(List<Chip> chipList, int id) {
		Chip chip = getById(chipList, id);
		if (chip != null) {
			
			return PAMPeList.getTotalMips((List<PAMPe>)chip.getPeList());
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
    public static <T extends Chip> int getTotalMips(List<Chip> chipList) {
    	int totalMips = 0;
    	for (Chip chip : chipList) {
    		totalMips += chip.getMips();
		}
    	return totalMips;
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
    public static <T extends Chip> void setStatusFailed(List<Chip> chipList, String resName, int hostId, boolean failed) {
        String status = null;
        if (failed) {
           status = "FAILED";
        } else {
           status = "WORKING";
    	}

        Log.printLine(resName + " - Machine: " + hostId + " is " + status);

        setStatusFailed(chipList, failed);
    }

    /**
     * Sets the status of PEs of this machine to FAILED.
     *
     * @param failed      the new value for the "failed" parameter
     * @param peList the pe list
     */
    public static <T extends Chip> void setStatusFailed(List<Chip> chipList, boolean failed) {
        // a loop to set the status of all the PEs in this machine
    	for (Chip chip : chipList) {
            if (failed) {
            	chip.setStatus(Chip.FAILED);
            } else {
            	chip.setStatus(Chip.FREE);
            }
		}
    }

}
