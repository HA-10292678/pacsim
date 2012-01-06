package org.cloudbus.cloudsim.pam;

import java.util.List;




public class Chip {
	
	/** chip id*/
	private int id;
	
	  /** Denotes Chip is FREE for allocation. */
    public static final int FREE  = 1;

    /** Denotes Chip is allocated and hence busy in processing Cloudlet. */
    public static final int BUSY = 2;

    /** Denotes Chip is failed and hence it can't process any Cloudlet at this moment. This Pe is failed because it belongs to a machine which is also failed. */
    public static final int FAILED = 3;
	
    /** The status of Chip: FREE, BUSY, FAILED: . */
    private int status;
    
    /** The pe list. */
	private List<? extends PAMPe> peList;
	
	/** state of the chip */
	private int state;
	
	private int mips;
	

	@SuppressWarnings("unchecked")
	public Chip(int Id,int state,List<? extends PAMPe> peList){
		
		setId(Id);
	
		setPeList(peList);
		setMips(PAMPeList.getTotalMips((List<PAMPe>) peList));
		setState(state);
		
	}
	
	@SuppressWarnings("unchecked")
	public Chip(int Id,List<? extends PAMPe> peList){
		
		setId(Id);
	
		setPeList(peList);
		setMips(PAMPeList.getTotalMips((List<PAMPe>) peList));
		
	}

	/**
	 * Gets the pes number.
	 *
	 * @return the pes number
	 */
	public int getPesNumber() {
		return getPeList().size();
	}

	/**
	 * Gets the free pes number.
	 *
	 * @return the free pes number
	 */
	@SuppressWarnings("unchecked")
	public int getFreePesNumber() {
		return PAMPeList.getFreePesNumber((List<PAMPe>) getPeList());
	}
	
	@SuppressWarnings("unchecked")
	public int getBusyPesNumber(){
		return PAMPeList.getBusyPesNumber((List<PAMPe>) getPeList());
	}

	@SuppressWarnings("unchecked")
	public int getPesInNap(){
		
		return PAMPeList.getPesInNap((List<PAMPe>) getPeList());
	}
	
	
	@SuppressWarnings("unchecked")
	public int getPesInWinkle(){	
		return PAMPeList.getPesInWinkle((List<PAMPe>) getPeList());
	}
	
	@SuppressWarnings("unchecked")
	public int getPesInOn(){	
		return PAMPeList.getPesInOn((List<PAMPe>) getPeList());
	}
	
//included
	 @SuppressWarnings("unchecked")
	public boolean setPeStatus(int peId, int status) {
	    return PAMPeList.setPeStatus((List<PAMPe>) getPeList(), peId, status);
	 }
	
	 


	    /**
	     * Sets Chip status to free, meaning it is available for processing.
	     * This should be used by SPACE shared hostList only.
	     *
	     * @pre $none
	     * @post $none
	     */
	    public void setStatusFree() {
	        setStatus(FREE);
	        
	    }

	    /**
	     * Sets Chip status to busy, meaning it is already executing Cloudlets.
	     * This should be used by SPACE shared hostList only.
	     *
	     * @pre $none
	     * @post $none
	     */
	    public void setStatusBusy() {
	    	setStatus(BUSY);
	    }

	    /**
	     * Sets this Chip to FAILED.
	     *
	     * @pre $none
	     * @post $none
	     */
	    public void setStatusFailed() {
	    	setStatus(FAILED);
	    }

	    /**
	     * Sets Chip status to either <tt>Chip.FREE</tt> or <tt>Chip.BUSY</tt>
	     *
	     * @param status     Chip status, <tt>true</tt> if it is FREE, <tt>false</tt>
	     * if BUSY.
	     *
	     * @pre $none
	     * @post $none
	     */
	    @SuppressWarnings("unchecked")
		public void setStatus(int status) {
	        this.status = status;
	        PAMPeList.setStatus((List<PAMPe>) getPeList(),status);
	    }

		 /**
	     * Gets the status of this Chip.
	     *
	     * @return the status of this Chip
	     *
	     * @pre $none
	     * @post $none
	     */
		public int getStatus() {
			return status;
		}

	//attributes setters and getters..
	public void setId(int id) {
		this.id = id;
	}


	public int getId() {
		return id;
	}
	
	public void setPeList(List<? extends PAMPe> peList) {
		this.peList = peList;
	}


	public List<? extends PAMPe> getPeList() {
		return peList;
	}

	public void setChipState(int state){
		this.state=state;
	}
	@SuppressWarnings("unchecked")
	public void setState(int state) {
		this.state = state;
		PAMPeList.setState((List<PAMPe>) getPeList(), state);
	}


	public int getState() {
		return state;
	}



	public int getMips() {

		return mips;
	}

	public void setMips(int mips) {
		this.mips = mips;
	}


	
}
