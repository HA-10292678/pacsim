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

public class PAMVmAllocationPolicyWorstFit extends PAMVmAllocationPolicy {

	/** The vm table. */
	private Map<String, PAMHost> vmTable;

	/** The used pes. */
	private Map<String, Integer> usedPes;

	/** The free pes. */
	private List<Integer> freePes;
	
	private Map<Integer,Integer> hostToPesMap;
   
	/**
	 * Creates the new VmAllocationPolicySimple object.
	 *
	 * @param list the list
	 *
	 * @pre $none
	 * @post $none
	 */
	public PAMVmAllocationPolicyWorstFit(List<? extends PAMHost> list) {
		super(list);

		setFreePes(new ArrayList<Integer>());
		for (PAMHost host : getHostList()) {
			getFreePes().add(host.getPesNumber());

		}

		setVmTable(new HashMap<String, PAMHost>());
		setUsedPes(new HashMap<String, Integer>());
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
		//List<Integer> freePesTmp = new ArrayList<Integer>();
		int freePesInHost=0;
		int idx=-1;
		int remainingPes=0;
		int pesInNap=0;
		for(PAMHost host: getHostList()){
			freePesInHost=host.getFreePesNumber();
			hostToPesMap.put(host.getId(), freePesInHost);	
		}
		if (!getVmTable().containsKey(vm.getUid())) {
			do{
				freePesInHost=0;
				Iterator<Integer> it = hostToPesMap.keySet().iterator();
				int lessFreePes=Integer.MAX_VALUE;
				while( it. hasNext() ){
					Integer hostId=it.next();
					freePesInHost=(Integer)hostToPesMap.get(hostId);
					if (freePesInHost < lessFreePes) {
						lessFreePes = freePesInHost;
						idx = hostId;
					}	
				}
		
				PAMHost host = PAMHostList.getById(getHostList(), idx);
				if(lessFreePes>=requiredPes){
					pesInNap=ChipList.getPesInNap(host.getChipList());
					if(pesInNap>=requiredPes){
						result = host.vmCreate(vm);
					}else{
						remainingPes=requiredPes-pesInNap;
						incVmRequestNotFullfilled();
							System.out.println(CloudSim.getClockTick()+": Pes in Nap is "+pesInNap+" in Host #"+host.getId()+" for Vm #"+vm.getId()+" with required Pes: "+vm.getPesNumber());
							System.out.println(CloudSim.getClockTick()+": Taking Time to Move "+remainingPes+" Pes in Host #"+host.getId()+" from WRINKLE to NAP...");
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
						}
				
				}else{
					//System.out.println("pes in host"+idx+" is "+lessFreePes);
					hostToPesMap.put(host.getId(), Integer.MAX_VALUE);
					//System.out.println("pes in host"+idx+" is "+hostToPesMap.get(host.getId()));
					}
				if (result) { //if vm were succesfully created in the host
					//Log.printLine("VmAllocationPolicy: VM #"+vm.getVmId()+ "Chosen host: #"+host.getMachineID()+" idx:"+idx);
					getVmTable().put(vm.getUid(), host);
					getUsedPes().put(vm.getUid(), requiredPes);
					//getFreePes().set(idx, getFreePes().get(idx) - requiredPes);
					result = true;
					break;
				} 
				tries++;
			
			}while(!result && tries < getHostList().size());
		}
		if(result==false){
			incVmFailed();
			System.out.println(CloudSim.getClockTick()+": Vm #"+vm.getId()+" Failed with required Pes: "+vm.getPesNumber());
	}
		return result;
		
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
	//	int idx = getHostList().indexOf(host);
		//int pes = getUsedPes().remove(vm.getUid());
		if (host != null) {
			host.vmDestroy(vm);
			//getFreePes().set(idx, getFreePes().get(idx) + pes);
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

	public void setHostToPesMap(Map<Integer,Integer> hostToPesMap) {
		this.hostToPesMap = hostToPesMap;
	}

	public Map<Integer,Integer> getHostToPesMap() {
		return hostToPesMap;
	}




}
