package com.nvada.blocklite.data;

import java.sql.Timestamp;

import com.nvada.blocklite.config.Constants;

public abstract class DataCell {

	protected String dtuId;
	
	protected String senderID;
	protected String receiverID;
	
	protected float weight;
	
	protected byte[] data;
	
	protected Timestamp dtuTime;
	
	protected boolean isValid = true;
	
	public DataCell(String dtuId, String receiverID, Timestamp dtuTime) {
		this.dtuId = dtuId;
		this.receiverID = receiverID;
		this.dtuTime = dtuTime;
		
		this.senderID = Constants.GOD_ID;
		this.weight = 0;
	}
	
	//function to return unique transaction id
	public String getDtuId(){
		return dtuId;
	}

	//to return transaction amount
	public float getWeight(){
		if(senderID.equals(receiverID)) {
			return 0;
		}
		return weight;
	}
	
	//to update the amount of transaction
	public void setWeight(float newWeight){
		this.weight = newWeight;
	}

	//to return senderID
	public String getSenderID(){
		return senderID;
	}

	//to return recieverID
	public String getReceiverID(){
		return receiverID;
	}
	
	//to return time of transaction
	public Timestamp getDtuTime(){
		return dtuTime;
	}
	
	public byte[] getData() {
		return data;
	}

	public void setData(byte[] data) {
		this.data = data;
	}
	
	public boolean isValid(){
		return isValid;
	}
	
	public void setValid(boolean valid){
		this.isValid = valid;
	}
	
	public abstract DataCellType type();
}
