package org.cloudbus.cloudsim.examples.nonchipawarepolicies;


import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;


import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.pam.Chip;
import org.cloudbus.cloudsim.pam.DCManager;
import org.cloudbus.cloudsim.pam.IBMPowerState;
import org.cloudbus.cloudsim.pam.PAMDatacenter;
import org.cloudbus.cloudsim.pam.PAMDatacenterBrokerExtended;
import org.cloudbus.cloudsim.pam.PAMDatacenterCharacteristics;
import org.cloudbus.cloudsim.pam.PAMHost;
import org.cloudbus.cloudsim.pam.PAMPe;
import org.cloudbus.cloudsim.pam.PAMVmAllocationPolicyWorstFit;
import org.cloudbus.cloudsim.pam.PAMVmSchedulerNonChipAware;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;

/**
*
* @author		Srinivasan.S
* @version		CS 1.0
*/

public class CloudSimNonChipAwareWorstFit {
	
public static void main(String args[]){
	Log.printLine("Starting CloudSimNonChipAwareWorstFit...");

	try {
		// First step: Initialize the CloudSim package. It should be called
		// before creating any entities.
		int num_user = 1; // number of cloud users
		Calendar calendar = Calendar.getInstance();
		boolean trace_flag = false; // mean trace events

		// Initialize the CloudSim library
		CloudSim.init(num_user, calendar, trace_flag);

		// Second step: Create Datacenters
		// Datacenters are the resource providers in CloudSim. We need at
		// list one of them to run a CloudSim simulation
		CloudSimNonChipAwareWorstFit csps=new CloudSimNonChipAwareWorstFit();
		PAMDatacenter datacenter0 = csps.createDatacenter("Datacenter_0");
		csps.createDCManager(datacenter0);

		@SuppressWarnings("unused")
		PAMDatacenterBrokerExtended pamDBE=new PAMDatacenterBrokerExtended("PAMDatacenterBrokerExtended","workloadpattern_101");
		
		CloudSim.startSimulation();
		CloudSim.stopSimulation();
		datacenter0.printDebts();

		Log.printLine("CloudSimNonChipAwareWorstFit finished!");
	}
	catch (Exception e) {
		e.printStackTrace();
		Log.printLine("Unwanted errors happen");
	}
}


private void createDCManager(PAMDatacenter datacenter0) {
	
	try {
		new DCManager("DCManager_0",datacenter0);
	} catch (Exception e) {
	
		e.printStackTrace();
	}
	
}


public List<PAMPe> createPe(){
	
	List<PAMPe> peList= new ArrayList<PAMPe>(); ;
	int mips = 2000;
	//int peState=IBMPowerState.NAP;
	// 3. Create PEs and add these into a list.
	for(int i=0;i<8;i++){
		peList.add(new PAMPe(i, new PeProvisionerSimple(mips)));
	}
	
	return peList;
}

public List<Chip> createChip(){
	List<Chip> chipList= new ArrayList<Chip>();
//	int chipState=IBMPowerState.NAP;
	for(int i=0;i<5;i++){
		List<PAMPe> peList=createPe();
		chipList.add(new Chip(i,peList));
		
	}
	return chipList;
}
private  PAMDatacenter createDatacenter(String name) {

	// Here are the steps needed to create a PowerDatacenter:
	// 1. We need to create a list to store
	// our machine
	List<PAMHost> hostList = new ArrayList<PAMHost>();

	// 2. A Machine contains one or more PEs or CPUs/Cores.
	// In this example, it will have only one core.
 // need to store Pe id and MIPS Rating

	// 4. Create Host with its id and list of PEs and add them to the list
	// of machines
	int hostId = 0;
	int ram = 8192; // host memory (MB)
	long storage = 1000000; // host storage
	int bw = 10000;
	int hostState=IBMPowerState.NAP;
	for(hostId=0;hostId<15;hostId++){
		List<Chip> chipList=createChip();
		//List<Pe> peList=createPe();
	hostList.add(
		new PAMHost(
			hostId,
			new RamProvisionerSimple(ram),
			new BwProvisionerSimple(bw),
			storage,
			chipList,
			new PAMVmSchedulerNonChipAware(chipList),
			hostState
		)
	);
	} // This is our machine
	

	// 5. Create a DatacenterCharacteristics object that stores the
	// properties of a data center: architecture, OS, list of
	// Machines, allocation policy: time- or space-shared, time zone
	// and its price (G$/Pe time unit).
	String arch = "x86"; // system architecture
	String os = "Linux"; // operating system
	String vmm = "Xen";
	double time_zone = 10.0; // time zone this resource located
	double cost = 3.0; // the cost of using processing in this resource
	double costPerMem = 0.05; // the cost of using memory in this resource
	double costPerStorage = 0.001; // the cost of using storage in this
									// resource
	double costPerBw = 0.0; // the cost of using bw in this resource
	LinkedList<Storage> storageList = new LinkedList<Storage>(); // we are not adding SAN
												// devices by now

	PAMDatacenterCharacteristics characteristics = new PAMDatacenterCharacteristics(
			arch, os, vmm, hostList, time_zone, cost, costPerMem,
			costPerStorage, costPerBw);

	// 6. Finally, we need to create a PowerDatacenter object.
	PAMDatacenter datacenter = null;
	try {
		datacenter = new PAMDatacenter(name, characteristics, new PAMVmAllocationPolicyWorstFit(hostList), storageList, 0);
	} catch (Exception e) {
		e.printStackTrace();
	}

	return datacenter;
}
}