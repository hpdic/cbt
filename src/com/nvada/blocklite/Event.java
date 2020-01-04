package com.nvada.blocklite;
import java.sql.Timestamp;

import com.nvada.blocklite.data.DataCell;

public class Event implements Comparable<Event>{

	//eventType is receiveBlock = 1, generateBlock = 2, receiveTransaction = 3, generateTransaction = 4.
	private int eventType;
	private Block eventBlock=null;
	
	private int creatorNodeId; //Used only in case of block events handling
	private int senderNodeId; 	//id of the node which forwards this transaction
	
	private int receiverNodeId;
	private DataCell dataCell = null;
	private Timestamp eventTimestamp ;
	
	private boolean executed = false;
	
	public final static int RECEIVE_BLOCK_EVENT = 1;			// receive block
	public final static int GENERATE_BLOCK_EVENT = 2;			// generate block
	public final static int RECEIVE_DATA_CELL_EVENT = 3;		// receive transaction
	public final static int GENERATE_DATA_CELL_EVENT = 4;		// generate transaction
	
	public static Event generateBlockEvent(Block eventBlock, Timestamp eventTimestamp, int nodeId) {
		return new Event(Event.GENERATE_BLOCK_EVENT, eventBlock, eventTimestamp, nodeId);
	}
	
	public static Event receiveBlockEvent(Block eventBlock, Timestamp eventTimestamp, int recvNodeId, int senderNodeId) {
		return new Event(Event.RECEIVE_BLOCK_EVENT, eventBlock, eventTimestamp, recvNodeId, senderNodeId);
	}
	
	public static Event generateDataCellEvent(DataCell cell, Timestamp eventTimestamp) {
		return new Event(Event.GENERATE_DATA_CELL_EVENT, cell, eventTimestamp);
	}
	
	public static Event receiveDataCellEvent(DataCell cell, Timestamp eventTimestamp, int recvNodeId, int senderNodeId) {
		return new Event(Event.RECEIVE_DATA_CELL_EVENT, cell, eventTimestamp, recvNodeId, senderNodeId);
	}
	
	//constructors to create various types of events
	private Event(int eventType, Block eventBlock, Timestamp eventTimestamp, int nodeId){
		this.eventType = eventType;
		this.eventBlock = eventBlock;
		this.eventTimestamp = eventTimestamp;
		this.creatorNodeId = nodeId;
	}

	private Event(int eventType, Block eventBlock, Timestamp eventTimestamp, int recvNodeId, int senderNodeId){
		this.eventType = eventType;
		this.eventBlock = eventBlock;
		this.eventTimestamp = eventTimestamp;
		this.receiverNodeId = recvNodeId;
		this.senderNodeId = senderNodeId;
	}

	private Event(int eventType, DataCell eventDataCell, Timestamp eventTimestamp){
		this.eventType = eventType;
		this.dataCell = eventDataCell;
		this.eventTimestamp = eventTimestamp;
	}

	private Event(int eventType, DataCell eventDataCell, Timestamp eventTimestamp, int recvNodeId, int senderNodeId){
		this.eventType = eventType;
		this.dataCell = eventDataCell;
		this.eventTimestamp = eventTimestamp;
		this.receiverNodeId = recvNodeId;
		this.senderNodeId = senderNodeId;
	}

	//Creating a function to compare events
	public int compareTo(Event otherEvent){
		if(this.eventTimestamp.before(otherEvent.getEventTimestamp())){
			return -1;
		}
		else if(this.eventTimestamp.after(otherEvent.getEventTimestamp())){
			return 1;
		}
		else{
			return 0;
		}
	}

	//function to update senderNum
	public void updateSender(int newSenderNodeId){
		this.senderNodeId = newSenderNodeId;
	}

	//function to update receiverNum
	public void updateReceiver(int newReceiverNodeId){
		this.receiverNodeId = newReceiverNodeId;
	}

	//function to retrieve event timestamp
	public Timestamp getEventTimestamp(){
		return eventTimestamp;
	}

	//function to retrieve event type
	public int getEventType(){
		return eventType;
	}
	
	//function to retrieve event type
	public String getEventName(){
		if(getEventType() == Event.RECEIVE_BLOCK_EVENT) {
			return "RECEIVE BLOCK " + receiverNodeId;
		} else if(getEventType() == Event.GENERATE_BLOCK_EVENT) {
			return "GENERATE BLOCK " + creatorNodeId;
		} else if(getEventType() == Event.RECEIVE_DATA_CELL_EVENT) {
			return "RECEIVE TRANSCATION " + receiverNodeId;
		} else if(getEventType() == Event.GENERATE_DATA_CELL_EVENT){
			return "GENERATE TRANSCATION";
		} else {
			return "Wrong Event";
		}
	}

	//function to retrieve event status i.e whether that is being already executed or not
	public boolean getEventStatus(){
		return executed;
	}

	//function to retrieve event block corresponding to it
	public Block getEventBlock(){
		return eventBlock;
	}

	//function to retrieve event transaction corresponding to it
	public DataCell getEventDataCell(){
		return dataCell;
	}

	public void changeEventStatus(){
		this.executed = true;
	}

	//return id of the node to whom it is forwarded
	public int getReceiverNodeId(){
		return receiverNodeId;
	}

	//num of the node which forwarded this
	public int getSenderNodeId(){
		return senderNodeId;
	}

	//id number of the creator of the block is returned
	public int getCreatorNodeId(){
		return this.creatorNodeId;
	}
}