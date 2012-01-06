package org.cloudbus.cloudsim.pam;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.cloudbus.cloudsim.Pe;



public class PAMVmSchedulerSpaceShared extends PAMVmScheduler{



	/** Map containing VM ID and a vector of PEs allocated to this VM. */
	private Map<String, List<String>> peAllocationMap;
	
	private Map<String, String> chipAllocationMap;

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
    
	public PAMVmSchedulerSpaceShared(List<? extends Chip> chipList) {
		super(chipList);
		setPeAllocationMap(new HashMap<String, List<String>>());
		setPesToChipMap(new HashMap<Chip,List<PAMPe>>());
		setMappingVmToChip(new HashMap<String,List<Integer>>());
		setChipAllocationMap(new HashMap<String, String>());
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
		int pesInChip=getChipList().get(0).getPesNumber();
		int chipsNeeded=totalPesNeeded/pesInChip;
		int pesNeeded=totalPesNeeded%pesInChip;
		String chipPeId;
		List<String> selectedPes = new ArrayList<String>();
		List<Integer> selectedChips = new ArrayList<Integer>();
		
		for(Double mips:mipsShare){
			totalMips+=mips;
		}
		
		for(Chip chip:getChipList()){
			if(chipsNeeded>0){
				if(chip.getPesInNap()==chip.getPesNumber()){
					chip.setChipState(IBMPowerState.ON);
					selectedChips.add(chip.getId());
					//maps pe id and chip id to the vm
					for(PAMPe pe:chip.getPeList()){
						chipPeId=getCPid(chip.getId(), pe.getId());
						selectedPes.add(chipPeId);
						getChipAllocationMap().put(chipPeId,vm.getUid());  // chip to vm map
						pe.setStatusBusy();
						pe.setState(IBMPowerState.ON);
					}
					allottedMips+=chip.getMips();
					chipsNeeded--;
					totalPesNeeded-=chip.getPesInNap();
					//chip.setStatusBusy();
				}
			}		
		}
		if(chipsNeeded>0){
			pesNeeded+=chipsNeeded*pesInChip;
		}
		

		int lessFree = Integer.MAX_VALUE;
		int idx = -1;
		//selects the chip to allocate pe for vm
	loop1:	for(Chip chip:getChipList()){
			for(Integer chipId:selectedChips){
				if(chipId==chip.getId()){
					continue loop1;
				}
			}
			if((chip.getPesInNap()>=pesNeeded)&&(pesNeeded>0)){
				if(chip.getPesInNap()<lessFree){
					lessFree=chip.getPesInNap();
					idx=chip.getId();
				}
			}
		}
		//System.out.println("Choosed PE: "+idx);
		int requiredPes=0;
		//allocates pes to vm in the selected chip
		if(idx!=-1){
			Chip chip= ChipList.getById(getChipList(), idx);
			chip.setChipState(IBMPowerState.ON);
			selectedChips.add(chip.getId());		
		for(PAMPe pe:ChipList.getById(getChipList(), idx).getPeList()){
			if(requiredPes!=pesNeeded){
				if((pe.getStatus()==FREE)&&(pe.getState()==IBMPowerState.NAP)){
					chipPeId=getCPid(idx, pe.getId());
					allottedMips+=pe.getMips();
					pe.setStatusBusy();
					pe.setState(IBMPowerState.ON);
					requiredPes++;
					getChipAllocationMap().put(chipPeId,vm.getUid()); // chip to vm map
					selectedPes.add(chipPeId);
				}
			}
		}
		pesNeeded-=requiredPes;
		}

		List<Integer> cId=new ArrayList<Integer>();
		int freePesInChip=0;
		requiredPes=0;
		//if there is no chip that can allocate pes required by vm in it then spread it in different chips  
		if((idx==-1)&&(pesNeeded>0)){
			
			for(Chip chip:getChipList()){
				if(freePesInChip<=pesNeeded){
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
					if(requiredPes!=pesNeeded){
						if((pe.getStatus()==FREE)&&(pe.getState()==IBMPowerState.NAP)){
							chipPeId=getCPid(chip.getId(), pe.getId());
							allottedMips+=pe.getMips();
							pe.setStatusBusy();
							pe.setState(IBMPowerState.ON);
							requiredPes++;
							getChipAllocationMap().put(chipPeId,vm.getUid()); // chip to vm map
							selectedPes.add(chipPeId);
						}
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
		/*List<String> cpIdList=getPeAllocationMap().get(vm.getUid());
		
		for(int i=0;i<cpIdList.size();i++){
			String str=cpIdList.get(i);
			StringTokenizer st=new StringTokenizer(str,"-");
			while(st.hasMoreTokens()){
				Integer chipId=Integer.valueOf(st.nextToken());
				Integer peId=Integer.valueOf(st.nextToken());
				Chip chip=ChipList.getById(getChipList(), chipId);
				PAMPe pe=PAMPeList.getById(chip.getPeList(), peId);
				pe.setStatusFree();
				pe.setState(IBMPowerState.NAP);
				if(chip.getPesInNap()==chip.getPesNumber()){
					chip.setChipState(IBMPowerState.NAP);
				}	
			}
		}
		cpIdList.clear();
		*/
		
		getPeAllocationMap().remove(vm.getUid());
		
		
		double totalMips = 0;
		for (double mips : getMipsMap().get(vm.getUid())) {
			totalMips += mips;
		}
		setAvailableMips(getAvailableMips() + totalMips);

		getMipsMap().remove(vm.getUid());
		
		
		List<String> tempPeChipList = new ArrayList<String>();
		for(Map.Entry<String, String>entry:getChipAllocationMap().entrySet()){
			String peChipId = entry.getKey();
			String vmId = entry.getValue();
			//System.out.println(peChipId +" === "+vmId); 
			if(vmId.equals(vm.getUid())){
				tempPeChipList.add(peChipId);
			}
		}
		
		for(String peChipId:tempPeChipList){
			String str=peChipId;
			StringTokenizer st=new StringTokenizer(str,"-");
			while(st.hasMoreTokens()){
				Integer chipId=Integer.valueOf(st.nextToken());
				Integer peId=Integer.valueOf(st.nextToken());
				Chip chip=ChipList.getById(getChipList(), chipId);
				PAMPe pe=PAMPeList.getById(chip.getPeList(), peId);
				pe.setStatusFree();
				pe.setState(IBMPowerState.NAP);
				if(chip.getPesInNap()==chip.getPesNumber()){
					chip.setChipState(IBMPowerState.NAP);
				}	
			}
		}
		
		tempPeChipList.clear();
		
		
	}

	
	public void computeDynamicScheduling(){
		//Map<Integer,Integer> tempChipList = new HashMap<Integer,Integer>();
		Integer tempBusyPes[] = new Integer[10];
		Integer tempFreePes[] = new Integer[10];
		Integer chipId[] = new Integer[10];
		List<Chip> chip = new ArrayList<Chip>();
		List<Integer> donorList = new ArrayList<Integer>();
		List<Integer> receiverList = new ArrayList<Integer>();
		chip = getChipList();
		
		for(int i=0;i<chip.size();i++){
			chipId[i]=chip.get(i).getId();
			tempFreePes[i] = chip.get(i).getFreePesNumber();
			tempBusyPes[i] = chip.get(i).getBusyPesNumber();
		}
		
		for(int i=0;i<chipId.length;i++){
			for(int j=i;j<chipId.length;j++){
				if(tempBusyPes[i]>tempBusyPes[j]){ //check which sort happens (>)-ascending, (<)-descending
					int temp = tempBusyPes[j];
					tempBusyPes[j] = tempBusyPes[i];
					tempBusyPes[i] = temp;
					int tempFree = tempFreePes[j];
					tempFreePes[j] = tempFreePes[i];
					tempFreePes[i]=tempFree;
					int tempId = chipId[j];
					chipId[j] = chipId[i];
					chipId[i] = tempId;
				}
			}
		}
		
		int freePeNo = 0;
		int busyPeNo = 0;
		for(int i=0;i<chipId.length;i++){
			busyPeNo = 0;
			freePeNo += tempFreePes[i];
			receiverList.add(chipId[i]);
			for(int j=i+1;j<chipId.length;j++){
				donorList.add(chipId[j]);
				busyPeNo += tempBusyPes[j];
			}
			if(freePeNo>=busyPeNo){
				break;
			}
			donorList.clear();
		}
		
		//Collections.sort(donorList,Collections.reverseOrder());
		
		for(int i=0;i<donorList.size();i++){
			System.out.println("$$$$$$$$$$ Donor Chip ID: "+donorList.get(i));
		}
		for(int i=0;i<receiverList.size();i++){
			System.out.println("$$$$$$$$$$ Receiver Chip ID: "+receiverList.get(i));
		}  
		
		//finds the free pes in receiver chip list and adds to the recvChipPeIdList
		List<String> recvChipPeIdList = new ArrayList<String>();
		for(Integer recvChipId:receiverList){
			Chip tempChip= ChipList.getById(getChipList(), recvChipId);		
			for(PAMPe pe:tempChip.getPeList()){
				if((pe.getStatus()==FREE)){ 			//&&(pe.getState()==IBMPowerState.NAP
					String chipPeId=getCPid(tempChip.getId(), pe.getId());
					recvChipPeIdList.add(chipPeId);        //clear the list at the end of the method 
				}
			}
		}
		
		//finds the busy pes in donor chip list and adds to the donorChipPeIdList
		List<String> donorChipPeIdList = new ArrayList<String>();
		for(Integer donorChipId:donorList){
			Chip tempChip= ChipList.getById(getChipList(), donorChipId);		
			for(PAMPe pe:tempChip.getPeList()){
				if((pe.getStatus()==BUSY)){ 			//&&(pe.getState()==IBMPowerState.NAP
					String chipPeId=getCPid(tempChip.getId(), pe.getId());
					donorChipPeIdList.add(chipPeId);        //clear the list at the end of the method 
				}
			}
		}
		
		//finds the vms that want for context switching
		List<String> toSwitchVmIdList = new ArrayList<String>();  //clear at the end
		for(Map.Entry<String, String>entry:getChipAllocationMap().entrySet()){
			String peChipId = entry.getKey();
			String vmId = entry.getValue();
			//System.out.println(peChipId +" === "+vmId); 
			for(String donorChipPeId:donorChipPeIdList){
				if(donorChipPeId.equals(peChipId)){
					toSwitchVmIdList.add(vmId);
				}
			}
		}
		
		//changes the states of all the donor chip pes to nap
		for(String peChipId:donorChipPeIdList){
			String str=peChipId;
			StringTokenizer st=new StringTokenizer(str,"-");
			while(st.hasMoreTokens()){
				Integer donorChipId=Integer.valueOf(st.nextToken());
				Integer donorPeId=Integer.valueOf(st.nextToken());
				Chip donorChip=ChipList.getById(getChipList(), donorChipId);
				PAMPe pe=PAMPeList.getById(donorChip.getPeList(), donorPeId);
				pe.setStatusFree();
				pe.setState(IBMPowerState.NAP);							// can change the state to winkle because its a long run both pe and donorChip state
				if(donorChip.getPesInNap()==donorChip.getPesNumber()){
					donorChip.setChipState(IBMPowerState.NAP);
				}	
			}		
			getChipAllocationMap().remove(peChipId);
		}
		
		// maps all the vm to the new chip(receiver chip) and changes the state to ON
		for(int i=0;i<toSwitchVmIdList.size();i++){
			String chipPeId = recvChipPeIdList.get(i);
			String vmId = toSwitchVmIdList.get(i);
			getChipAllocationMap().put(chipPeId,vmId);
			
			String str=chipPeId;
			StringTokenizer st=new StringTokenizer(str,"-");
			while(st.hasMoreTokens()){
				Integer recvChipId=Integer.valueOf(st.nextToken());
				Integer recvPeId=Integer.valueOf(st.nextToken());
				Chip recvChip=ChipList.getById(getChipList(), recvChipId);
				PAMPe pe=PAMPeList.getById(recvChip.getPeList(), recvPeId);
				pe.setStatusBusy();
				pe.setState(IBMPowerState.ON);							// can change the state to winkle because its a long run both pe and donorChip state
			}		
			
		}

		/*List<String> vmList = new ArrayList<String>();
		for(Map.Entry<String, List<String>>entry:getPeAllocationMap().entrySet()){
			String vmId =entry.getKey();
			List<String> peChipIdList = entry.getValue();
			
			for(int i=0;i<peChipIdList.size();i++){
				String str=peChipIdList.get(i);
				StringTokenizer st=new StringTokenizer(str,"-");
				while(st.hasMoreTokens()){
					Integer tempChipId=Integer.valueOf(st.nextToken());
				//	Integer tempPeId=Integer.valueOf(st.nextToken());
					for(Integer donorId:donorList){
						if(donorId == tempChipId){
							vmList.add(vmId);
							break;
						}
					}
				}
			}
		}*/

		
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

	public void setChipAllocationMap(Map<String, String> chipAllocationMap) {
		this.chipAllocationMap = chipAllocationMap;
	}

	public Map<String, String> getChipAllocationMap() {
		return chipAllocationMap;
	}
}
