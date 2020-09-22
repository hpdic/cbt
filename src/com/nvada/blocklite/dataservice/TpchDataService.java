package com.nvada.blocklite.dataservice;

import com.nvada.blocklite.reader.TPCH_Reader;

public class TpchDataService extends TxDataService {
	
	private static TpchDataService _instance = null;
	
	public static TpchDataService instance() {
		if(_instance == null) {
			_instance = new TpchDataService();
		}
		return _instance;
	}
	
	private TpchDataService() {
		readDataFromCsv();
	}

	@Override
	public void readDataFromCsv() {
		this.txList = new TPCH_Reader().readTxfromCsv("data/orders.csv");
	}
}