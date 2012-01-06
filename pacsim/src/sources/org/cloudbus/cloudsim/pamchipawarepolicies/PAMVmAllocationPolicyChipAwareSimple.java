package org.cloudbus.cloudsim.pamchipawarepolicies;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.pam.Chip;
import org.cloudbus.cloudsim.pam.ChipList;
import org.cloudbus.cloudsim.pam.IBMPowerState;
import org.cloudbus.cloudsim.pam.PAMHost;
import org.cloudbus.cloudsim.pam.PAMHostList;
import org.cloudbus.cloudsim.pam.PAMPe;
import org.cloudbus.cloudsim.pam.PAMVm;
import org.cloudbus.cloudsim.pam.PAMVmAllocationPolicy;

public class PAMVmAllocationPolicyChipAwareSimple extends PAMVmAllocationPolicy{
	
	/** The vm table. */
	private Map<String, PAMHost> vmTable;

	/** The used pes. */
	private Map<String, Integer> usedPes;

	/** The free pes. */
	
	private Map<Integer,Integer> hostToPesInNapMap;
   
	private Map<Integer,Integer> hostToPesMap;
	
	public PAMVmAllocationPolicyChipAwareSimple(List<? extends PAMHost> list) {
		super(list);
		

		setVmTable(new HashMap<String, PAMHost>());
		setUsedPes(new HashMap<String, Integer>());
		setHostToPesInNapMap(new HashMap<Integer,Integer>());
		setHostToPesMap(new HashMap<Integer,Integer>());
	}

	@Override
	public boolean allocateHostForVm(PAMVm vm) {
		
		int requiredPes = vm.getPesNumber();
		boolean result = false;
		int tries = 0;
		//List<Integer> freePesTmp = new ArrayList<Integer>();
		int pesInNap=0;
		int pesInWinkle=0;
		int idx=-1;
		int remainingPes=0;
		int freePesInHost=0;
		
		/*for(PAMHost host: getHostList()){
			pesInNap=ChipList.getPesInNap(host.getChipList());
			hostToPesInNapMap.put(host.getId(), pesInNap);				
		}*/
		for (PAMHost host : getHostList()) {
			freePesInHost = host.getFreePesNumber();
			hostToPesInNapMap.put(host.getId(), freePesInHost);	
		}
		
		
		if (!getVmTable().containsKey(vm.getUid())) {
			do{
				pesInNap=0;
				Iterator<Integer> it = hostToPesInNapMap.keySet().iterator();
				int moreInNap=Integer.MIN_VALUE;
				while( it. hasNext() ){
					Integer hostId=it.next();
					pesInNap=(Integer)hostToPesInNapMap.get(hostId);
					if (pesInNap > moreInNap) {
						moreInNap = pesInNap;
						idx = hostId;
					}	
				}
				
				PAMHost host = PAMHostList.getById(getHostList(), idx);
				int totalPesNeeded=vm.getPesNumber();
				int pesInChip=host.getChipList().get(0).getPesNumber();
				int chipsNeeded=(totalPesNeeded/pesInChip);
				int pesNeeded=totalPesNeeded%pesInChip;
				int pesToNap=0;
				List<Integer> selectedChips = new ArrayList<Integer>();
				
				if(moreInNap>=requiredPes){
					
					for(Chip chip:host.getChipList()){
						if(chipsNeeded>0){
							if(chip.getState()==IBMPowerState.NAP){
								selectedChips.add(chip.getId());
								chipsNeeded--;
								totalPesNeeded-=chip.getPesInNap();
							}
						/*	if(chip.getPesInNap()==chip.getPesNumber()){
								selectedChips.add(chip.getId());
								chipsNeeded--;
								totalPesNeeded-=chip.getPesInNap();
							}*/
						}		
					}
					
					if(host.getPesInWinkle()>=requiredPes){
					
					if(chipsNeeded>0){
						for(Chip chip:host.getChipList()){
							if(chipsNeeded>0){
								if((chip.getPesInNap()+chip.getPesInWinkle())==chip.getPesNumber()){
									for(PAMPe pe:chip.getPeList()){
										if(pe.getState()==IBMPowerState.WINKLE){
												pe.setState(IBMPowerState.NAP);
												pesToNap++;
											}
									}
									selectedChips.add(chip.getId());
									chipsNeeded--;
									totalPesNeeded-=chip.getPesInNap();
								}
							}		
						}
					}
					}
					int moreFree = Integer.MIN_VALUE;
					int idcx = -1;
					
					if(pesNeeded>0){
					//selects the chip to allocate pe for vm
		loop1:			for(Chip chip:host.getChipList()){
						for(Integer chipId:selectedChips){
							if(chipId==chip.getId()){
								continue loop1;
							}
						}
							if((chip.getPesInNap()>=pesNeeded)){
								if(chip.getPesInNap()>moreFree){
									moreFree=chip.getPesInNap();
									idcx=chip.getId();
								}
							}
						}
					}
					
					if(idcx==-1){
						if(host.getPesInWinkle()>=requiredPes){
						loop1:for(Chip chip:host.getChipList()){
								for(Integer chipId:selectedChips){
									if(chipId==chip.getId()){
										continue loop1;
									}
								}
									if((chip.getPesInNap()+chip.getPesInWinkle())>=pesNeeded){
										if(chip.getPesInNap()>moreFree){
											moreFree=chip.getPesInNap();
											idcx=chip.getId();
										}
									}
								}
							if(idcx!=-1){
							Chip chip= ChipList.getById(host.getChipList(), idcx);
							remainingPes=pesNeeded-chip.getPesInNap();
							for(PAMPe pe:chip.getPeList()){
								if(remainingPes!=0){
									if(pe.getState()==IBMPowerState.WINKLE){
										pe.setState(IBMPowerState.NAP);
										pesToNap++;
										remainingPes--;
									}
								}
							}
							totalPesNeeded-=pesNeeded;
							selectedChips.add(idcx);
							}
						}
						}else{
							totalPesNeeded-=pesNeeded;
							selectedChips.add(idcx);
							
						}
					loop1:for(Chip chip:host.getChipList()){
						for(Integer chipId:selectedChips){
							if(chipId==chip.getId()){
								continue loop1;
							}
						}
						if(pesToNap!=0){
							for(PAMPe pe:chip.getPeList()){
								if(pe.getState()==IBMPowerState.NAP){
									pe.setState(IBMPowerState.WINKLE);
									pesToNap--;
								}
							}	
						}
					}
					
					//allocate in the selected chips
					if(totalPesNeeded==0){
						result = host.vmCreate(vm,selectedChips);
					} /*else{
						System.out.println(CloudSim.getClockTick()+": Context Switching.............. ");
						selectedChips.clear();
						totalPesNeeded=vm.getPesNumber();
						chipsNeeded=(totalPesNeeded/pesInChip);
						pesNeeded=totalPesNeeded%pesInChip;
						pesToNap=0;
						
						for(Chip chip:host.getChipList()){
							if(chipsNeeded>0){
								if(chip.getState()==IBMPowerState.NAP){
									selectedChips.add(chip.getId());
									chipsNeeded--;
									totalPesNeeded-=chip.getPesInNap();
								}
							}		
						}
						
						if(chipsNeeded>0){
							for(Chip chip:host.getChipList()){
								if(chipsNeeded>0){
									if((chip.getPesInNap()+chip.getPesInWinkle())==chip.getPesNumber()){
										for(PAMPe pe:chip.getPeList()){
											if(pe.getState()==IBMPowerState.WINKLE){
													pe.setState(IBMPowerState.NAP);
													pesToNap++;
												}
										}
										selectedChips.add(chip.getId());
										chipsNeeded--;
										totalPesNeeded-=chip.getPesInNap();
									}
								}		
							}
						}
						
						 moreFree = Integer.MIN_VALUE;
						 idcx = -1;
						if(pesNeeded>0){
						//selects the chip to allocate pe for vm
			loop1:			for(Chip chip:host.getChipList()){
							for(Integer chipId:selectedChips){
								if(chipId==chip.getId()){
									continue loop1;
								}
							}
								if((chip.getPesInNap()>=pesNeeded)){
									if(chip.getPesInNap()>moreFree){
										moreFree=chip.getPesInNap();
										idcx=chip.getId();
									}
								}
							}
						}
						
						if(idcx==-1){
						loop1:for(Chip chip:host.getChipList()){
								for(Integer chipId:selectedChips){
									if(chipId==chip.getId()){
										continue loop1;
									}
								}
									if((chip.getPesInNap()+chip.getPesInWinkle())>=pesNeeded){
										if(chip.getPesInNap()>moreFree){
											moreFree=chip.getPesInNap();
											idcx=chip.getId();
										}
									}
								}
						
							Chip chip= ChipList.getById(host.getChipList(), idcx);
							remainingPes=pesNeeded-chip.getPesInNap();
							for(PAMPe pe:chip.getPeList()){
								if(remainingPes!=0){
									if(pe.getState()==IBMPowerState.WINKLE){
										pe.setState(IBMPowerState.NAP);
										pesToNap++;
										remainingPes--;
									}
								}
							}
							totalPesNeeded-=pesNeeded;
							selectedChips.add(idcx);
						}else{
							totalPesNeeded-=pesNeeded;
							selectedChips.add(idcx);
							
						}
						
						loop1:for(Chip chip:host.getChipList()){
								for(Integer chipId:selectedChips){
									if(chipId==chip.getId()){
										continue loop1;
									}
								}
								if(pesToNap!=0){
									for(PAMPe pe:chip.getPeList()){
										if(pe.getState()==IBMPowerState.NAP){
											pe.setState(IBMPowerState.WINKLE);
											pesToNap--;
										}
									}	
								}
							}
						if(totalPesNeeded==0){
						result = host.vmCreate(vm,selectedChips);
						}
					}*/

				}else{
					remainingPes=requiredPes-moreInNap;
					pesInWinkle=ChipList.getPesInWinkle(host.getChipList());
					if(pesInWinkle>=remainingPes){
						incVmRequestNotFullfilled();
						System.out.println(CloudSim.getClockTick()+": Pes in Nap is "+moreInNap+" in Host #"+idx+" for Vm #"+vm.getId()+" with required Pes: "+vm.getPesNumber());
						System.out.println(CloudSim.getClockTick()+": Taking Time to Move "+remainingPes+" Pes in Host #"+idx+" from WRINKLE to NAP...");
					
						for(Chip chip:host.getChipList()){
							if(chipsNeeded>0){
								if(chip.getState()==IBMPowerState.NAP){
									selectedChips.add(chip.getId());
									chipsNeeded--;
									totalPesNeeded-=chip.getPesInNap();
								}
							}		
						}
						
						if(chipsNeeded>0){
							for(Chip chip:host.getChipList()){
								if(chipsNeeded>0){
									if((chip.getPesInNap()+chip.getPesInWinkle())==chip.getPesNumber()){
										for(PAMPe pe:chip.getPeList()){
											if(pe.getState()==IBMPowerState.WINKLE){
													pe.setState(IBMPowerState.NAP);
													pesToNap++;
												}
										}
										selectedChips.add(chip.getId());
										chipsNeeded--;
										totalPesNeeded-=chip.getPesInNap();
									}
								}		
							}
						}
						
						 int moreFree = Integer.MIN_VALUE;
						 int idcx = -1;
						if(pesNeeded>0){
						//selects the chip to allocate pe for vm
			loop1:			for(Chip chip:host.getChipList()){
							for(Integer chipId:selectedChips){
								if(chipId==chip.getId()){
									continue loop1;
								}
							}
								if((chip.getPesInNap()>=pesNeeded)){
									if(chip.getPesInNap()>moreFree){
										moreFree=chip.getPesInNap();
										idcx=chip.getId();
									}
								}
							}
						}
						
						if(idcx==-1){
						loop1:for(Chip chip:host.getChipList()){
								for(Integer chipId:selectedChips){
									if(chipId==chip.getId()){
										continue loop1;
									}
								}
									if((chip.getPesInNap()+chip.getPesInWinkle())>=pesNeeded){
										if(chip.getPesInNap()>moreFree){
											moreFree=chip.getPesInNap();
											idcx=chip.getId();
										}
									}
								}
						
							Chip chip= ChipList.getById(host.getChipList(), idcx);
							remainingPes=pesNeeded-chip.getPesInNap();
							for(PAMPe pe:chip.getPeList()){
								if(remainingPes!=0){
									if(pe.getState()==IBMPowerState.WINKLE){
										pe.setState(IBMPowerState.NAP);
										pesToNap++;
										remainingPes--;
									}
								}
							}
							totalPesNeeded-=pesNeeded;
							selectedChips.add(idcx);
						}else{
							totalPesNeeded-=pesNeeded;
							selectedChips.add(idcx);
							
						}
						if(totalPesNeeded==0){
						result = host.vmCreate(vm,selectedChips);
						}
					}else{
						hostToPesInNapMap.put(host.getId(), Integer.MIN_VALUE);
					}
				}
				if (result) { //if vm were succesfully created in the host
					//Log.printLine("VmAllocationPolicy: VM #"+vm.getVmId()+ "Chosen host: #"+host.getMachineID()+" idx:"+idx);
					getVmTable().put(vm.getUid(), host);
					getUsedPes().put(vm.getUid(), requiredPes);
					result = true;
					break;
				} 
				tries++;
			
			}while(!result && tries < getHostList().size());
		}
		return result;
	}

	@Override
	public boolean allocateHostForVm(PAMVm vm, PAMHost host) {
		if (host.vmCreate(vm)) { //if vm has been succesfully created in the host
			getVmTable().put(vm.getUid(), host);
			Log.formatLine("%.2f: VM #" + vm.getId() + " has been allocated to the host #" + host.getId(), CloudSim.clock());
			return true;
		}

		return false;
	}

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
	@Override
	public List<Map<String, Object>> optimizeAllocation(
			List<? extends PAMVm> vmList) {
		// TODO Auto-generated method stub
		return null;
	}

	public void setUsedPes(Map<String, Integer> usedPes) {
		this.usedPes = usedPes;
	}

	public Map<String, Integer> getUsedPes() {
		return usedPes;
	}

	public void setVmTable(Map<String, PAMHost> vmTable) {
		this.vmTable = vmTable;
	}

	public Map<String, PAMHost> getVmTable() {
		return vmTable;
	}

	public Map<Integer, Integer> getHostToPesInNapMap() {
		return hostToPesInNapMap;
	}

	public void setHostToPesInNapMap(Map<Integer, Integer> hostToPesInNapMap) {
		this.hostToPesInNapMap = hostToPesInNapMap;
	}

	public Map<Integer, Integer> getHostToPesMap() {
		return hostToPesMap;
	}

	public void setHostToPesMap(Map<Integer, Integer> hostToPesMap) {
		this.hostToPesMap = hostToPesMap;
	}

	
}
