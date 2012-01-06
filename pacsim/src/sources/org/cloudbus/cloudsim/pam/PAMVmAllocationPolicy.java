package org.cloudbus.cloudsim.pam;

import java.util.List;
import java.util.Map;

public abstract class PAMVmAllocationPolicy {


		/** The host list. */
		private List<? extends PAMHost> hostList;
		
		private int vmRequestNotFullfilled=0;
		
		private int vmFailed=0;
		/**
		 * Allocates a new VmAllocationPolicy object.
		 *
		 * @param list Machines available in this Datacentre
		 *
		 * @pre $none
		 * @post $none
		 */
		public PAMVmAllocationPolicy(List<? extends PAMHost> list) {
			setHostList(list);
		}

		/**
		 * Allocates a host for a given VM. The host to be allocated is the one
		 * that was already reserved.
		 *
		 * @param vm virtual machine which the host is reserved to
		 *
		 * @return $true if the host could be allocated; $false otherwise
		 *
		 * @pre $none
		 * @post $none
		 */
		public abstract boolean allocateHostForVm(PAMVm vm);

		/**
		 * Allocates a specified host for a given VM.
		 *
		 * @param vm virtual machine which the host is reserved to
		 *
		 * @return $true if the host could be allocated; $false otherwise
		 *
		 * @pre $none
		 * @post $none
		 */
		public abstract boolean allocateHostForVm(PAMVm vm, PAMHost host);

		/**
		 * Optimize allocation of the VMs according to current utilization.
		 *
		 * @param vmList the vm list
		 * @param utilizationBound the utilization bound
		 * @param time the time
		 *
		 * @return the array list< hash map< string, object>>
		 */
		public abstract List<Map<String, Object>> optimizeAllocation(List<? extends PAMVm> vmList);

		/**
		 * Releases the host used by a VM.
		 *
		 * @param vm the vm
		 *
		 * @pre $none
		 * @post $none
		 */
		public abstract void deallocateHostForVm(PAMVm vm);

		/**
		 * Get the host that is executing the given VM belonging to the
		 * given user.
		 *
		 * @param vm the vm
		 *
		 * @return the Host with the given vmID and userID; $null if not found
		 *
		 * @pre $none
		 * @post $none
		 */
		public abstract PAMHost getHost(PAMVm vm);

		/**
		 * Get the host that is executing the given VM belonging to the
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
		public abstract PAMHost getHost(int vmId, int userId);

		/**
		 * Sets the host list.
		 *
		 * @param hostList the new host list
		 */
		protected void setHostList(List<? extends PAMHost> hostList) {
			this.hostList = hostList;
		}

		/**
		 * Gets the host list.
		 *
		 * @return the host list
		 */
		@SuppressWarnings("unchecked")
		public <T extends PAMHost> List<T> getHostList() {
			return (List<T>) hostList;
		}

		public void setVmRequestNotFullfilled(int vmRequestNotFullfilled) {
			this.vmRequestNotFullfilled = vmRequestNotFullfilled;
		}

		public int getVmRequestNotFullfilled() {
			return vmRequestNotFullfilled;
		}
		
		public void incVmRequestNotFullfilled(){
			vmRequestNotFullfilled++;
		}

		public void setVmFailed(int vmFailed) {
			this.vmFailed = vmFailed;
		}

		public int getVmFailed() {
			return vmFailed;
		}
		
		public void incVmFailed(){
			vmFailed++;
		}


	}

