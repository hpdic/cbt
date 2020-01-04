package com.nvada.blocklite.data;
import java.sql.Timestamp;

import com.nvada.blocklite.config.Constants;

public class JpegDataCell extends DataCell {
	
	public JpegDataCell(byte[] jpegData) {
		super(Constants.JPEG_ID, Constants.GOD_ID, new Timestamp(System.currentTimeMillis()));
		this.data = new byte[] {'\0'};
	}
	
	@Override
	public DataCellType type() {
		return DataCellType.Jpeg;
	}
}