package org.cloudbus.cloudsim.pam;

/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2010, The University of Melbourne, Australia
 */



import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cloudbus.cloudsim.Log;


/**
 * VmScheduler is an abstract class that represents the
 * policy used by a VMM to share processing power among VMs running
 * in a host.
 *
 * @author		Srinivasan.S	
 * @version		CS 1.0
 */
public abstract class PAMVmScheduler {

	/** The peList. */
	private List<? extends Chip> chipList;

	/** The MIPS that are currently allocated to the VMs. */
	private Map<String, List<Double>> mipsMap;

	/** The total available mips. */
	private double availableMips;

	/**
	 * Creates a new HostAllocationPolicy.
	 *
	 * @param pelist the pelist
	 *
	 * @pre peList != $null
	 * @post $none
	 */
	public PAMVmScheduler(List<? extends Chip> chipList) {
		setChipList(chipList);
		setMipsMap(new HashMap<String, List<Double>>());
		setAvailableMips(ChipList.getTotalMips(getChipList()));
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
	public abstract boolean allocatePesForVm(PAMVm vm, List<Double> mipsShare);

	public abstract void  computeDynamicScheduling();
	
	public abstract boolean allocatePesForVm(PAMVm vm, List<Integer> selectedChipList, List<Double> mipsShare);
	
	/**
	 * Releases PEs allocated to a VM.
	 *
	 * @param vm the vm
	 *
	 * @pre $none
	 * @post $none
	 */
	public abstract void deallocatePesForVm(PAMVm vm);

	/**
	 * Releases PEs allocated to all the VMs.
	 *
	 * @param vm the vm
	 *
	 * @pre $none
	 * @post $none
	 */
	public void deallocatePesForAllVms() {
		getMipsMap().clear();
		setAvailableMips(ChipList.getTotalMips(getChipList()));
		for (Chip chip : getChipList()) {
			for(PAMPe pe: chip.getPeList()){ //note: want to implement chipProvisioner
			pe.getPeProvisioner().deallocateMipsForAllVms();
			}
		}
	
	}
	
	public abstract int vmsCreatedAcrossChip();
	
	public abstract int vmsCreatedWithinChip();

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
		return getMipsMap().get(vm.getUid());
	}

	/**
	 * Gets the total allocated MIPS for a VM over all the PEs.
	 *
	 * @param vm the vm
	 *
	 * @return the allocated mips for vm
	 */
	public double getTotalAllocatedMipsForVm(PAMVm vm) {
		double allocated = 0;
		List<Double> mipsMap = getAllocatedMipsForVm(vm);
		if (mipsMap != null) {
			for (double mips : mipsMap) {
				allocated += mips;
			}
		}
		return allocated;
	}

	/**
	 * Returns maximum available MIPS among all the PEs.
	 *
	 * @return max mips
	 */
	public double getMaxAvailableMips() {
		if (getChipList() == null) {
			Log.printLine("Chip list is empty");
			return 0;
		}

		double max = 0.0;
		for(Chip chip : getChipList()){
			for (PAMPe pe : chip.getPeList()) {
				double tmp = pe.getPeProvisioner().getAvailableMips();
				if (tmp > max) {
					max = tmp;
				}
			}
		}

		return max;
	}

	
	/**
	 * Returns Chip capacity in MIPS.
	 *
	 * @return mips
	 */
	public double getChipCapacity() {
		if (getChipList()== null) {
			Log.printLine("Chip list is empty");
			return 0;
		}
		return getChipList().get(0).getMips();
	}
	
	/**
	 * Returns PE capacity in MIPS.
	 *
	 * @return mips
	 */
	public double getPeCapacity() {
		if (getChipList()== null) {
			Log.printLine("Chip list is empty");
			return 0;
		}
		return getChipList().get(0).getPeList().get(0).getMips();
	}

	/**
	 * Gets the vm list.
	 *
	 * @return the vm list
	 */
	@SuppressWarnings("unchecked")
	public <T extends Chip> List<T> getChipList() {
		return (List<T>) chipList;
	}

	/**
	 * Sets the vm list.
	 *
	 * @param peList the pe list
	 */
	protected <T extends Chip> void setChipList(List<T> chipList) {
		this.chipList = chipList;
	}

	/**
	 * Gets the mips map.
	 *
	 * @return the mips map
	 */
	protected Map<String, List<Double>> getMipsMap() {
		return mipsMap;
	}

	/**
	 * Sets the mips map.
	 *
	 * @param mipsMap the mips map
	 */
	protected void setMipsMap(Map<String, List<Double>> mipsMap) {
		this.mipsMap = mipsMap;
	}

	/**
	 * Gets the free mips.
	 *
	 * @return the free mips
	 */
	public double getAvailableMips() {
		return availableMips;
	}

	/**
	 * Sets the free mips.
	 *
	 * @param availableMips the new free mips
	 */
	protected void setAvailableMips(double availableMips) {
		this.availableMips = availableMips;
	}

}
