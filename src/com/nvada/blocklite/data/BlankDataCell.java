package com.nvada.blocklite.data;

import java.sql.Timestamp;

import com.nvada.blocklite.config.Constants;

public class BlankDataCell extends DataCell {
	
	public BlankDataCell() {
		super(Constants.BLANK_ID, Constants.GOD_ID, new Timestamp(System.currentTimeMillis()));
	}
	
	@Override
	public DataCellType type() {
		return DataCellType.Blank;
		
	}
}
