package com.nvada.blocklite.dataservice;

import com.nvada.blocklite.reader.TransactionReader;

public class EtcDataService extends TxDataService {
	
	private static EtcDataService _instance = null;
	
	public static EtcDataService instance() {
		if(_instance == null) {
			_instance = new EtcDataService();
		}
		return _instance;
	}
	
	private EtcDataService() {
		readDataFromCsv();
	}

	@Override
	public void readDataFromCsv() {
		this.txList = new TransactionReader().readTxfromCsv("data/export-token.csv");
	}
}