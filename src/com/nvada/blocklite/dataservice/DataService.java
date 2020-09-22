package com.nvada.blocklite.dataservice;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;

import com.nvada.blocklite.data.BlankDataCell;
import com.nvada.blocklite.data.DataCell;


public class DataService {
	
	private final static int MAX_DATA_SIZE = 100;

	private ArrayBlockingQueue<DataCell> pendingCell = null;
	private List<DataCellListener> listeners;
	
	public DataService() {
		this.listeners = new ArrayList<DataCellListener>();
		this.pendingCell = new ArrayBlockingQueue<DataCell>(MAX_DATA_SIZE);
	}
	
	public void addDataCellListener(DataCellListener listener) {
		this.listeners.add(listener);
	}
	
	public void removeDataCellListener(DataCellListener listener) {
		this.listeners.remove(listener);
	}
	
	public boolean addDataCell(DataCell e) {
		return pendingCell.offer(e);
	}
	
	public int listenerSize() {
		return this.listeners.size();
	}

	public DataCell getDataCell(boolean generateBlank) {
		DataCell cell =  pendingCell.poll();
		
		if(cell == null) {
			if(generateBlank) {
				cell = new BlankDataCell();
			}  else {
				return null;
			}
		}
		
		for(DataCellListener listener: this.listeners) {
			listener.onDataCellDidAdd(cell);
		}
		
		return cell;
	}
	
	public static interface DataCellListener {
		public void onDataCellDidAdd(DataCell cell);
		
	}; 
}