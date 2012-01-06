/**
 * 
 */
package org.cloudbus.cloudsim.pam;



import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.core.SimEntity;
import org.cloudbus.cloudsim.core.SimEvent;
import org.cloudbus.cloudsim.Log;

/**
 * @author vasan
 *
 */


public class DCManager extends SimEntity {

	
	/** The vm list. */
	private List<PAMVm> vmList;
	
	/** The vms created list. */
	private List<PAMVm> vmsCreatedList;
	
	private List<PAMVm> vmSubmitted;
	
	/** The vms to datacenters map. */
	private Map<Integer, Integer> vmsToDatacentersMap;
	
	private Map<Integer, Integer> busyPesList;
	
	private List<String> durationToPes;
	
	
	private static int dcId;
	
	private static String dcName;
	
	private PAMDatacenter datacenter;
	
	private String regionalCisName;
	
	private int brokerId;
	
	private PAMDatacenterCharacteristics characteristics;
	
	private int vmsRequested;
	
	private int vmsAcks;
	
	private int vmsDestroyed;
	
	private static boolean cflag=true;
	
	private static boolean dflag=true;
	
	private static int listsSubmitted=0;
	
	private List<String> inNapState;
	
	private List<String> inWinkleState;
	
	private Integer[] predictionList;

	private static boolean enterOnce=false;
	
	private static int batchSize=0;
	
	private static int freePes=0;
	
	private static int busyPes=0;
	
	private static int waitClock=0;
	
	private static boolean run=true;
	
	private static boolean sortAsec=false;
	
	private static boolean sortDesc=false;
	
	private static final int asec=1;
	
	private static final int desc=2;
	
	private static int transitionToNap = 0;
	
	private static int transitionToWinkle = 0;
	
	private static int dynamicCount =0;
	
	  /** Denotes Pe is FREE for allocation. */
    public static final int FREE  = 1;

    /** Denotes Pe is allocated and hence busy in processing Cloudlet. */
    public static final int BUSY = 2;

    /** Denotes Pe is failed and hence it can't process any Cloudlet at this moment. This Pe is failed because it belongs to a machine which is also failed. */
    public static final int FAILED = 3;
    
	public DCManager(String name, PAMDatacenter datacenter0) {
		super(name);
		
		setBatchSize(0);
		setVmsRequested(0);
		setVmsAcks(0);
		setDatacenter(datacenter0);
		setDcId(getDatacenter().getId());
		setDcName(getDatacenter().getName());
		setVmsToDatacentersMap(new HashMap<Integer, Integer>());
		setBusyPesList(new HashMap<Integer, Integer>());
		setVmList(new ArrayList<PAMVm>()) ;
		setVmsCreatedList(new ArrayList<PAMVm>());
		setVmSubmitted(new ArrayList<PAMVm>());
		setInNapState(new ArrayList<String>());
		setInWinkleState(new ArrayList<String>());
		setPredictionList(new Integer[3]);
		getDatacenter().setDcManager(this);
		setDurationToPes(new ArrayList<String>());
		
	}







	@Override
	public void processEvent(SimEvent ev) {

		switch (ev.getTag()){
		
		 case CloudSimTags.RESOURCE_CHARACTERISTICS:
				processResourceCharacteristics(ev);
             break;
             //broker contacts Datacenter Manager
		case CloudSimTags.CONTACT_DCMANAGER:
			setBrokerId((Integer)ev.getData());
			break;
		case CloudSimTags.DCMANAGER_START:
				Log.printLine("Initiate DCManager...");
				sendNow(getId(),CloudSimTags.VM_DESTROY);
				sendNow(getId(),CloudSimTags.VM_CREATE);
				sendNow(getDcId(), CloudSimTags.RESOURCE_CHARACTERISTICS, getId());
				sendNow(getId(),CloudSimTags.PREDICTION_ALGORITHM);
				sendNow(getId(),CloudSimTags.DYNAMIC_SCHEDULING);

			break;
			//databroker submits vmlist
		case CloudSimTags.SUBMIT_LIST:
			processSubmitList(ev);
			break;
			//process vm requests
		case CloudSimTags.VM_CREATE:
				//ProcessVMRequests(ev);
				processCreateVmsInDatacenter();
			break;
		case CloudSimTags.VM_CREATE_ACK:
				processVmCreate(ev);
			break;
		case CloudSimTags.VM_DESTROY:
				processVmDestroy(ev);
			break;
		case CloudSimTags.VM_DESTROY_ACK:
			int[] data = (int[]) ev.getData();
			@SuppressWarnings("unused")
			int datacenterId = data[0];
			int vmId = data[1];
			int result = data[2];
			if (result == CloudSimTags.TRUE) {
			Log.printLine(CloudSim.getClockTick() + ": " + getName() + ": VM #" + vmId +" Destroyed");
			}
			break;
		case CloudSimTags.POWER_SAVED:
			CalculatePowerSaved();
			break;
		case CloudSimTags.PREDICTION_ALGORITHM:
			processPredictionAlgorithm();
			break;
		case CloudSimTags.DYNAMIC_SCHEDULING:
			processDynamicScheduling();
			break;			
			default:
				processEvent(ev);
			break;
		}
		
	}


@SuppressWarnings("unchecked")
protected void processDynamicScheduling() {

	int busyPesInChip = 0;
	int calcTotal = 0;
	int calcPercentage = 0;
	for(PAMHost host:datacenter.getHostList()){
		busyPesInChip  = 0;
		for(Chip chip:host.getChipList()){
			if(chip.getStatus()!=FREE){
				busyPesInChip += PAMPeList.getBusyPesNumber((List<PAMPe>) chip.getPeList());
			}
		}
		
		if(dynamicCount>0){
			calcTotal =  busyPesList.get(host.getId())-busyPesInChip;
			if(calcTotal<0){
				busyPesList.put(host.getId(),busyPesInChip);
			}
			if(busyPesList.get(host.getId())<=0){
				calcPercentage = 0;
			}else{
			if(calcTotal>0){
				calcPercentage = (calcTotal*100)/busyPesList.get(host.getId());
			}else{
				calcPercentage = 0;
			}
		}
		}
		if(calcPercentage>=30){
			host.computeDynamicScheduling();
			busyPesList.put(host.getId(),busyPesInChip);
		}
		if(dynamicCount==0){
			busyPesList.put(host.getId(),busyPesInChip);
		}
	}
	dynamicCount++;
	
	if(run==true){
		sendNow(getId(),CloudSimTags.DYNAMIC_SCHEDULING);
	}
	processEndofSimulation();
		
	}







protected void processPredictionAlgorithm() {

	int predicted=0;
	int futureFreePes=0;
	int percentToNap=0;
	int toBeprovisioned=0;
	int requiredPes=0;
	int remainingPes=0;
	int morePes=0;
	int pesToNap=0;
	int pesToWinkle=0;
	int pesInNap=0;
	int pesInWinkle=0;
	int chipInWinkle=0;
	int chipInNap=0;
	int totalPesInNap=0;
	int totalPesInWinkle=0;
	int pesInWinkleTemp=0;
	int pesInNapTemp=0;
	int hostInWinkle =0;
	int hostInNap=0;
	int extraReservation=10; 	//extra reservation for provisioned
	int timeDelay=60;	//to change the batch processing duration
	
	if(CloudSim.getClockTick()%timeDelay==0){
		
		if(listsSubmitted>=3){
			for(int i=0;i<predictionList.length;i++){
				predicted+=predictionList[i];
			}
			predicted=predicted/predictionList.length;
			//extra reservation for provisioned
			percentToNap=(freePes*extraReservation)/100;
			toBeprovisioned=predicted+percentToNap;
			Log.printLine(CloudSim.getClockTick()+": "+getName()+ ": No of Pes predicted: "+predicted);
			Log.printLine(CloudSim.getClockTick()+": "+getName()+ ": No of Pes provisioned: "+toBeprovisioned);
			Collections.sort(durationToPes);
			for(String dtp:durationToPes){
				StringTokenizer st=new StringTokenizer(dtp,"-");
				while(st.hasMoreTokens()){
					Integer endTime=Integer.valueOf(st.nextToken());
					Integer pes=Integer.valueOf(st.nextToken());
					if((endTime>CloudSim.getClockTick())&&(endTime<=(CloudSim.getClockTick()+4))){//(10-1) the timeslot of batch dispatching by broker
						//System.out.println(dtp+"future free pes: "+futureFreePes);
						futureFreePes+=pes;
					/*	toRemove.add(dtp);
					}else{
						toRemove.add(dtp);*/
					}
				}
			}
			//durationToPes.removeAll(toRemove);
			Log.printLine(CloudSim.getClockTick()+": "+getName()+ ": No of Pes going to free in future: "+futureFreePes);
			if(futureFreePes>toBeprovisioned){ //if future pes is greater then toBeprovisioned, take 10% of mores to nap and remaining to winkle
				morePes=futureFreePes-toBeprovisioned;
				pesToNap=(morePes*extraReservation)/100; //Provisioning Percentage 
				pesToWinkle=morePes-pesToNap;
				if(pesToWinkle>(PAMHostList.getPesNumber(datacenter.getHostList())%5))
					transitionToWinkle++;
				for(PAMHost host:datacenter.getHostList()){	
					for(Chip chip:host.getChipList()){
						for(PAMPe pe:chip.getPeList()){
							if(pesToWinkle!=0){
								if(pe.getState()==IBMPowerState.NAP){
									pe.setState(IBMPowerState.WINKLE);
									pesToWinkle--;
								}
							}	
						}
					if(chip.getPesInWinkle()==chip.getPesNumber()){
						chip.setChipState(IBMPowerState.WINKLE);
					}
					}
					if(host.getPesInWinkle()==host.getPesNumber()){
						host.setHostState(IBMPowerState.WINKLE);
					}
				}
			}else							//if future pes is less then predicted
			{
				requiredPes=toBeprovisioned-futureFreePes;
				 pesInNap=PAMHostList.getPesInNap(datacenter.getHostList());
				if(pesInNap>=requiredPes){
					remainingPes=pesInNap-requiredPes; //move the remaining pes to winkle
					if(remainingPes>(PAMHostList.getPesNumber(datacenter.getHostList())%5))
						transitionToWinkle++;
					for(PAMHost host:datacenter.getHostList()){	
						for(Chip chip:host.getChipList()){
							for(PAMPe pe:chip.getPeList()){
								if(remainingPes!=0){
									if(pe.getState()==IBMPowerState.NAP){
										pe.setState(IBMPowerState.WINKLE);
										remainingPes--;
									}
								}
							}
							if(chip.getPesInWinkle()==chip.getPesNumber()){
								chip.setChipState(IBMPowerState.WINKLE);
							}
						}
						if(host.getPesInWinkle()==host.getPesNumber()){
							host.setHostState(IBMPowerState.WINKLE);
						}
					}
						
				}else{
					remainingPes=requiredPes-pesInNap; //move the remaining pes from winkle to nap
					if(remainingPes>(PAMHostList.getPesNumber(datacenter.getHostList())%5))
						transitionToNap++;
					for(PAMHost host:datacenter.getHostList()){	
						for(Chip chip:host.getChipList()){
							for(PAMPe pe:chip.getPeList()){
								if(remainingPes!=0){
									if(pe.getState()==IBMPowerState.WINKLE){
										pe.setState(IBMPowerState.NAP);
										remainingPes--;
									}
								}
							}
							if(chip.getPesInNap()==chip.getPesNumber()){
								chip.setChipState(IBMPowerState.NAP);
							}
						}
						if(host.getPesInNap()==host.getPesNumber()){
							host.setHostState(IBMPowerState.NAP);
						}
					}
					
				}
			}
			
			//System.out.println("Free pes: "+freePes);
			//System.out.println("predicted pes: "+predicted);
			//System.out.println("provisioned: "+provisioned);
			// we need hw much pes in nap and winkle currently and how much going to get free in the 
			//next batch of time
			
		
		}
		
		for(PAMHost host:datacenter.getHostList()){
			if(host.getPesInWinkle()==host.getPesNumber()){
				pesInWinkleTemp+=host.getPesNumber();
				hostInWinkle++;
				continue;
			}
			if(host.getPesInNap()==host.getPesNumber()){
				pesInNapTemp+=host.getPesNumber();
				hostInNap++;
				continue;
			}
			for(Chip chip:host.getChipList()){
				if(chip.getPesInWinkle()==chip.getPesNumber()){
					chipInWinkle++;
					pesInWinkleTemp+=chip.getPesNumber();
				}
				if(chip.getPesInNap()==chip.getPesNumber()){
					chipInNap++;
					pesInNapTemp+=chip.getPesNumber();
				}
				
			}
		}
		 totalPesInNap=PAMHostList.getPesInNap(datacenter.getHostList());
		 totalPesInWinkle=PAMHostList.getPesInWinkle(datacenter.getHostList());
		 pesInNap=totalPesInNap-pesInNapTemp;
		 pesInWinkle=totalPesInWinkle-pesInWinkleTemp;
		 
		Log.printLine(CloudSim.getClockTick()+": "+getName()+ ": No of  Host "+hostInNap+", Chip " +chipInNap+", and Pes " +pesInNap+ " in NAP State: ");
		Log.printLine(CloudSim.getClockTick()+": "+getName()+ ": Total Pes in Nap State: "+hostInNap+"*40+"+chipInNap+"*8+"+pesInNap+" = "+(hostInNap*40+chipInNap*8+pesInNap));
		Log.printLine(CloudSim.getClockTick()+": "+getName()+ ": No of  Host "+hostInWinkle+", Chip " +chipInWinkle+", and Pes " +pesInWinkle+ " in Winkle State: ");
		Log.printLine(CloudSim.getClockTick()+": "+getName()+ ": Total Pes in Winkle State: "+hostInWinkle+"*40+"+chipInWinkle+"*8+"+pesInWinkle+" = "+(hostInWinkle*40+chipInWinkle*8+pesInWinkle));
		
		//Log.printLine(CloudSim.getClockTick()+": "+getName()+ ": No of Chips and Pes in WINKLE State: "+chipinWinkle+" and "+pesinWinkle);
		String hostChipNapId;
		hostChipNapId=getHostChipNapId(hostInNap, chipInNap, pesInNap);
		inNapState.add(hostChipNapId);
		hostChipNapId=getHostChipNapId(hostInWinkle, chipInWinkle, pesInWinkle);
		inWinkleState.add(hostChipNapId);


	}
	
	if(run==true){
		sendNow(getId(), CloudSimTags.PREDICTION_ALGORITHM);
	}
	processEndofSimulation();
	
}	
		
public static String getHostChipNapId(int hostInNap, int chipInNap,int pesInNap) {
	return hostInNap + "-" + chipInNap + "-" + pesInNap;
}






protected void CalculatePowerSaved() {
		
		
		double powerConservedInNap=0.0;
		double powerConservedInWinkle=0.0;
		int pesinHost=datacenter.getHostList().get(0).getPesNumber();
		int pesinChip=datacenter.getHostList().get(0).getChipList().get(0).getPesNumber();
		for(int i=0;i<inNapState.size();i++){
			String str=inNapState.get(i);
			StringTokenizer st=new StringTokenizer(str,"-");
			while(st.hasMoreTokens()){
				Integer hostsInNap=Integer.valueOf(st.nextToken());
				Integer ChipsInNap=Integer.valueOf(st.nextToken());
				Integer PesInNap=Integer.valueOf(st.nextToken());
				powerConservedInNap+=IBMPowerState.calculatePowerConservedByHost(5, IBMPowerState.NAP, 20, hostsInNap,pesinHost);
				powerConservedInNap+=IBMPowerState.calculatePowerConservedByChip(5, IBMPowerState.NAP, 20, ChipsInNap,pesinChip);
				powerConservedInNap+=IBMPowerState.calculatePowerConservedByPe(5, IBMPowerState.NAP, 20, PesInNap);
				
			}
		}
			
		for(int i=0;i<inWinkleState.size();i++){
			String str=inWinkleState.get(i);
			StringTokenizer st=new StringTokenizer(str,"-");
			while(st.hasMoreTokens()){
				Integer hostsInWinkle=Integer.valueOf(st.nextToken());
				Integer ChipsInWinkle=Integer.valueOf(st.nextToken());
				Integer PesInWinkle=Integer.valueOf(st.nextToken());
				powerConservedInWinkle+=IBMPowerState.calculatePowerConservedByHost(5, IBMPowerState.WINKLE, 20, hostsInWinkle,pesinHost);
				powerConservedInWinkle+=IBMPowerState.calculatePowerConservedByChip(5, IBMPowerState.WINKLE, 20, ChipsInWinkle,pesinChip);
				powerConservedInWinkle+=IBMPowerState.calculatePowerConservedByPe(5, IBMPowerState.WINKLE, 20, PesInWinkle);
			}
		}
		
		Log.printLine("**************Power Conserved using Power States***************");
		Log.printLine("Power Conservered in Nap state: "+powerConservedInNap+" Watts");
		Log.printLine("Power Conservered in Winkle state: "+powerConservedInWinkle+" Watts");
		Log.printLine("Total Power Conserved: "+(powerConservedInNap+powerConservedInWinkle)+" Watts");
		Log.printLine("Total Number of Vm Requests not fullfilled immediately: "+datacenter.getPAMVmAllocationPolicy().getVmRequestNotFullfilled());
		Log.printLine("Total Number of Vms Failed: "+datacenter.getPAMVmAllocationPolicy().getVmFailed());
		Log.printLine("Total Transition from Winkle to Nap is : "+transitionToNap);
		Log.printLine("Total Transition from Nap to Winkle is : "+transitionToWinkle);
		Log.printLine("Total Number of Vms Created across Chip: "+datacenter.vmsCreatedAcrossChipInDC());
		Log.printLine("Total Number of Vms Created within Chip: "+datacenter.vmsCreatedWithinChipInDC());
		Log.printLine("******************************************************************");
}







/** for each time unit check there is any vm to destroy */
	protected void processVmDestroy(SimEvent ev) {
		waitClock++;
		int duration;
		 List<PAMVm> toRemove=new ArrayList<PAMVm>();
		if(!getVmsCreatedList().isEmpty()){
			waitClock=0;
		
			//for(int i=0;i<getVmsCreatedList().size();i++){
			for(PAMVm vm:getVmsCreatedList()){	
			//PAMVm vm= vmsCreatedList.get(i);
				duration=vm.getDuration();
				duration--;
				if(duration<=0){
					dflag=true;
					Log.printLine(CloudSim.getClockTick() + ": " + getName() + ": Destroying VM #" + vm.getId());
					sendNow(getDcId(), CloudSimTags.VM_DESTROY_ACK, vm);
					toRemove.add(vm);
					//vmsCreatedList.remove(i);
				}else{
					vm.setDuration(duration);
				}				
			}
			getVmsCreatedList().removeAll(toRemove);
		}
		if(run==true){
		
			sendNow(getId(),CloudSimTags.VM_DESTROY);
		}
		processEndofSimulation();
		
	}


protected void processEndofSimulation() {
	
	if((waitClock>1000)&&(enterOnce==false)){
		enterOnce=true;
		run=false;
		sendNow(getId(), CloudSimTags.POWER_SAVED);
	}
	
}







/** for each time unit submit the vm request to datacenter */
	protected void processCreateVmsInDatacenter() {
		
		createVmsInDatacenter(getDcId());
	}


	/** sends vm creation request to datacenter */
	protected void createVmsInDatacenter(int datacenterId) {
		// send as much vms as possible for this datacenter before trying the next one
		int requestedVms = 0;
		int totalPes=0;
		int position=0;
		String datacenterName = CloudSim.getEntityName(datacenterId);
		waitClock++;
		for (PAMVm vm : getVmList()) {
			totalPes+=vm.getPesNumber();
			Log.printLine(CloudSim.getClockTick() + ": " + getName() + ": PEs Required by VM #" + vm.getId()+" is: "+vm.getPesNumber()+" and duration : "+vm.getDuration() );
			Log.printLine(CloudSim.getClockTick() + ": " + getName() + ": Trying to Create VM #" + vm.getId() + " in " + datacenterName);
			sendNow(datacenterId,CloudSimTags.VM_CREATE_ACK, vm);
			getVmSubmitted().add(vm);
			requestedVms++;
		}
		if(!vmList.isEmpty()){

			waitClock=0;
			Log.printLine(CloudSim.getClockTick() + ": " + getName() + ": Total PEs required by the VM Batch : " + totalPes);
			batchSize=vmList.size();
			position=(listsSubmitted)%3;
			listsSubmitted++;
			predictionList[position]=totalPes;
			cflag=true;
			vmList.clear();
		}
		setVmsRequested(requestedVms);
		setVmsAcks(0);
		if(run==true){
			sendNow(getId(),CloudSimTags.VM_CREATE);
		}
		processEndofSimulation();
	}
		

	/** for each time unit process the resource characteristics of the datacenter */
	protected void processResourceCharacteristics(SimEvent ev) {
		setCharacteristics((PAMDatacenterCharacteristics) ev.getData());
		waitClock++;
		if((cflag==true)||(dflag==true)){
			waitClock=0;
			cflag=false;
			dflag=false;
			freePes=characteristics.getFreePesNumber();
			busyPes=characteristics.getBusyPesNumber();
			Log.printLine(CloudSim.getClockTick()+":----------------------------------------------------------------");
			//Log.printLine(CloudSim.getClockTick()+" : Total Number of Pes Required by VMs: "+ totalPes);
			Log.printLine(CloudSim.getClockTick()+": Total Number of Free Pes in the machine: "+ freePes);
			Log.printLine(CloudSim.getClockTick()+": Total Number of Busy Pes in the machine: "+ busyPes);
			Log.printLine(CloudSim.getClockTick()+":----------------------------------------------------------------");
			//Log.printLine("end of a time slot");
		}
		if(run==true){
			sendNow(getDcId(), CloudSimTags.RESOURCE_CHARACTERISTICS, getId());
		}
		processEndofSimulation();

}
	
	/** gets sumbitted the vm lists from the broker */
	protected void processSubmitList(SimEvent ev) {
			
		Object[] obj=(Object[])ev.getData();
		if(obj!=null){
			for(int i=0;i<obj.length;i++){
				PAMVm vm=(PAMVm)obj[i];
				getVmList().add(vm);
			}
		}
		
		if(sortDesc==true){
			for(int i=0;i<getVmList().size();i++){
				for(int j=i;j<getVmList().size();j++){
					PAMVm vm;
					if(getVmList().get(i).getPesNumber()<getVmList().get(j).getPesNumber()){
						vm=getVmList().get(i);
						getVmList().set(i, getVmList().get(j));
						getVmList().set(j,vm);
					}
				}
			}
		}
		if(sortAsec==true){
			for(int i=0;i<getVmList().size();i++){
				for(int j=i;j<getVmList().size();j++){
					PAMVm vm;
					if(getVmList().get(i).getPesNumber()>getVmList().get(j).getPesNumber()){
						vm=getVmList().get(i);
						getVmList().set(i, getVmList().get(j));
						getVmList().set(j,vm);
					}
				}
			}
		}
		
	}


	/** acks from datacenter whether the vm is created successfully or not */
	protected void processVmCreate(SimEvent ev) {
		int[] data = (int[]) ev.getData();
		int datacenterId = data[0];
		int vmId = data[1];
		int result = data[2];
		String str;
		if (result == CloudSimTags.TRUE) {
			getVmsToDatacentersMap().put(vmId, datacenterId);
			int duration=PAMVmList.getById(getVmSubmitted(), vmId).getDuration();
			PAMVmList.getById(getVmSubmitted(), vmId).setDuration(--duration);
			int endTime=(int) (CloudSim.getClockTick()+duration);
			str=getDurationToPeMap(endTime, PAMVmList.getById(getVmSubmitted(), vmId).getPesNumber());
			durationToPes.add(str);
				getVmsCreatedList().add(PAMVmList.getById(getVmSubmitted(), vmId));
			Log.printLine(CloudSim.getClockTick()+": "+getName()+ ": VM #"+vmId+" has been created in Datacenter #" + datacenterId + ", Host #" + PAMVmList.getById(getVmsCreatedList(), vmId).getPAMHost().getId());
		} else {
			Log.printLine(CloudSim.getClockTick()+": "+getName()+ ": Creation of VM #"+vmId+" failed in Datacenter #" + datacenterId);
		}
		
		incrementVmsAcks();
		/** prediction algorithm
		 * @author srinivasan
		 */
		
	
		
	}

	/** process other default events */
	 protected void processOtherEvent(SimEvent ev){
	        if (ev == null){
	            Log.printLine(getName() + ".processOtherEvent(): " + "Error - an event is null.");
	            return;
	        }

	        Log.printLine(getName() + ".processOtherEvent(): " + "Error - event unknown by this DatacenterBroker.");
	    }

	@Override
	public void shutdownEntity() {
		
	}

	@Override
	public void startEntity() {
		
		Log.printLine("DCManager started....");
		 // this resource should register to regional GIS.
        // However, if not specified, then register to system GIS (the
        // default CloudInformationService) entity.
        int gisID = CloudSim.getEntityId(regionalCisName);
        if (gisID == -1) {
            gisID = CloudSim.getCloudInfoServiceEntityId();
        }

        // send the registration to GIS
        sendNow(gisID, CloudSimTags.REGISTER_RESOURCE, getId());
        registerOtherEntity();
        // Below method is for a child class to override
		schedule(getId(), 0, CloudSimTags.DCMANAGER_START);	
		
		sendNow(getDcId(), CloudSimTags.RESOURCE_CHARACTERISTICS, getId());
	}
	
	  protected void registerOtherEntity() {
	        // empty. This should be override by a child class
	    }
	

	  public void sortVm(int Order){
		  
		  if(Order==asec){
			  sortAsec=true;
		  }
		  if(Order==desc){
			  sortDesc=true;
		  }
	  }
	  
	/**
	 * Gets the vms to datacenters map.
	 *
	 * @return the vms to datacenters map
	 */
	protected Map<Integer, Integer> getVmsToDatacentersMap() {
		return vmsToDatacentersMap;
	}
	
	protected void setVmsToDatacentersMap(Map<Integer, Integer> vmsToDatacentersMap) {
		this.vmsToDatacentersMap = vmsToDatacentersMap;
	}
	public static void setDcId(int dcId) {
		DCManager.dcId = dcId;
	}

	public static int getDcId() {
		return dcId;
	}
	
	public static String getDcName() {
		return dcName;
	}

	public static void setDcName(String dcName) {
		DCManager.dcName = dcName;
	}

	public PAMDatacenter getDatacenter() {
		return datacenter;
	}


	public void setDatacenter(PAMDatacenter datacenter) {
		this.datacenter = datacenter;
	}

	
	public String getRegionalCisName() {
		return regionalCisName;
	}

	public void setRegionalCisName(String regionalCisName) {
		this.regionalCisName = regionalCisName;
	}
	
	public PAMDatacenterCharacteristics getCharacteristics() {
		return characteristics;
	}


	public void setCharacteristics(PAMDatacenterCharacteristics characteristics) {
		this.characteristics = characteristics;
	}


	public int getBrokerId() {
		return brokerId;
	}


	public void setBrokerId(int brokerId) {
		this.brokerId = brokerId;
	}

	public List<PAMVm> getVmList() {
		return vmList;
	}


	public void setVmList(List<PAMVm> vmList) {
		this.vmList = vmList;
	}



	public void setBatchSize(int batchSize) {
		DCManager.batchSize = batchSize;
	}



	public int getBatchSize() {
		return batchSize;
	}



	public void setVmsRequested(int vmsRequested) {
		this.vmsRequested = vmsRequested;
	}



	public int getVmsRequested() {
		return vmsRequested;
	}



	public void setVmsAcks(int vmsAcks) {
		this.vmsAcks = vmsAcks;
	}



	public int getVmsAcks() {
		return vmsAcks;
	}



	public void setVmsCreatedList(List<PAMVm> vmsCreatedList) {
		this.vmsCreatedList = vmsCreatedList;
	}


	public List<PAMVm> getVmsCreatedList() {
		return vmsCreatedList;
	}
	
	protected void incrementVmsAcks() {
		this.vmsAcks++;
	}
	
	protected int getVmsDestroyed() {
		return vmsDestroyed;
	}
	
	protected void setVmsDestroyed(int vmsDestroyed) {
		this.vmsDestroyed = vmsDestroyed;
	}



	public void setVmSubmitted(List<PAMVm> vmSubmitted) {
		this.vmSubmitted = vmSubmitted;
	}



	public List<PAMVm> getVmSubmitted() {
		return vmSubmitted;
	}



	public static void setCflag(boolean cflag) {
		DCManager.cflag = cflag;
	}



	public static boolean isCflag() {
		return cflag;
	}



	public static void setDflag(boolean dflag) {
		DCManager.dflag = dflag;
	}



	public static boolean isDflag() {
		return dflag;
	}



	public static void setListsSubmitted(int listsSubmitted) {
		DCManager.listsSubmitted = listsSubmitted;
	}



	public static int getListsSubmitted() {
		return listsSubmitted;
	}



	public Integer[] getPredictionList() {
		return predictionList;
	}


	public void setPredictionList(Integer[] predictionList) {
		this.predictionList = predictionList;
	}

	public static int getFreePes() {
		return freePes;
	}

	public static void setFreePes(int freePes) {
		DCManager.freePes = freePes;
	}

	public static int getBusyPes() {
		return busyPes;
	}

	public static void setBusyPes(int busyPes) {
		DCManager.busyPes = busyPes;
	}

	public void setDurationToPes(List<String> durationToPes) {
		this.durationToPes = durationToPes;
	}



	public List<String> getDurationToPes() {
		return durationToPes;
	}
	
	public static String getDurationToPeMap(int duration, int pes) {
		return duration + "-" + pes;
	}





	public static void setWaitClock(int waitClock) {
		DCManager.waitClock = waitClock;
	}




	public static int getWaitClock() {
		return waitClock;
	}




	public static void setRun(boolean run) {
		DCManager.run = run;
	}



	public static boolean isRun() {
		return run;
	}




	public static boolean setEnterOnce(boolean enterOnce) {
		DCManager.enterOnce = enterOnce;
		return enterOnce;
	}



	public static boolean isEnterOnce() {
		return enterOnce;
	}







	public static void setTransitionToWinkle(int transitionToWinkle) {
		DCManager.transitionToWinkle = transitionToWinkle;
	}







	public static int getTransitionToWinkle() {
		return transitionToWinkle;
	}







	public static void setTransitionToNap(int transitionToNap) {
		DCManager.transitionToNap = transitionToNap;
	}







	public static int getTransitionToNap() {
		return transitionToNap;
	}



	public void setInNapState(List<String> inNapState) {
		this.inNapState = inNapState;
	}





	public List<String> getInNapState() {
		return inNapState;
	}







	public void setInWinkleState(List<String> inWinkleState) {
		this.inWinkleState = inWinkleState;
	}







	public List<String> getInWinkleState() {
		return inWinkleState;
	}







	public void setBusyPesList(Map<Integer, Integer> busyPesList) {
		this.busyPesList = busyPesList;
	}







	public Map<Integer, Integer> getBusyPesList() {
		return busyPesList;
	}







	public static void setDynamicCount(int dynamicCount) {
		DCManager.dynamicCount = dynamicCount;
	}







	public static int getDynamicCount() {
		return dynamicCount;
	}


}