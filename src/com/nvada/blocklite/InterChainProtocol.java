package com.nvada.blocklite;

import com.nvada.blocklite.data.DataCell;
import com.nvada.blocklite.dataservice.DataService.DataCellListener;

public interface InterChainProtocol {
	public void sendCell(DataCell cell);
	public void recvCell(DataCell cell);
	public void addDataCellListener(DataCellListener listener);
	public void stop();
}
