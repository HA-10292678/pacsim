package org.cloudbus.cloudsim.pam;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.cloudbus.cloudsim.Pe;



public class PAMVmSchedulerNonChipAware extends PAMVmScheduler{



	/** Map containing VM ID and a vector of PEs allocated to this VM. */
	private Map<String, List<String>> peAllocationMap;

	/** The free pes vector. */
	private Map<Chip,List<PAMPe>> pesToChipMap;
	
	private List<Chip> freeChips;
	
	private List<Pe> freePes;

	  /** Denotes Pe is FREE for allocation. */
    public static final int FREE  = 1;

    /** Denotes Pe is allocated and hence busy in processing Cloudlet. */
    public static final int BUSY = 2;

    /** Denotes Pe is failed and hence it can't process any Cloudlet at this moment. This Pe is failed because it belongs to a machine which is also failed. */
    public static final int FAILED = 3;
	/**
	 * Instantiates a new vm scheduler space shared.
	 *
	 * @param pelist the pelist
	 */
    private Map<String,List<Integer>> mappingVmToChip;
    
	public PAMVmSchedulerNonChipAware(List<? extends Chip> chipList) {
		super(chipList);
		setPeAllocationMap(new HashMap<String, List<String>>());
		setPesToChipMap(new HashMap<Chip,List<PAMPe>>());
		setMappingVmToChip(new HashMap<String,List<Integer>>());
		setPesToAllChipMap(chipList);
		setFreeChips(new ArrayList<Chip>());
		getFreeChips().addAll(chipList);
		
	}

	/* (non-Javadoc)
	 * @see org.cloudbus.cloudsim.VmScheduler#allocatePesForVm(org.cloudbus.cloudsim.Vm, java.util.List)
	 */

	@Override
	public boolean allocatePesForVm(PAMVm vm, List<Double> mipsShare) {
		//if there is no enough free PEs, fails
		if (getFreePesInHost() < mipsShare.size()) {
			return false;
		}
	
		double totalMips = 0;
		int allottedMips=0;
		int totalPesNeeded=vm.getPesNumber();
		//int pesInChip=getChipList().get(0).getPesNumber();
		//int chipsNeeded=totalPesNeeded/pesInChip;
		//int pesNeeded=totalPesNeeded%pesInChip;
		String chipPeId;
		List<String> selectedPes = new ArrayList<String>();
		List<Integer> selectedChips = new ArrayList<Integer>();
		
		for(Double mips:mipsShare){
			totalMips+=mips;
		}
		

		//int idx = -1;
		int requiredPes=0;
		List<Integer> cId=new ArrayList<Integer>();
		int freePesInChip=0;
		
		//if there is no chip that can allocate pes required by vm in it then spread it in different chips  
			
			for(Chip chip:getChipList()){
				if(freePesInChip<=totalPesNeeded){
					if(chip.getPesInNap()>0){
						freePesInChip+=chip.getPesInNap();
						cId.add(chip.getId());
					}
				}
			}
			
			for(int i=0;i<cId.size();i++){
				Chip chip= ChipList.getById(getChipList(), cId.get(i));
				chip.setChipState(IBMPowerState.ON);
				selectedChips.add(chip.getId());	
				for(PAMPe pe:ChipList.getById(getChipList(), cId.get(i)).getPeList()){
					if(requiredPes!=totalPesNeeded){
						if((pe.getStatus()==FREE)&&(pe.getState()==IBMPowerState.NAP)){
							chipPeId=getCPid(chip.getId(), pe.getId());
							allottedMips+=pe.getMips();
							pe.setStatusBusy();
							pe.setState(IBMPowerState.ON);
							requiredPes++;
							selectedPes.add(chipPeId);
						}
					}
				}
			
			}	
		

		
		if (mipsShare.size() > selectedPes.size()) {
			return false;
		}
		//System.out.println("Allotted Mips for vm id: "+vm.getId()+" : "+allottedMips);
		//System.out.println("Total Mips: "+totalMips);
		getMappingVmToChip().put(vm.getUid(), selectedChips);
		getPeAllocationMap().put(vm.getUid(), selectedPes);
		getMipsMap().put(vm.getUid(), mipsShare);
		setAvailableMips(getAvailableMips() - totalMips);
		
		return true;
	}

	/* (non-Javadoc)
	 * @see org.cloudbus.cloudsim.VmScheduler#deallocatePesForVm(org.cloudbus.cloudsim.Vm)
	 */
	@Override
	public void deallocatePesForVm(PAMVm vm) {
		List<String> cpIdList=getPeAllocationMap().get(vm.getUid());
		
		for(int i=0;i<cpIdList.size();i++){
			String str=cpIdList.get(i);
			StringTokenizer st=new StringTokenizer(str,"-");
			while(st.hasMoreTokens()){
				Integer chipId=Integer.valueOf(st.nextToken());
				Integer peId=Integer.valueOf(st.nextToken());
				Chip chip=ChipList.getById(getChipList(), chipId);
				/*if(chip.getStatus()==BUSY){
					chip.setStatusFree();
				}*/
				PAMPe pe=PAMPeList.getById(chip.getPeList(), peId);
				pe.setStatusFree();
				pe.setState(IBMPowerState.NAP);
				if(chip.getPesInNap()==chip.getPesNumber()){
					chip.setChipState(IBMPowerState.NAP);
				}
			
				
			}
		}
		cpIdList.clear();
		
		getPeAllocationMap().remove(vm.getUid());
		double totalMips = 0;
		for (double mips : getMipsMap().get(vm.getUid())) {
			totalMips += mips;
		}
		setAvailableMips(getAvailableMips() + totalMips);

		getMipsMap().remove(vm.getUid());
	}

	/**
	 * Sets the pe allocation map.
	 *
	 * @param peAllocationMap the pe allocation map
	 */
	protected void setPeAllocationMap(Map<String, List<String>> peAllocationMap) {
		this.peAllocationMap = peAllocationMap;
	}
	
	public List<String> getPesMappedToVm(PAMVm vm){
		List<String> cpIdList=getPeAllocationMap().get(vm.getUid());
		return cpIdList;
	}
	

	/**
	 * Gets the pe allocation map.
	 *
	 * @return the pe allocation map
	 */
	protected Map<String, List<String>> getPeAllocationMap() {
		return peAllocationMap;
	}

	/**
	 * Sets the free pes vector.
	 *
	 * @param freePes the new free pes vector
	 */
	protected void setFreePes(List<Pe> freePes) {
		this.freePes = freePes;
	}

	/**
	 * Gets the free pes vector.
	 *
	 * @return the free pes vector
	 */
	protected List<Pe> getFreePes() {
		return freePes;
	}

	public void setPesToChipMap(Map<Chip,List<PAMPe>> pesToChipMap) {
		this.pesToChipMap = pesToChipMap;
	}

	public Map<Chip,List<PAMPe>> getPesToChipMap() {
		return pesToChipMap;
	}
	
	@SuppressWarnings("unchecked")
	public void setPesToAllChipMap(List<? extends Chip> chipList){
		for(Chip chip:chipList){
			getPesToChipMap().put(chip, (List<PAMPe>)chip.getPeList());
		}
	}

	public int getFreePesInHost() {

		return ChipList.getFreePesNumber(getChipList());
	}

	public void setFreeChips(List<Chip> freeChips) {
		this.freeChips = freeChips;
	}

	public List<Chip> getFreeChips() {
		return freeChips;
	}
	
	public static String getCPid(int chipId, int peId) {
		return chipId + "-" + peId;
	}

	public void setMappingVmToChip(Map<String,List<Integer>> mappingVmToChip) {
		this.mappingVmToChip = mappingVmToChip;
	}

	public Map<String,List<Integer>> getMappingVmToChip() {
		return mappingVmToChip;
	}
	
	public int vmsCreatedAcrossChip(){
		int acrossChips=0;
		Iterator<String> it = getMappingVmToChip().keySet().iterator();
		while( it. hasNext() ){
			String uId=it.next();
			List<Integer> chips=getMappingVmToChip().get(uId);
			int noOfChips= chips.size();
			if(noOfChips>1)
				acrossChips++;
		}
		return acrossChips;	
	}
	
	public int vmsCreatedWithinChip(){
		int acrossChips=0;
		Iterator<String> it = getMappingVmToChip().keySet().iterator();
		while( it. hasNext() ){
			String uId=it.next();
			List<Integer> chips=getMappingVmToChip().get(uId);
			int noOfChips= chips.size();
			if(noOfChips==1)
				acrossChips++;
		}
		return acrossChips;	
	}

	@Override
	public boolean allocatePesForVm(PAMVm vm, List<Integer> selectedChipList,
			List<Double> mipsShare) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void computeDynamicScheduling() {
		// TODO Auto-generated method stub
		
	}
}
