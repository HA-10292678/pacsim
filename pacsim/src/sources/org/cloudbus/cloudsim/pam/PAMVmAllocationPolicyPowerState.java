package org.cloudbus.cloudsim.pam;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.core.CloudSim;

/**
*
* @author		Srinivasan.S
* @version		CS 1.0
*/

public class PAMVmAllocationPolicyPowerState extends PAMVmAllocationPolicy {

	/** The vm table. */
	private Map<String, PAMHost> vmTable;

	/** The used pes. */
	private Map<String, Integer> usedPes;

	/** The free pes. */
	private List<Integer> freePes;
	
	private Map<Integer,Integer> hostToPesInNapMap;
   
	private Map<Integer,Integer> hostToPesMap;
	/**
	 * Creates the new VmAllocationPolicySimple object.
	 *
	 * @param list the list
	 *
	 * @pre $none
	 * @post $none
	 */
	public PAMVmAllocationPolicyPowerState(List<? extends PAMHost> list) {
		super(list);

		setFreePes(new ArrayList<Integer>());
		for (PAMHost host : getHostList()) {
			getFreePes().add(host.getPesNumber());

		}

		setVmTable(new HashMap<String, PAMHost>());
		setUsedPes(new HashMap<String, Integer>());
		setHostToPesInNapMap(new HashMap<Integer,Integer>());
		setHostToPesMap(new HashMap<Integer,Integer>());
	}

	/**
	 * Allocates a host for a given VM.
	 *
	 * @param vm VM specification
	 *
	 * @return $true if the host could be allocated; $false otherwise
	 *
	 * @pre $none
	 * @post $none
	 */
	@Override
	public boolean allocateHostForVm(PAMVm vm) {
		int requiredPes = vm.getPesNumber();
		boolean result = false;
		int tries = 0;
		int freePesInHost=0;
		
		for (PAMHost host : getHostList()) {
			freePesInHost = host.getFreePesNumber();
			hostToPesMap.put(host.getId(), freePesInHost);	
		}

		if (!getVmTable().containsKey(vm.getUid())) { //if this vm was not created
			do {//we still trying until we find a host or until we try all of them
				int moreFree = Integer.MIN_VALUE;
				int idx = -1;

				//we want the host with less pes in use
				freePesInHost=0;
				Iterator<Integer> it = hostToPesMap.keySet().iterator();
				while( it. hasNext() ){
					Integer hostId=it.next();
					freePesInHost=(Integer)hostToPesMap.get(hostId);
					if (freePesInHost > moreFree) {
						moreFree = freePesInHost;
						idx = hostId;
					}	
				}
	
					int pesInNap=0;
					PAMHost host = PAMHostList.getById(getHostList(), idx);
						if(host.getFreePesNumber()>=requiredPes){
						pesInNap=ChipList.getPesInNap(host.getChipList());
						hostToPesInNapMap.put(host.getId(), pesInNap);
						if(pesInNap>=requiredPes){
							result = host.vmCreate(vm);	
						}
						}
						//host.getPeList().get(0).setStatusBusy();
					
				if (result) { //if vm were succesfully created in the host
					//Log.printLine("VmAllocationPolicy: VM #"+vm.getVmId()+ "Chosen host: #"+host.getMachineID()+" idx:"+idx);

					getVmTable().put(vm.getUid(), host);
					getUsedPes().put(vm.getUid(), requiredPes);
					//getFreePes().set(idx, getFreePes().get(idx) - requiredPes);
					result = true;
					hostToPesInNapMap.clear();
					//System.out.println("Checking......... VM # "+vm.getUid()+ " PES in Nap in host id: "+host.getId()+" is "+pesInNap+" result: "+result);
					break;
				} else {
					hostToPesMap.put(host.getId(), Integer.MIN_VALUE);
				}
				tries++;
			} while (!result && tries <  getHostList().size());

		}

		if(result==true){
			
		return result;
		}else
		{
			int moreInNap=Integer.MIN_VALUE;
			int idx=-1;
			int remainingPes=0;
			if (!getVmTable().containsKey(vm.getUid())) {
			Iterator<Integer> it = hostToPesInNapMap.keySet().iterator();
				while( it. hasNext() ){
					Integer hostId=it.next();
					Integer pesInNap=(Integer)hostToPesInNapMap.get(hostId);
					if (pesInNap > moreInNap) {
						moreInNap = pesInNap;
						idx = hostId;
					}	
				}
			remainingPes=requiredPes-moreInNap;
		
			if(idx!=-1){
			PAMHost host = (PAMHost) getHostList().get(idx);
			if(host.getPesInWinkle()>=remainingPes){
				incVmRequestNotFullfilled();
			System.out.println(CloudSim.getClockTick()+": Pes in Nap is "+moreInNap+" in Host #"+idx+" for Vm #"+vm.getId()+" with required Pes: "+vm.getPesNumber());
			System.out.println(CloudSim.getClockTick()+": Moving "+remainingPes+" Pes in Host #"+idx+" from WRINKLE to NAP...");
			
			for(Chip chip:host.getChipList()){
				for(PAMPe pe:chip.getPeList()){
					if(remainingPes!=0){
						if(pe.getState()==IBMPowerState.WINKLE){
							pe.setState(IBMPowerState.NAP);
							remainingPes--;
						}
					}
				}
			}			
			result = host.vmCreate(vm);
			}else{
				incVmFailed();
				System.out.println(CloudSim.getClockTick()+": Vm #"+vm.getId()+" Failed with required Pes: "+vm.getPesNumber()+" in Host #"+idx+ " With free pes"+host.getFreePesNumber());
			}
			if (result) { //if vm were succesfully created in the host
				//Log.printLine("VmAllocationPolicy: VM #"+vm.getVmId()+ "Chosen host: #"+host.getMachineID()+" idx:"+idx);
				getVmTable().put(vm.getUid(), host);
				getUsedPes().put(vm.getUid(), requiredPes);
			//	getFreePes().set(idx, getFreePes().get(idx) - requiredPes);
				result = true;
				//break;
			}
			hostToPesInNapMap.clear();
			}else{
				incVmFailed();
				System.out.println(CloudSim.getClockTick()+": Vm Failed to create due to no required Pes available in all Hosts.....");
			}
			}
			return result;
		}
	}

	/**
	 * Releases the host used by a VM.
	 *
	 * @param vm the vm
	 *
	 * @pre $none
	 * @post none
	 */
	@Override
	public void deallocateHostForVm(PAMVm vm) {
		PAMHost host = getVmTable().remove(vm.getUid());
		if (host != null) {
			host.vmDestroy(vm);
		}
	}

	/**
	 * Gets the host that is executing the given VM belonging to the
	 * given user.
	 *
	 * @param vm the vm
	 *
	 * @return the Host with the given vmID and userID; $null if not found
	 *
	 * @pre $none
	 * @post $none
	 */
	@Override
	public PAMHost getHost(PAMVm vm) {
		return getVmTable().get(vm.getUid());
	}

	/**
	 * Gets the host that is executing the given VM belonging to the
	 * given user.
	 *
	 * @param vmId the vm id
	 * @param userId the user id
	 *
	 * @return the Host with the given vmID and userID; $null if not found
	 *
	 * @pre $none
	 * @post $none
	 */
	@Override
	public PAMHost getHost(int vmId, int userId) {
		return getVmTable().get(Vm.getUid(userId, vmId));
	}

	/**
	 * Gets the vm table.
	 *
	 * @return the vm table
	 */
	public Map<String, PAMHost> getVmTable() {
		return vmTable;
	}

	/**
	 * Sets the vm table.
	 *
	 * @param hashMap the vm table
	 */
	protected void setVmTable(HashMap<String, PAMHost> hashMap) {
		this.vmTable = hashMap;
	}

	/**
	 * Gets the used pes.
	 *
	 * @return the used pes
	 */
	protected Map<String, Integer> getUsedPes() {
		return usedPes;
	}

	/**
	 * Sets the used pes.
	 *
	 * @param usedPes the used pes
	 */
	protected void setUsedPes(Map<String, Integer> usedPes) {
		this.usedPes = usedPes;
	}

	/**
	 * Gets the free pes.
	 *
	 * @return the free pes
	 */
	protected List<Integer> getFreePes() {
		return freePes;
	}

	/**
	 * Sets the free pes.
	 *
	 * @param freePes the new free pes
	 */
	protected void setFreePes(List<Integer> freePes) {
		this.freePes = freePes;
	}

	/* (non-Javadoc)
	 * @see cloudsim.VmAllocationPolicy#optimizeAllocation(double, cloudsim.VmList, double)
	 */
	@Override
	public List<Map<String, Object>> optimizeAllocation(List<? extends PAMVm> vmList) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.cloudbus.cloudsim.VmAllocationPolicy#allocateHostForVm(org.cloudbus.cloudsim.Vm, org.cloudbus.cloudsim.Host)
	 */
	@Override
	public boolean allocateHostForVm(PAMVm vm, PAMHost host) {
	//	PAMHost phost=(PAMHost)host;
		if (host.vmCreate(vm)) { //if vm has been succesfully created in the host
			getVmTable().put(vm.getUid(), host);
			Log.formatLine("%.2f: VM #" + vm.getId() + " has been allocated to the host #" + host.getId(), CloudSim.clock());
			return true;
		}

		return false;
	}

	public void setHostToPesInNapMap(Map<Integer,Integer> hostToPesInNapMap) {
		this.hostToPesInNapMap = hostToPesInNapMap;
	}

	public Map<Integer,Integer> getHostToPesInNapMap() {
		return hostToPesInNapMap;
	}

	public Map<Integer, Integer> getHostToPesMap() {
		return hostToPesMap;
	}

	public void setHostToPesMap(Map<Integer, Integer> hostToPesMap) {
		this.hostToPesMap = hostToPesMap;
	}




}
