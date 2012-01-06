

package org.cloudbus.cloudsim.pam;

import java.util.ArrayList;
import java.util.List;

import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.provisioners.BwProvisioner;
import org.cloudbus.cloudsim.provisioners.RamProvisioner;

/**
 *
 * @author		Srinivasan.S
 * @version		CS 1.0
 */

public class PAMHost{
	

	/** The id. */
	private int id;

	/** The storage. */
	private long storage;

	/** The ram provisioner. */
	private RamProvisioner ramProvisioner;

	/** The bw provisioner. */
	private BwProvisioner bwProvisioner;

	/** The vm list. */
	private List<? extends PAMVm> vmList;


    /** Tells whether this machine is working properly or has failed. */
    private boolean failed;

	/** The vms migrating in. */
	private List<PAMVm> vmsMigratingIn;
	
	private int state;
	
	/** The chip list. */
	private List<? extends Chip> chipList;

	 
	 private PAMVmScheduler vmScheduler;
	/**
	 * Instantiates a new host.
	 *
	 * @param id the id
	 * @param storage the storage
	 * @param ramProvisioner the ram provisioner
	 * @param bwProvisioner the bw provisioner
	 * @param peList the pe list
	 * @param vmScheduler the vm scheduler
	 */
	public PAMHost(int id,
				RamProvisioner ramProvisioner,
				BwProvisioner bwProvisioner,
				long storage,
				List<? extends Chip> chipList,
				PAMVmScheduler vmScheduler,
				int State) {
		
		setId(id);
		setRamProvisioner(ramProvisioner);
		setBwProvisioner(bwProvisioner);
		setStorage(storage);
		setVmScheduler(vmScheduler);
		setVmList(new ArrayList<PAMVm>());
		
		setChipList(chipList);
		setState(State);
		setFailed(false);

	}
	public PAMHost(int id,
			RamProvisioner ramProvisioner,
			BwProvisioner bwProvisioner,
			long storage,
			List<? extends Chip> chipList,
			PAMVmScheduler vmScheduler) {
	
	setId(id);
	setRamProvisioner(ramProvisioner);
	setBwProvisioner(bwProvisioner);
	setStorage(storage);
	setVmScheduler(vmScheduler);
	setVmList(new ArrayList<PAMVm>());
	
	setChipList(chipList);
	setFailed(false);

}
	/**
	 * Requests updating of processing of cloudlets in the VMs running in this host.
	 *
	 * @param currentTime the current time
	 *
	 * @return 		expected time of completion of the next cloudlet in all VMs in this host. Double.MAX_VALUE
	 * if there is no future events expected in this host
	 *
	 * @pre 		currentTime >= 0.0
	 * @post 		$none
	 */
	public double updateVmsProcessing(double currentTime) {
		double smallerTime = Double.MAX_VALUE;

		for (PAMVm vm : getVmList()) {
//			if (vm.isInMigration()) {
//				continue;
//			}
			double time = vm.updateVmProcessing(currentTime, getVmScheduler().getAllocatedMipsForVm(vm));
			if (time > 0.0 && time < smallerTime) {
				smallerTime = time;
			}
		}

		return smallerTime;
	}

	public void addMigratingInVm(PAMVm vm) {
		if (!getVmsMigratingIn().contains(vm)) {
			getRamProvisioner().allocateRamForVm(vm, vm.getCurrentRequestedRam());
			getBwProvisioner().allocateBwForVm(vm, vm.getCurrentRequestedBw());
			getVmsMigratingIn().add(vm);
		}
	}

	public void removeMigratingInVm(PAMVm vm) {
		getRamProvisioner().deallocateRamForVm(vm);
		getBwProvisioner().deallocateBwForVm(vm);
		getVmsMigratingIn().remove(vm);
	}

	public void reallocateMigratingVms() {
		for (PAMVm vm : getVmsMigratingIn()) {
			getRamProvisioner().allocateRamForVm(vm, vm.getCurrentRequestedRam());
			getBwProvisioner().allocateBwForVm(vm, vm.getCurrentRequestedBw());
		}
	}
	
	/**
	 * Checks if is suitable for vm.
	 *
	 * @param vm the vm
	 *
	 * @return true, if is suitable for vm
	 */
	public boolean isSuitableForVm(PAMVm vm) {
		return (getVmScheduler().getAvailableMips() >= vm.getCurrentRequestedTotalMips() &&
				getRamProvisioner().isSuitableForVm(vm, vm.getCurrentRequestedRam()) &&
				getBwProvisioner().isSuitableForVm(vm, vm.getCurrentRequestedBw()));
	}


	/**
	 * Allocates PEs and memory to a new VM in the Host.
	 *
	 * @param vm Vm being started
	 *
	 * @return $true if the VM could be started in the host; $false otherwise
	 *
	 * @pre $none
	 * @post $none
	 */
	public boolean vmCreate(PAMVm vm) {
		
		//System.out.println("Host Id: "+getId());
		if (!getRamProvisioner().allocateRamForVm(vm, vm.getCurrentRequestedRam())) {
			Log.printLine("Allocation of VM #" + vm.getId() + " to Host #" + getId() + " failed by RAM");
			return false;
		}

		if (!getBwProvisioner().allocateBwForVm(vm, vm.getCurrentRequestedBw())) {
			Log.printLine("Allocation of VM #" + vm.getId() + " to Host #" + getId() + " failed by BW");
			getRamProvisioner().deallocateRamForVm(vm);
			return false;
		}

		if (!getVmScheduler().allocatePesForVm(vm, vm.getCurrentRequestedMips())) {
			Log.printLine("[VmScheduler.vmCreate] Allocation of VM #" + vm.getId() + " to Host #" + getId() + " failed by MIPS");
			getRamProvisioner().deallocateRamForVm(vm);
			getBwProvisioner().deallocateBwForVm(vm);
			return false;
		}
	
		this.setHostState(IBMPowerState.ON);

		getVmList().add(vm);
		vm.setPAMHost(this);
		return true;
	}

	public boolean vmCreate(PAMVm vm,List<Integer> selectedChipList) {
		
		//System.out.println("Host Id: "+getId());
		if (!getRamProvisioner().allocateRamForVm(vm, vm.getCurrentRequestedRam())) {
			Log.printLine("Allocation of VM #" + vm.getId() + " to Host #" + getId() + " failed by RAM");
			return false;
		}

		if (!getBwProvisioner().allocateBwForVm(vm, vm.getCurrentRequestedBw())) {
			Log.printLine("Allocation of VM #" + vm.getId() + " to Host #" + getId() + " failed by BW");
			getRamProvisioner().deallocateRamForVm(vm);
			return false;
		}

		if (!getVmScheduler().allocatePesForVm(vm, selectedChipList, vm.getCurrentRequestedMips())) {
			Log.printLine("[VmScheduler.vmCreate] Allocation of VM #" + vm.getId() + " to Host #" + getId() + " failed by MIPS");
			getRamProvisioner().deallocateRamForVm(vm);
			getBwProvisioner().deallocateBwForVm(vm);
			return false;
		}
	
		this.setHostState(IBMPowerState.ON);

		getVmList().add(vm);
		vm.setPAMHost(this);
		return true;
	}
	/**
	 * Destroys a VM running in the host.
	 *
	 * @param vm the VM
	 *
	 * @pre $none
	 * @post $none
	 */
	public void vmDestroy(PAMVm vm) {
		if (vm != null) {
			vmDeallocate(vm);
			getVmList().remove(vm);
			((PAMVm) vm).setPAMHost(null);
			if(this.getPesInNap()==this.getPesNumber()){
			this.setHostState(IBMPowerState.NAP);
			}
		}
		 
			
	}
	
	public void computeDynamicScheduling(){
		
	}

	/**
	 * Destroys all VMs running in the host.
	 *
	 * @pre $none
	 * @post $none
	 */
	public void vmDestroyAll() {
		vmDeallocateAll();
		for (PAMVm vm : getVmList()) {
			((PAMVm) vm).setPAMHost(null);
		}
		getVmList().clear();
	}

	/**
	 * Deallocate all hostList for the VM.
	 *
	 * @param vm the VM
	 */
	protected void vmDeallocate(PAMVm vm) {
		getRamProvisioner().deallocateRamForVm(vm);
		getBwProvisioner().deallocateBwForVm(vm);
		getVmScheduler().deallocatePesForVm(vm);
	}

	/**
	 * Deallocate all hostList for the VM.
	 *
	 * @param vm the VM
	 */
	protected void vmDeallocateAll() {
		getRamProvisioner().deallocateRamForAllVms();
		getBwProvisioner().deallocateBwForAllVms();
		getVmScheduler().deallocatePesForAllVms();
	}

	/**
	 * Returns a VM object.
	 *
	 * @param vmId the vm id
	 * @param userId ID of VM's owner
	 *
	 * @return the virtual machine object, $null if not found
	 *
	 * @pre $none
	 * @post $none
	 */
	public PAMVm getVm(int vmId, int userId){
		for (PAMVm vm : getVmList()) {
			if (vm.getId() == vmId && vm.getUserId() == userId) {
				return vm;
			}
		}
		return null;
	}


	@SuppressWarnings("unchecked")
	public int getPesNumber(){
		return ChipList.getPesNumber((List<Chip>) getChipList());
	}
	/**
	 * Gets the free pes number.
	 *
	 * @return the free pes number
	 */
	@SuppressWarnings("unchecked")
	public int getFreePesNumber() {
		return ChipList.getFreePesNumber((List<Chip>) getChipList());
	}

	
	@SuppressWarnings("unchecked")
	public int getBusyPesNumber(){
		return ChipList.getBusyPesNumber((List<Chip>) getChipList());
	}
	/**
	 * Gets the total mips.
	 *
	 * @return the total mips
	 */
	@SuppressWarnings("unchecked")
	public int getTotalMips() {
		return ChipList.getTotalMips((List<Chip>)getChipList());
	}
	
	/**
	 * Allocates PEs for a VM.
	 *
	 * @param vm the vm
	 * @param mipsShare the mips share
	 *
	 * @return $true if this policy allows a new VM in the host, $false otherwise
	 *
	 * @pre $none
	 * @post $none
	 */
	public boolean allocatePesForVm(PAMVm vm, List<Double> mipsShare) {
		return getVmScheduler().allocatePesForVm(vm, mipsShare);
	}

	/**
	 * Releases PEs allocated to a VM.
	 *
	 * @param vm the vm
	 *
	 * @pre $none
	 * @post $none
	 */
	public void deallocatePesForVm(PAMVm vm) {
		getVmScheduler().deallocatePesForVm(vm);
	}

	/**
	 * Returns the MIPS share of each Pe that is allocated to a given VM.
	 *
	 * @param vm the vm
	 *
	 * @return an array containing the amount of MIPS of each pe that is available to the VM
	 *
	 * @pre $none
	 * @post $none
	 */
	public List<Double> getAllocatedMipsForVm(PAMVm vm) {
		return getVmScheduler().getAllocatedMipsForVm(vm);
	}

	/**
	 * Gets the total allocated MIPS for a VM over all the PEs.
	 *
	 * @param vm the vm
	 *
	 * @return the allocated mips for vm
	 */
	public double getTotalAllocatedMipsForVm(PAMVm vm) {
		return getVmScheduler().getTotalAllocatedMipsForVm(vm);
	}

	/**
	 * Returns maximum available MIPS among all the PEs.
	 *
	 * @return max mips
	 */
	public double getMaxAvailableMips() {
		return getVmScheduler().getMaxAvailableMips();
	}

	/**
	 * Gets the free mips.
	 *
	 * @return the free mips
	 */
	public double getAvailableMips() {
		return getVmScheduler().getAvailableMips();
	}

	/**
	 * Gets the machine bw.
	 *
	 * @return the machine bw
	 *
	 * @pre $none
	 * @post $result > 0
	 */
	public long getBw() {
		return getBwProvisioner().getBw();
	}

	/**
	 * Gets the machine memory.
	 *
	 * @return the machine memory
	 *
	 * @pre $none
	 * @post $result > 0
	 */
	public int getRam() {
		return getRamProvisioner().getRam();
	}

	/**
	 * Gets the machine storage.
	 *
	 * @return the machine storage
	 *
	 * @pre $none
	 * @post $result > 0
	 */
	public long getStorage() {
		return storage;
	}

	/**
	 * Gets the id.
	 *
	 * @return the id
	 */
	public int getId() {
		return id;
	}

	/**
	 * Sets the id.
	 *
	 * @param id the new id
	 */
	protected void setId(int id) {
		this.id = id;
	}

	/**
	 * Gets the ram provisioner.
	 *
	 * @return the ram provisioner
	 */
	public RamProvisioner getRamProvisioner() {
		return ramProvisioner;
	}

	/**
	 * Sets the ram provisioner.
	 *
	 * @param ramProvisioner the new ram provisioner
	 */
	protected void setRamProvisioner(RamProvisioner ramProvisioner) {
		this.ramProvisioner = ramProvisioner;
	}

	/**
	 * Gets the bw provisioner.
	 *
	 * @return the bw provisioner
	 */
	public BwProvisioner getBwProvisioner() {
		return bwProvisioner;
	}

	/**
	 * Sets the bw provisioner.
	 *
	 * @param bwProvisioner the new bw provisioner
	 */
	protected void setBwProvisioner(BwProvisioner bwProvisioner) {
		this.bwProvisioner = bwProvisioner;
	}
	/**
	 * Gets the vm list.
	 *
	 * @return the vm list
	 */
	@SuppressWarnings("unchecked")
	public <T extends PAMVm> List<T> getVmList() {
		return (List<T>) vmList;
	}

	/**
	 * Sets the vm list.
	 *
	 * @param vmList the new vm list
	 */
	protected <T extends PAMVm> void setVmList(List<T> vmList) {
		this.vmList = vmList;
	}

	/**
	 * Sets the storage.
	 *
	 * @param storage the new storage
	 */
	protected void setStorage(long storage) {
		this.storage = storage;
	}
	/**
	 * Gets the vms migrating in.
	 *
	 * @return the vms migrating in
	 */
    public List<PAMVm> getVmsMigratingIn() {
		return vmsMigratingIn;
	}

	/**
	 * Sets the vms migrating in.
	 *
	 * @param vmsMigratingIn the new vms migrating in
	 */
	protected void setVmsMigratingIn(List<PAMVm> vmsMigratingIn) {
		this.vmsMigratingIn = vmsMigratingIn;
	}


	public <T extends Chip> boolean setPeStatus(List<T> chipList,int chipId,int peId, int status) {
	    
		return ChipList.setPeStatus(chipList,status, chipId,peId);
	 }

	/**
	 * Checks if is failed.
	 *
	 * @return true, if is failed
	 */
	public boolean isFailed() {
		return failed;
	}

    /**
     * Sets the PEs of this machine to a FAILED status.
     * NOTE: <tt>resName</tt> is used for debugging purposes,
     * which is <b>ON</b> by default.
     * Use {@link #setFailed(boolean)} if you do not want
     * this information.
     *
     * @param resName   the name of the resource
     * @param failed the failed
     *
     * @return <tt>true</tt> if successful, <tt>false</tt> otherwise
     */
    @SuppressWarnings("unchecked")
	public boolean setFailed(String resName, boolean failed) {
        // all the PEs are failed (or recovered, depending on fail)
    	this.failed = failed;
        ChipList.setStatusFailed((List<Chip>)getChipList(), resName, getId(), failed);
        return true;
    }

    /**
     * Sets the PEs of this machine to a FAILED status.
     *
     * @param failed the failed
     *
     * @return <tt>true</tt> if successful, <tt>false</tt> otherwise
     */
    @SuppressWarnings("unchecked")
	public boolean setFailed(boolean failed) {
        // all the PEs are failed (or recovered, depending on fail)
    	this.failed = failed;
        ChipList.setStatusFailed((List<Chip>) getChipList(), failed);
        return true;
    }


	
	
	
	
	//attributes getters and setters
	@SuppressWarnings("unchecked")
	public void setState(int state) {
		this.state = state;
		ChipList.setState((List<Chip>) getChipList(), state);
	}
	
	public void setHostState(int state){
		this.state=state;
	}
	
	@SuppressWarnings("unchecked")
	public int getPesInNap() {
		return ChipList.getPesInNap((List<Chip>) getChipList());
	}

	@SuppressWarnings("unchecked")
	public int getPesInWinkle() {
		return ChipList.getPesInWinkle((List<Chip>) getChipList());
	}

	@SuppressWarnings("unchecked")
	public int getPesInOn() {
		return ChipList.getPesInOn((List<Chip>) getChipList());
	}

	
	public int getState() {
		return state;
	}


	public void setChipList(List<? extends Chip> chipList) {
		this.chipList = chipList;
	}

	public List<? extends Chip> getChipList() {
		return chipList;
	}

	public void setVmScheduler(PAMVmScheduler vmScheduler) {
		this.vmScheduler = vmScheduler;
	}

	public PAMVmScheduler getVmScheduler() {
		return vmScheduler;
	}


	
}
