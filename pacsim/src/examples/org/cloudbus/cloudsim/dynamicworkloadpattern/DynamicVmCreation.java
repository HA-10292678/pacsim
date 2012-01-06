package org.cloudbus.cloudsim.dynamicworkloadpattern;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.cloudbus.cloudsim.Log;



public class DynamicVmCreation{
	
	private static int noOfBatchs = 0;
	
	public ResultSet rs;
	
	public Connection con;
	
	public PreparedStatement stmt;
	
	public PreparedStatement ps;
	
	private int maxVmRequest;
	
	private int maxTimeDurationOfVm;

	private int maxNoOfPeForVm;
	
	private int noOfBatchSubmission;
	
	private int maxRam;

	private static int rangePercent1;
	
	private static int rangePercent2;
	
	private static int rangePercent3;
	
	private static int pePercent1;
	
	private static int pePercent2;
	
	private static int pePercent3;
	
	private static int RangePercentDur1;
	
	private static int RangePercentDur2;
	
	private static int RangePercentDur3;
	
	private static int durationPercent1;
	
	private static int durationPercent2;
	
	private static int durationPercent3;
	
	private static boolean mixedPattern=false;
	
	private String name;
	
	private static int vmid=0;
	
	public DynamicVmCreation(String name,int maxVmRequest,int maxTimeDurationOfVm,int maxNoOfPeForVm,int batchingTimeDelay,int noOfBatchSubmission,int maxRam) {
		
		setName(name);
		setMaxVmRequest(maxVmRequest);
		setMaxTimeDurationOfVm(maxTimeDurationOfVm);
		setMaxNoOfPeForVm(maxNoOfPeForVm);
		setNoOfBatchSubmission(noOfBatchSubmission);
		setMaxRam(maxRam);
		
		initilizeDB();
	}
	
	public void initilizeDB(){
		try {
			Class.forName("sun.jdbc.odbc.JdbcOdbcDriver");
			con=DriverManager.getConnection("jdbc:odbc:ClientRequestDB");

		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	
}
	public void generateRandomVmList(){
		
		//int vmid = 0;
		int mips = 1000;
		int size = 10000; // image size (MB)
		int ram = 512; // vm memory (MB)
		int bw = 1000;
		int pesNumber = 0; // number of cpus
		int duration=10;//duration of a vm
		String vmm = "Xen"; // VMM name
	//	int brokerId=4;
		int count=0;
		
		try {
			/*String check="select * from workload where WORKLOADID=?";
			PreparedStatement ps1=con.prepareStatement(check);
			ps1.setString(1, name);
			ResultSet rs=ps1.executeQuery();
			String wName=rs.getString("WORKLOADID");
			if(wName.equals(name)){
				throw new SQLException("new");
			}*/
			
			String str1="insert into workload values (?,?)";
			ps=con.prepareStatement(str1);
			ps.setString(1,name);
			ps.setInt(2, noOfBatchSubmission);
			ps.executeUpdate();
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		
		while(noOfBatchs!=getNoOfBatchSubmission()){
			
			if(mixedPattern==true){
				if(noOfBatchs<(getNoOfBatchSubmission()/2)){
					DynamicVmCreation.setRandomNoSubRange(0,100,0);
					DynamicVmCreation.setRandomPeRange(38,38,24);
				}else{
					DynamicVmCreation.setRandomNoSubRange(50,0,50);
					DynamicVmCreation.setRandomPeRange(25,50,25);
				}
			}
			
		int vmSize=(int) ((Math.random()*getMaxVmRequest())+1);	//vm size the no of vms submitted for a batch
			System.out.println("Batch no"+noOfBatchs+" and size "+vmSize);
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
				randRange=(int)((Math.random())*100)+1;
				if((randRange>0)&&(randRange<=getRangePercentDur1())){
					duration=(int)((Math.random())*((getMaxTimeDurationOfVm()*getDurationPercent1())/100))+1;
				}else if((randRange>getRangePercentDur1())&&(randRange<=getRangePercentDur2())){
					duration=(((getMaxTimeDurationOfVm()*getDurationPercent1())/100))+(int)((Math.random())*((getMaxTimeDurationOfVm()*getDurationPercent2())/100))+1;
				}else{
					duration=(((getMaxTimeDurationOfVm()*(getDurationPercent1()+getDurationPercent2()))/100))+(int)((Math.random())*((getMaxTimeDurationOfVm()*getDurationPercent3())/100))+1;
				}
				//duration=(int)((Math.random())*getMaxTimeDurationOfVm())+1;
				ram =((int)((Math.random())*getMaxRam())+1)*512;
	
				try {
					String str="insert into VMRequests values(?,?,?,?,?,?,?,?,?,?)";
					stmt=con.prepareStatement(str);
					stmt.setString(1, name);
					stmt.setInt(2, noOfBatchs);
					stmt.setInt(3, vmid);
				//	stmt.setInt(4, brokerId);
					stmt.setInt(4, mips);
					stmt.setInt(5, size);
					stmt.setInt(6, ram);
					stmt.setInt(7, bw);
					stmt.setInt(8, pesNumber);
					stmt.setString(9, vmm);
					stmt.setInt(10, duration);
					stmt.executeUpdate();
					count++;
					
				} catch (SQLException e) {
					e.printStackTrace();
				}
				
				vmid++;
			}

			noOfBatchs++;
		}
		System.out.println("Number of values inserted: "+count);
		
		try {
			con.close();
			System.out.println("Connection Closed");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	
	}
	
	public static void setRandomNoSubRange(int rangePercent1, int rangePercent2, int rangePercent3) {
		Log.printLine("Random number sub ranges : 0-"+rangePercent1+", "+(rangePercent1+1)+"-"+(rangePercent1+rangePercent2)+", "+(rangePercent1+rangePercent2+1)+"-"+(rangePercent1+rangePercent2+rangePercent3));
		setRangePercent1(rangePercent1);
		setRangePercent2(rangePercent2);
		setRangePercent3(rangePercent3);
		
	}
	
	public static void setRandomNoSubRangeforDuration(int rangePercent1, int rangePercent2, int rangePercent3) {
		Log.printLine("Random number sub ranges for Duration: 0-"+rangePercent1+", "+(rangePercent1+1)+"-"+(rangePercent1+rangePercent2)+", "+(rangePercent1+rangePercent2+1)+"-"+(rangePercent1+rangePercent2+rangePercent3));
		setRangePercentDur1(rangePercent1);
		setRangePercentDur2(rangePercent2);
		setRangePercentDur3(rangePercent3);
		
	}

	public static void setRandomPeRange(int pePercent1, int pePercent2, int pePercent3) {
		Log.printLine("Pe Range : 0-"+pePercent1+", "+(pePercent1+1)+"-"+(pePercent1+pePercent2)+", "+(pePercent1+pePercent2+1)+"-"+(pePercent1+pePercent2+pePercent3));
		setPePercent1(pePercent1);
		setPePercent2(pePercent2);
		setPePercent3(pePercent3);
		
	}
	
	public static void setRandomDurationRange(int durationPercent1, int durationPercent2, int durationPercent3) {
		Log.printLine("Pe Range : 0-"+durationPercent1+", "+(durationPercent1+1)+"-"+(durationPercent1+durationPercent2)+", "+(durationPercent1+durationPercent2+1)+"-"+(durationPercent1+durationPercent2+durationPercent3));
		setDurationPercent1(durationPercent1);
		setDurationPercent2(durationPercent2);
		setDurationPercent3(durationPercent3);
		
	}
	public int getMaxVmRequest() {
		return maxVmRequest;
	}
	public void setMaxVmRequest(int maxVmRequest) {
		this.maxVmRequest = maxVmRequest;
	}
	public int getMaxTimeDurationOfVm() {
		return maxTimeDurationOfVm;
	}
	public void setMaxTimeDurationOfVm(int maxTimeDurationOfVm) {
		this.maxTimeDurationOfVm = maxTimeDurationOfVm;
	}
	public int getMaxNoOfPeForVm() {
		return maxNoOfPeForVm;
	}
	public void setMaxNoOfPeForVm(int maxNoOfPeForVm) {
		this.maxNoOfPeForVm = maxNoOfPeForVm;
	}
	
	public int getNoOfBatchSubmission() {
		return noOfBatchSubmission;
	}
	public void setNoOfBatchSubmission(int noOfBatchSubmission) {
		this.noOfBatchSubmission = noOfBatchSubmission;
	}
	public int getMaxRam() {
		return maxRam;
	}
	public void setMaxRam(int maxRam) {
		this.maxRam = maxRam;
	}
	public static int getRangePercent1() {
		return rangePercent1;
	}
	public static void setRangePercent1(int rangePercent1) {
		DynamicVmCreation.rangePercent1 = rangePercent1;
	}
	public static int getRangePercent2() {
		return rangePercent2;
	}
	public static void setRangePercent2(int rangePercent2) {
		DynamicVmCreation.rangePercent2 = rangePercent2;
	}
	public static int getRangePercent3() {
		return rangePercent3;
	}
	public static void setRangePercent3(int rangePercent3) {
		DynamicVmCreation.rangePercent3 = rangePercent3;
	}
	public static int getPePercent1() {
		return pePercent1;
	}
	public static void setPePercent1(int pePercent1) {
		DynamicVmCreation.pePercent1 = pePercent1;
	}
	public static int getPePercent2() {
		return pePercent2;
	}
	public static void setPePercent2(int pePercent2) {
		DynamicVmCreation.pePercent2 = pePercent2;
	}
	public static int getPePercent3() {
		return pePercent3;
	}
	public static void setPePercent3(int pePercent3) {
		DynamicVmCreation.pePercent3 = pePercent3;
	}
	public static void setNoOfBatchs(int noOfBatchs) {
		DynamicVmCreation.noOfBatchs = noOfBatchs;
	}
	public static int getNoOfBatchs() {
		return noOfBatchs;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public static int getRangePercentDur1() {
		return RangePercentDur1;
	}

	public static void setRangePercentDur1(int rangePercentDur1) {
		RangePercentDur1 = rangePercentDur1;
	}

	public static int getRangePercentDur2() {
		return RangePercentDur2;
	}

	public static void setRangePercentDur2(int rangePercentDur2) {
		RangePercentDur2 = rangePercentDur2;
	}

	public static int getRangePercentDur3() {
		return RangePercentDur3;
	}

	public static void setRangePercentDur3(int rangePercentDur3) {
		RangePercentDur3 = rangePercentDur3;
	}

	public static int getDurationPercent1() {
		return durationPercent1;
	}

	public static void setDurationPercent1(int durationPercent1) {
		DynamicVmCreation.durationPercent1 = durationPercent1;
	}

	public static int getDurationPercent2() {
		return durationPercent2;
	}

	public static void setDurationPercent2(int durationPercent2) {
		DynamicVmCreation.durationPercent2 = durationPercent2;
	}

	public static int getDurationPercent3() {
		return durationPercent3;
	}

	public static void setDurationPercent3(int durationPercent3) {
		DynamicVmCreation.durationPercent3 = durationPercent3;
	}

	public static void setMixedPattern(boolean mixedPattern) {
		DynamicVmCreation.mixedPattern = mixedPattern;
	}

	public static boolean isMixedPattern() {
		return mixedPattern;
	}
	
	
	
	
}