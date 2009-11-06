/*******************************************************************************
 * Copyright (c) 2009 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Alvaro Sanchez-Leon - Initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.lttng.ui.views.controlflow.model;

import java.util.HashMap;
import java.util.Iterator;

import org.eclipse.linuxtools.lttng.TraceDebug;
import org.eclipse.linuxtools.lttng.ui.model.trange.TimeRangeEventProcess;

/**
 * Contains the processes in use by the Control flow view
 * 
 * @author alvaro
 * 
 */
public class FlowProcessContainer {
	// ========================================================================
	// Data
	// ========================================================================
	private final HashMap<ProcessKey, TimeRangeEventProcess> allProcesses = new HashMap<ProcessKey, TimeRangeEventProcess>();
	private static Integer uniqueId = 0;
	
	// ========================================================================
	// Constructor
	// ========================================================================

	/**
	 * Package level constructor
	 */
	FlowProcessContainer() {

	}

	// ========================================================================
	// Methods
	// ========================================================================
	/**
	 * Interface to add a new process.<p>
	 * 
	 * Note : Process with the same key will be overwritten, it's calling function job to make sure the new process is unique.
	 * 
	 * @param newProcess   The process to add
	 */
	public void addProcess(TimeRangeEventProcess newProcess) {
		if (newProcess != null) {
			allProcesses.put(new ProcessKey(newProcess), newProcess);
		}
	}
	
	/**
     * Request a unique ID
     * 
     * @return Integer
     */
    public Integer getUniqueId() {
        return uniqueId++;
    }
    
    /**
     * This method is intended for read only purposes in order to keep the
     * internal data structure in synch
     * 
     * @return TimeRangeEventProcess[]
     */
	public TimeRangeEventProcess[] readProcesses() {
	    // This allow us to return an Array of the correct type of the exact correct dimension, without looping
		return allProcesses.values().toArray(new TimeRangeEventProcess[allProcesses.size()]);
	}
	
	/**
     * Clear the children information for processes related to a specific trace
     * e.g. just before refreshing data with a new time range
     * 
     * @param traceId   The trace unique id (trace name?) on which we need to eliminate children.
     */
	public void clearChildren(String traceId) {
	    TimeRangeEventProcess process = null;
        Iterator<ProcessKey> iterator = allProcesses.keySet().iterator();
        
        while (iterator.hasNext()) {
            process = allProcesses.get(iterator.next());
            
            if (process.getTraceID().equals(traceId)) {
                // Reset clear childEventComposites() and traceEvents()
                // Also restore the nextGoodTime to the insertionTime for the drawing
                process.reset();
            }
        }
	}
	
	/**
     * Clear all process items
     */
    public void clearProcesses() {
        allProcesses.clear();
    }
	
    /**
     * Remove the process related to a specific trace e.g. during trace
     * removal
     * 
     * @param traceId   The trace unique id (trace name?) on which we want to remove process
     */
	public void removeProcesses(String traceId) {
	    ProcessKey iterKey = null;

        Iterator<ProcessKey> iterator = allProcesses.keySet().iterator();
        while (iterator.hasNext()) {
            iterKey = iterator.next();
            
            if (allProcesses.get(iterKey).getTraceID().equals(traceId)) {
                allProcesses.remove(iterKey);
            }
        }
	}
	
    /**
     * Search by keys (pid, cpuId, traceId and creationTime)<p>
     * 
     * A match is returned if the four arguments received match an entry
     *  Otherwise null is returned
     *  
     * @param searchedPid       The processId (Pid) we are looking for
     * @param searchedCpuId     The cpu Id we are looking for
     * @param searchedTraceID   The traceId (trace name?) we are looking for
     * @param searchedCreationtime The creation time we are looking for
     * 
     * @return TimeRangeEventProcess
     */
    public TimeRangeEventProcess findProcess(Long searchedPid, Long searchedCpuId, String searchedTraceID, Long searchedCreationtime) {
        // Get the TimeRangeEventProcess associated to a key we create here
        TimeRangeEventProcess foundProcess = allProcesses.get( new ProcessKey(searchedPid, searchedCpuId, searchedTraceID, searchedCreationtime) );
        
        return foundProcess;
    }
}


class ProcessKey {
    private TimeRangeEventProcess valueRef = null;
    
    private Long    pid = null;
    private Long    cpuId = null;
    private String  traceId = null;
    private Long    creationtime = null;
    
    @SuppressWarnings("unused")
    private ProcessKey() { }
    
    public ProcessKey(TimeRangeEventProcess newRef) {
        valueRef = newRef;
    }
    
    public ProcessKey(Long newPid, Long newCpuId, String newTraceId, Long newCreationTime) {
        pid = newPid;
        cpuId = newCpuId;
        traceId = newTraceId;
        creationtime = newCreationTime;
    }
    
    @Override
    public boolean equals(Object obj) {
        boolean isSame = false;
        
        if ( obj instanceof ProcessKey ) {
        	ProcessKey procKey = (ProcessKey) obj;
        	
        	if ( valueRef != null ) {
	            if ( (procKey.getPid().equals(valueRef.getPid()) ) &&
	                 (procKey.getTraceId().equals(valueRef.getTraceID()) ) &&
	                 (procKey.getCpuId().equals(valueRef.getCpu()) ) &&
	                 (procKey.getCreationtime().equals(valueRef.getCreationTime()) )  )
	            {
	                isSame = true;
	            }
        	}
        	else {
        		if ( (procKey.getPid().equals(this.pid ) ) &&
   	                 (procKey.getTraceId().equals(this.traceId ) ) &&
   	                 (procKey.getCpuId().equals(this.cpuId ) ) &&
   	                 (procKey.getCreationtime().equals(this.creationtime ) )  )
   	            {
   	                isSame = true;
   	            }
        	}
        }
        else {
        	TraceDebug.debug("ERROR : The given key is not of the type ProcessKey!" + obj.getClass().toString());
        }
        
        return isSame;
    }
    
    // *** WARNING : Everything in there work because the check "valueRef != null" is the same for ALL getter
    // Do NOT change this check without checking.
    public Long getPid() {
    	if ( valueRef != null ) {
            return valueRef.getPid();
        }
        else {
            return pid;
        }
    }

    public Long getCpuId() {
        if ( valueRef != null ) {
            return valueRef.getCpu();
        }
        else {
            return cpuId;
        }
    }
    
    public String getTraceId() {
        if ( valueRef != null ) {
            return valueRef.getTraceID();
        }
        else {
            return traceId;
        }
    }
    
    public Long getCreationtime() {
        if ( valueRef != null ) {
            return valueRef.getCreationTime();
        }
        else {
            return creationtime;
        }
    }
    
    @Override
    public int hashCode() {
    	return this.toString().hashCode();
    }
    
    
    @Override
    public String toString() {
        if ( valueRef != null ) {
            return (valueRef.getPid().toString() + ":" + valueRef.getCpu().toString() + ":" + valueRef.getTraceID().toString() + ":" + valueRef.getCreationTime().toString());
        } 
        
        return (pid + ":" + cpuId + ":" + traceId + ":" + creationtime);
    }
}
