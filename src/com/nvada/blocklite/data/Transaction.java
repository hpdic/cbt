package com.nvada.blocklite.data;
import java.sql.Timestamp;

public class Transaction extends DataCell {

	//Default constructor
	public Transaction(String uDtuId, String senderID, String receiverID, float amount, Timestamp dtuTime) {
		super(uDtuId, receiverID, dtuTime);
		this.senderID = senderID;
		this.receiverID = receiverID;
		this.weight = amount;
		this.dtuTime = dtuTime;
	}

	public Transaction(String uDtuId, String receiverID, Timestamp dtuTime) {
		super(uDtuId, receiverID, dtuTime);
		this.weight = 50;
	}

	//to return transaction status
	public  boolean getTxnStatus(){
		return isValid;
	}
	
	public float getAmount() {
		return weight;
	}

	//to update the amount of transaction
	public void updateAmount(float newAmount){
		this.weight = newAmount;
	}
	
	@Override
	public DataCellType type() {
		return DataCellType.Transaction;
	}

}