package com.nvada.blocklite.data;

import java.sql.Timestamp;

import com.nvada.blocklite.config.Constants;

public class MessageDataCell extends DataCell {
	
	private String message;
	
	public MessageDataCell(String message) {
		super(Constants.MSG_ID, Constants.GOD_ID, new Timestamp(System.currentTimeMillis()));
		this.message = message;
	}	
	
	public String getMessage() {
		return message;
	}
	
	@Override
	public DataCellType type() {
		return DataCellType.Message;
		
	}
}
