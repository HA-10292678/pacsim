package org.cloudbus.cloudsim.pam;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;


import org.cloudbus.cloudsim.CloudletSchedulerSpaceShared;
import org.cloudbus.cloudsim.CloudletSchedulerTimeShared;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.core.SimEntity;
import org.cloudbus.cloudsim.core.SimEvent;

/**
*
* @author		Srinivasan.S
* @version		CS 1.0
*/

public class PAMDatacenterBroker extends SimEntity  {

	private List<Vm> vmlist;
	
	private static int batchSize=0;
	
	private static int noOfBatchs=0;
	
	private static int vmid=0;
	
	public ResultSet rs;
	
	public Connection con;
	
	public Statement stmt;
	
	private boolean generatingList = false; 
	
	/** The datacenter manager ids list. */
	private List<Integer> dcMgrIdsList;
	
	private static int dcMgrId;
	
	private int maxVmRequest;
	
	private int maxTimeDurationOfVm;

	private int maxNoOfPeForVm;
	
	private int batchingTimeDelay;
	
	private int noOfBatchSubmission;
	
	private int maxRam;

	private static int rangePercent1;
	
	private static int rangePercent2;
	
	private static int rangePercent3;
	
	private static int pePercent1;
	
	private static int pePercent2;
	
	private static int pePercent3;
	
	
	public PAMDatacenterBroker(String name) {
		super(name);
		
		setMaxVmRequest(5); 
		setMaxTimeDurationOfVm(10);
		setMaxNoOfPeForVm(8);
		setBatchingTimeDelay(5);
		setNoOfBatchSubmission(10);
		setMaxRam(4);
		
		setVmlist(new ArrayList<Vm>()) ;
		setDcMgrIdsList(new LinkedList<Integer>());
	}
	
	public PAMDatacenterBroker(String name,int maxVmRequest,int maxTimeDurationOfVm,int maxNoOfPeForVm,int batchingTimeDelay,int noOfBatchSubmission,int maxRam) {
		super(name);
		
		setMaxVmRequest(maxVmRequest);
		setMaxTimeDurationOfVm(maxTimeDurationOfVm);
		setMaxNoOfPeForVm(maxNoOfPeForVm);
		setBatchingTimeDelay(batchingTimeDelay);
		setNoOfBatchSubmission(noOfBatchSubmission);
		setMaxRam(maxRam);
		
		setVmlist(new ArrayList<Vm>()) ;
		setDcMgrIdsList(new LinkedList<Integer>());
		
	}



	/**
	 * @param args
	 */

	@Override
	public void processEvent(SimEvent ev) {
		switch (ev.getTag()){
		case CloudSimTags.BROKER_START:
			processDCManager(ev);
			schedule(getId(), 0, CloudSimTags.VM_CREATE);
		break;
		case CloudSimTags.VM_CREATE:
				if((CloudSim.getClockTick()%getBatchingTimeDelay())==1)
				{
				generateLoad();
				}else if(noOfBatchs==getNoOfBatchSubmission()){
					
				//sendNow(getId(),CloudSimTags.END_OF_SIMULATION);
				}else{
					sendNow(getId(),CloudSimTags.VM_CREATE);
				}
		break;
		case CloudSimTags.END_OF_SIMULATION:
			System.out.println(getName() + " is shutting down...");
		
		}
	}
	
	protected void processDCManager(SimEvent ev) {
		setDcMgrIdsList(CloudSim.getCloudResourceList());
	//	setDatacenterCharacteristicsList(new HashMap<Integer, DatacenterCharacteristics>());

		Log.printLine(CloudSim.getClockTick()+": "+getName()+ ": Cloud Resource List received with "+getDcMgrIdsList().size()+" resource(s)");
	
		dcMgrId=getDcMgrIdsList().get(0);
		Log.printLine(CloudSim.getClockTick()+": "+getName()+ ": Contacting the Datacenter Manager Id: "+dcMgrId);
		sendNow(dcMgrId,CloudSimTags.CONTACT_DCMANAGER,getId());
	}
	
	@Override
	public void shutdownEntity() {
		
		   Log.printLine(getName() + " is shutting down...");
		/*try {
			con.close();
		} catch (SQLException e) {
	
			e.printStackTrace();
		}*/
	}

	@Override
	public void startEntity() {
		Log.printLine(getName() + " is starting...");
		//initilizeDB(); //initilize DB to load vm list from database 
		schedule(getId(), 0, CloudSimTags.BROKER_START);
		
	}
	public void initilizeDB(){
		try {
			Class.forName("sun.jdbc.odbc.JdbcOdbcDriver");
			con=DriverManager.getConnection("jdbc:odbc:ClientRequestDB");
			String str="select * from VMRequests";
			stmt=con.createStatement();
			rs=stmt.executeQuery(str);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	
}
	
	protected void generateLoad(){
		
		if(generateRandomVmList()){
		batchSize=0;
		generatingList=false;
		Object[] obj=vmlist.toArray();
		Log.printLine(CloudSim.getClockTick()+": ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ ");
		Log.printLine(CloudSim.getClockTick()+": "+getName() + " :submitting VmList "+ noOfBatchs+" of size: "+obj.length);
		Log.printLine(CloudSim.getClockTick()+": ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ ");
		sendNow(dcMgrId, CloudSimTags.SUBMIT_LIST, obj);
		vmlist.clear();
		sendNow(getId(),CloudSimTags.VM_CREATE);
		}
				
	}
	//Generates Vm's with random Pe's and time duration
	public boolean generateRandomVmList(){
		
		//int vmid = 0;
		int mips = 1000;
		long size = 10000; // image size (MB)
		int ram = 512; // vm memory (MB)
		long bw = 1000;
		int pesNumber = 0; // number of cpus
		int duration=10;//duration of a vm
		String vmm = "Xen"; // VMM name
		if(noOfBatchs!=getNoOfBatchSubmission()){
		int vmSize=(int) ((Math.random()*getMaxVmRequest())+1);	//vm size the no of vms submitted for a batch
			for(int i=0;i<vmSize;i++){
				int randRange=(int)((Math.random())*100)+1;
				
		
				if((randRange>0)&&(randRange<=getRangePercent1())){
					pesNumber=(int)((Math.random())*((getMaxNoOfPeForVm()*getPePercent1())/100))+1;
				}else if((randRange>getRangePercent1())&&(randRange<=getRangePercent2())){
					pesNumber=(((getMaxNoOfPeForVm()*getPePercent1())/100))+(int)((Math.random())*((getMaxNoOfPeForVm()*getPePercent2())/100))+1;
				}else{
					pesNumber=(((getMaxNoOfPeForVm()*(getPePercent1()+getPePercent2()))/100))+(int)((Math.random())*((getMaxNoOfPeForVm()*getPePercent3())/100))+1;
				}
				//generates vms in the range of 1-40 pes
				/*if((randRange>=0)&&(randRange<10)){
					pesNumber=(int)((Math.random())*20)+1;
				}else if((randRange>=10)&&(randRange<20)){
					pesNumber=21+(int)((Math.random())*10);
				}else{
					pesNumber=31+(int)((Math.random())*10);
				}*/
				duration=(int)((Math.random())*getMaxTimeDurationOfVm())+1;
				ram =((int)((Math.random())*getMaxRam())+1)*512;
				PAMVm vm = new PAMVm(vmid,getId(), mips, pesNumber, ram, bw, size,vmm, new CloudletSchedulerSpaceShared(),duration);
				vmlist.add(vm);
				vmid++;
			}
			generatingList=true;
			noOfBatchs++;
		
		}
		
		return generatingList;
		
	
	}
	//generates vm list, loads from database
	public boolean generateVmList(){
		try {
				double rand=Math.random();
				int randBatchSize=(int)(rand *10);
				//generatingList=false;
			while((batchSize!=randBatchSize)&&(rs.next())){
 
				// VM description
				int vmid = rs.getInt("VMID"); //vmid
				int mips = rs.getInt("MIPS");
				long size = rs.getInt("SIZE"); // image size (MB) 
				int ram = rs.getInt("RAM");// vm memory (MB)
				long bw = rs.getInt("BW");
				int pesNumber = rs.getInt("PESNUMBER"); // number of cpus
				int duration=rs.getInt("DURATION");
				String vmm = rs.getString("VMM"); // VMM name
				//System.out.println(vmid);
				// create VM
				PAMVm vm = new PAMVm(vmid,getId(), mips, pesNumber, ram, bw, size,vmm, new CloudletSchedulerTimeShared(),duration);

				// add the VM to the vmList
				generatingList=true;
				vmlist.add(vm);
				batchSize++;
			}
		
		}  catch (SQLException e) {
			e.printStackTrace();
		}
		return generatingList;
	}
	

	public List<Integer> getDcMgrIdsList() {
		return dcMgrIdsList;
	}


	public void setDcMgrIdsList(List<Integer> dcMgrIdsList) {
		this.dcMgrIdsList = dcMgrIdsList;
	}



	public List<Vm> getVmlist() {
		return vmlist;
	}

	public void setVmlist(List<Vm> vmlist) {
		this.vmlist = vmlist;
	}

	public static int getDcMgrId() {
		return dcMgrId;
	}

	public static void setDcMgrId(int dcMgrId) {
		PAMDatacenterBroker.dcMgrId = dcMgrId;
	}

	public void setMaxVmRequest(int maxVmRequest) {
		this.maxVmRequest = maxVmRequest;
	}

	public int getMaxVmRequest() {
		return maxVmRequest;
	}

	public void setMaxTimeDurationOfVm(int maxTimeDurationOfVm) {
		this.maxTimeDurationOfVm = maxTimeDurationOfVm;
	}

	public int getMaxTimeDurationOfVm() {
		return maxTimeDurationOfVm;
	}

	public void setBatchingTimeDelay(int batchingTimeDelay) {
		this.batchingTimeDelay = batchingTimeDelay;
	}

	public int getBatchingTimeDelay() {
		return batchingTimeDelay;
	}

	public void setMaxNoOfPeForVm(int maxNoOfPeForVm) {
		this.maxNoOfPeForVm = maxNoOfPeForVm;
	}

	public int getMaxNoOfPeForVm() {
		return maxNoOfPeForVm;
	}

	public void setNoOfBatchSubmission(int noOfBatchSubmission) {
		this.noOfBatchSubmission = noOfBatchSubmission;
	}

	public int getNoOfBatchSubmission() {
		return noOfBatchSubmission;
	}

	public void setMaxRam(int maxRam) {
		this.maxRam = maxRam;
	}

	public int getMaxRam() {
		return maxRam;
	}
	

	

	public static int getRangePercent1() {
		return rangePercent1;
	}

	public static void setRangePercent1(int rangePercent1) {
		PAMDatacenterBroker.rangePercent1 = rangePercent1;
		
	}

	public static int getRangePercent2() {
		return rangePercent2;
	}

	public static void setRangePercent2(int rangePercent2) {
		PAMDatacenterBroker.rangePercent2 = rangePercent2;
	}

	public static int getRangePercent3() {
		return rangePercent3;
	}

	public static void setRangePercent3(int rangePercent3) {
		PAMDatacenterBroker.rangePercent3 = rangePercent3;
	}

	public static int getPePercent1() {
		return pePercent1;
	}

	public static void setPePercent1(int pePercent1) {
		PAMDatacenterBroker.pePercent1 = pePercent1;
	}

	public static int getPePercent2() {
		return pePercent2;
	}

	public static void setPePercent2(int pePercent2) {
		PAMDatacenterBroker.pePercent2 = pePercent2;
	}

	public static int getPePercent3() {
		return pePercent3;
	}

	public static void setPePercent3(int pePercent3) {
		PAMDatacenterBroker.pePercent3 = pePercent3;
	}

	public static void setRandomNoSubRange(int rangePercent1, int rangePercent2, int rangePercent3) {
		Log.printLine("Random number sub ranges : 0-"+rangePercent1+", "+(rangePercent1+1)+"-"+(rangePercent1+rangePercent2)+", "+(rangePercent1+rangePercent2+1)+"-"+(rangePercent1+rangePercent2+rangePercent3));
		setRangePercent1(rangePercent1);
		setRangePercent2(rangePercent2);
		setRangePercent3(rangePercent3);
		
	}

	public static void setRandomPeRange(int pePercent1, int pePercent2, int pePercent3) {
		Log.printLine("Pe Range : 0-"+pePercent1+", "+(pePercent1+1)+"-"+(pePercent1+pePercent2)+", "+(pePercent1+pePercent2+1)+"-"+(pePercent1+pePercent2+pePercent3));
		setPePercent1(pePercent1);
		setPePercent2(pePercent2);
		setPePercent3(pePercent3);
		
	}

}
