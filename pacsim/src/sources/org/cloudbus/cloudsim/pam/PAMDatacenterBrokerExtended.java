package org.cloudbus.cloudsim.pam;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;


import org.cloudbus.cloudsim.CloudletSchedulerSpaceShared;
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

public class PAMDatacenterBrokerExtended extends SimEntity  {

	private List<Vm> vmlist;
	
	private static int batchNo=0;
	
	
	//private static int vmid=0;
	
	public ResultSet rs;
	
	public Connection con;
	
	public Statement stmt;
	
	private int totalBatches;
	
	private boolean generatingList = false; 
	
	/** The datacenter manager ids list. */
	private List<Integer> dcMgrIdsList;
	
	private static int dcMgrId;
	
	private String workloadString;
	
	
	public PAMDatacenterBrokerExtended(String name,String workloadName) {
		super(name);
		
		setWorkloadString(workloadName);
		setVmlist(new ArrayList<Vm>()) ;
		setDcMgrIdsList(new LinkedList<Integer>());
		initilizeDB();
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
			int timeDelay = 60;
				if((CloudSim.getClockTick()%timeDelay)==1)
				{
				generateLoad();
				}else if(batchNo==totalBatches){
					try {
						con.close();
					} catch (SQLException e) {
						e.printStackTrace();
					}
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
			String wStr="select * from workload where WORKLOADID like ?";
			PreparedStatement ps=con.prepareStatement(wStr);
			ps.setString(1, getWorkloadString());
			ResultSet rs=ps.executeQuery();
			while(rs.next()){
			totalBatches=rs.getInt("NOOFBATCHES");
			}
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	
}
	
	protected void generateLoad(){
		
		if(generateVmList()){
		generatingList=false;
		Object[] obj=vmlist.toArray();
		Log.printLine(CloudSim.getClockTick()+": ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ ");
		Log.printLine(CloudSim.getClockTick()+": "+getName() + " :submitting VmList "+ batchNo+" of size: "+obj.length);
		Log.printLine(CloudSim.getClockTick()+": ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ ");
		sendNow(dcMgrId, CloudSimTags.SUBMIT_LIST, obj);
		vmlist.clear();
		sendNow(getId(),CloudSimTags.VM_CREATE);
		}
				
	}

	//generates vm list, loads from database
	public boolean generateVmList(){
		try {
				//generatingList=false;
			if(batchNo!=getTotalBatches()){
				String str="select * from VMRequests where WORKLOADNAME=? and BATCHNO=?";
				PreparedStatement ps=con.prepareStatement(str);
				ps.setString(1, getWorkloadString());
				ps.setInt(2, batchNo);
				ResultSet rs=ps.executeQuery();
				
				while(rs.next()){
					// VM description
					int vmid = rs.getInt("VMID"); //vmid
					int mips = rs.getInt("MIPS");
					int size = rs.getInt("SIZE"); // image size (MB) 
					int ram = rs.getInt("RAM");// vm memory (MB)
					int bw = rs.getInt("BW");
					int pesNumber = rs.getInt("PESNUMBER"); // number of cpus
					int duration=rs.getInt("DURATION");
					String vmm = rs.getString("VMM"); // VMM name
					//System.out.println(vmid);
					// create VM
					PAMVm vm = new PAMVm(vmid,getId(), mips, pesNumber, ram, bw, size,vmm, new CloudletSchedulerSpaceShared(),duration);

					// add the VM to the vmList
					generatingList=true;
					vmlist.add(vm);	
				}
				
				batchNo++;
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



	public void setWorkloadString(String workloadString) {
		this.workloadString = workloadString;
	}



	public String getWorkloadString() {
		return workloadString;
	}



	public void setTotalBatches(int totalBatches) {
		this.totalBatches = totalBatches;
	}



	public int getTotalBatches() {
		return totalBatches;
	}



	public static void setBatchNo(int batchNo) {
		PAMDatacenterBrokerExtended.batchNo = batchNo;
	}



	public static int getBatchNo() {
		return batchNo;
	}
	


}
