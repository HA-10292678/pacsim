package org.cloudbus.cloudsim.dynamicworkloadpattern;



public class RandomVmRequestGenerationAlgorithm {
	
	public static void main(String args[]){
		

		//PAMDatacenetrBroker Constructor;
		// First  arg-->workload pattern name
		// Second arg-->maxVMRequest in a batch
		// Third arg-->maxTimeDuration i.e Execution Time for each VM
		// Fourth arg--> Max NoOfPeForVM; since we are packing the VMs within a chip and we are not placing a VM across chip to avoid latency,resulting in performance degradation
		// Fifth arg--> BatchingTimeDelay; Always making it 5;At every 5 time units, a batch of request is sent 
		// Sixth arg-->No Of BatchSubmission
		// Seventh arg-->maxRam
		DynamicVmCreation dVC=new DynamicVmCreation ("DynamicWorkload_5",20,10,8,60,10,1);
		// Pattern for generating PEs for a VM 10% of VM asks PEs in first SubRange; 10% of VM asks PEs second SubRange; 80% of VM asks VM in Third SubRange; 
		
		// PEs in the range 1-50 is asked by 10% of VM;
		// PEs in the range 51-75 is asked by 10% of VM;
		// PEs in the range 76-100 is asked by 80% of VM;
		/*
		 *To generate drastic smooth pattern 
		 */
		//DynamicVmCreation.setRandomNoSubRange(0,100,0);
		//DynamicVmCreation.setRandomPeRange(38,38,24);
		
		/*
		 *To generate drastic workload pattern 
		 */
		DynamicVmCreation.setRandomNoSubRange(50,0,50);
		DynamicVmCreation.setRandomPeRange(25,50,25);
		
	
		
	
		/*
		 *To generate mixed smooth pattern 
		 */
		//DynamicVmCreation.setMixedPattern(true);
		
		/*
		 *To generate random time duration
		 */
		DynamicVmCreation.setRandomNoSubRangeforDuration(10, 10, 80);
		DynamicVmCreation.setRandomDurationRange(50, 20, 30);
		
		dVC.generateRandomVmList();
	}

}
