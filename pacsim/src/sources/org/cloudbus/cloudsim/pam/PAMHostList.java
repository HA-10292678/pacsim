package org.cloudbus.cloudsim.pam;

import java.util.List;

public class PAMHostList {

	/**
	 * Gets the Machine object for a particular ID.
	 *
	 * @param id    the host ID
	 * @param hostList the host list
	 *
	 * @return the Machine object or <tt>null</tt> if no machine exists
	 *
	 * @see gridsim.Machine
	 * @pre id >= 0
	 * @post $none
	 */
    public static <T extends PAMHost> T getById(List<T> hostList, int id) {
        for (T host : hostList) {
    		if (host.getId() == id) {
    			return host;
    		}
		}
        return null;
    }

    /**
     * Gets the total number of PEs for all Machines.
     *
     * @param hostList the host list
     *
     * @return number of PEs
     *
     * @pre $none
     * @post $result >= 0
     */
    @SuppressWarnings("unchecked")
	public static <T extends PAMHost> int getPesNumber(List<T> hostList) {
        int pesNumber = 0;
        for (T host : hostList) {
        		pesNumber += ChipList.getPesNumber((List<Chip>)host.getChipList());
		}
        return pesNumber;
    }

    /**
     * Gets the total number of <tt>FREE</tt> or non-busy PEs for all Machines.
     *
     * @param hostList the host list
     *
     * @return number of PEs
     *
     * @pre $none
     * @post $result >= 0
     */
    @SuppressWarnings("unchecked")
	public static <T extends PAMHost> int getFreePesNumber(List<T> hostList) {
        int freePesNumber = 0;
        for (T host : hostList) {
        	freePesNumber += ChipList.getFreePesNumber((List<Chip>)host.getChipList());
        }
        return freePesNumber;
    }

    /**
     * Gets the total number of <tt>BUSY</tt> PEs for all Machines.
     *
     * @param hostList the host list
     *
     * @return number of PEs
     *
     * @pre $none
     * @post $result >= 0
     */
    @SuppressWarnings("unchecked")
	public static <T extends PAMHost> int getBusyPesNumber(List<T> hostList) {
        int busyPesNumber = 0;
        for (T host : hostList) {
        	busyPesNumber += ChipList.getBusyPesNumber((List<Chip>)host.getChipList());
		}
        return busyPesNumber;
    }

    /**
     * Gets a Machine with free Pe.
     *
     * @param hostList the host list
     *
     * @return a machine object or <tt>null</tt> if not found
     *
     * @pre $none
     * @post $none
     */
    public static <T extends PAMHost> T getHostWithFreePe(List<T> hostList) {
        return getHostWithFreePe(hostList, 1);
    }

    /**
     * Gets a Machine with a specified number of free Pe.
     *
     * @param pesNumber the pes number
     * @param hostList the host list
     *
     * @return a machine object or <tt>null</tt> if not found
     *
     * @pre $none
     * @post $none
     */
    @SuppressWarnings("unchecked")
	public static <T extends PAMHost> T getHostWithFreePe(List<T> hostList, int pesNumber) {
        for (T host : hostList) {
        	if (ChipList.getFreePesNumber((List<Chip>) host.getChipList()) >= pesNumber) {
        		return host;
        	}
		}
        return null;
    }

    /**
     * Sets the particular Pe status on a Machine.
     *
     * @param status   Pe status, either <tt>Pe.FREE</tt> or <tt>Pe.BUSY</tt>
     * @param hostId the host id
     * @param peId the pe id
     * @param hostList the host list
     *
     * @return <tt>true</tt> if the Pe status has changed, <tt>false</tt>
     * otherwise (Machine id or Pe id might not be exist)
     *
     * @pre machineID >= 0
     * @pre peID >= 0
     * @post $none
     */
   public static <T extends PAMHost> boolean setPeStatus(List<T> hostList, int status, int hostId, int chipId,int peId) {
        T host = getById(hostList, hostId);
        
        if (host == null) {
            return false;
        }
        return host.setPeStatus(host.getChipList(),chipId,peId, status);
    }
   

   @SuppressWarnings("unchecked")
   public static <T extends PAMHost> int getPesInOn(List<T> hostList){
   	   int pesInOn = 0;
    		for (T host : hostList) {
    			pesInOn+=ChipList.getPesInOn((List<Chip>)host.getChipList());
   		}
    		return pesInOn;
      }
      
   
   @SuppressWarnings("unchecked")
   public static <T extends PAMHost> int getPesInNap(List<T> hostList){
       int pesInNap = 0;
   		for (T host : hostList) {
   			pesInNap+=ChipList.getPesInNap((List<Chip>)host.getChipList());
		}
   		return pesInNap;
   }
   
   @SuppressWarnings("unchecked")
   public static <T extends PAMHost> int getPesInSleep(List<T> hostList){
	   int pesInSleep = 0;
  		for (T host : hostList) {
  			pesInSleep+=ChipList.getPesInSleep((List<Chip>)host.getChipList());
		}
  		return pesInSleep;
   }
   @SuppressWarnings("unchecked")
   public static <T extends PAMHost> int getPesInWinkle(List<T> hostList){
	   int pesInWinkle = 0;
 		for (T host : hostList) {
 			pesInWinkle+=ChipList.getPesInWinkle((List<Chip>)host.getChipList());
		}
 		return pesInWinkle;
   }
   
   @SuppressWarnings("unchecked")
   public static <T extends PAMHost> int getPesInOff(List<T> hostList){
   	   int pesInOff = 0;
    		for (T host : hostList) {
    			pesInOff+=ChipList.getPesInOff((List<Chip>)host.getChipList());
   		}
    		return pesInOff;
      }
      
 
   
   public static void setState(List<PAMHost> hostList,int state) {
		for (PAMHost host : hostList) {
			host.setState(state);
		}
		
	}

}
