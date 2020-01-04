package com.nvada.blocklite.data;

public enum DataCellType {
	Blank(0, "Blank"), 
	Message(1, "Message"), 
	Jpeg(2, "Jpeg"), 
	Transaction(3, "Transaction");
	
	private int value;
	private String name;
	
	private DataCellType(int value, String name) {
		this.value = value;
		this.name = name;
	}
	
	public int getValue() {
		return this.value;
	}
	
	@Override
	public String toString() {
		return name;
	}

}
