package com.nvada.blocklite;
import java.sql.Timestamp;
//import java.text.DateFormat;
//import java.text.SimpleDateFormat;
import java.util.ArrayList;

import com.nvada.blocklite.config.Constants;
import com.nvada.blocklite.data.DataCell;
import com.nvada.blocklite.proof.ProofBase;
import com.nvada.blocklite.proof.ProofStack;
import com.nvada.blocklite.proof.ProofWork;

public class Block{

	private String uBlokckID;
	private Timestamp creationTime;
	
	public long receiveTime=0;
	private String creatorID;
	private Block parentBlock;
	private int depth = 0;
	
	//new properties for nonce implementation begins
	public String previousHash;
	
	//new properties for nonce implementation ends

	//This list contains all the transactions included in the block
	private ArrayList<String> childList = new ArrayList<String>();
	private int numChild = 0;
	public ArrayList<DataCell> cells = new ArrayList<DataCell>();
	
	// default proof
	private ProofBase proof = new ProofWork(5, 0);
	
	public static Block generateGenesisBlock(String uBlokckID, Timestamp creationTime) {
		return new Block(uBlokckID, creationTime);
	}
	
	private Block(String uBlokckID, Timestamp creationTime){
		this.uBlokckID = uBlokckID;
		this.creationTime = creationTime;
		this.creatorID = Constants.GOD_ID;
		this.parentBlock = null;
		this.depth = 0;
	}

	public Block(ProofBase proof, Timestamp creationTime, String creatorID, Block parentBlock, ArrayList<DataCell> cells) {
		this.creationTime = creationTime;
		this.creatorID = creatorID;
		this.parentBlock = parentBlock;
		this.depth = parentBlock.getDepth()+1;
		if(cells!=null){
			this.cells = cells;
		} else {
			this.cells = new ArrayList<DataCell>();
		}
		
		if(proof != null) {
			this.proof = proof;
		} else {
			this.proof = new ProofStack(5,0) ;
		}
		
		this.uBlokckID = this.proof.calculateHash(this);
	}

	public Block(Block b){
		this.uBlokckID = b.getBlockID();
		this.creationTime = b.getCreationTime();
		this.creatorID = b.getCreatorID();
		this.parentBlock = b.getParentBlock();
		this.depth = b.depth;
		this.childList = b.getChildList();
		this.numChild = this.childList.size();
		this.cells = b.getCells();
	}
	
	public ProofBase getProof() {
		return proof;
	}

	public void setProof(ProofBase proof) {
		this.proof = proof;
	}

	//get transcation list;
	public ArrayList<DataCell> getCells(){
		return this.cells;
	}
	
	//function to add txns to a block
	public void addTxn(DataCell newTxn){
		this.cells.add(newTxn);
	}

	//returning a transaction from the block using correspondng transaction id
	public DataCell getTxn(String txnID){
		for(int i = 0; i< this.cells.size(); i++){
			if(cells.get(i).getDtuId().equals(txnID)){
				return cells.get(i);
			}
		}
		return null;
	}

	//to check whether a txn with particular id has been there in the list or not
	public boolean containsTxn(String txnID){
		for(int i = 0; i<this.cells.size(); i++){
			if(cells.get(i).getDtuId().equals(txnID)){
				return true;
			}
		}
		return false;
	}

	//To store all list of the childIDs
	public ArrayList<String> getChildList() {
		return childList;
	}

	public void putChild(String newChildID){
		childList.add(numChild++, newChildID);
	}

	public boolean checkChild(String childID){
		for(int i=0; i<numChild; i++){
			if(childID.equals(childList.get(i))){
				return true;
			}
		}
		return false;
	}
	
	public float getOwnerWeight() {
		float weight = 0;
		
		//Note: get parent block's transaction list for a given block from DB
		Block blk_iter = this;
		
		String ownerId = this.creatorID;
		
		while(blk_iter!=null) {			
			if(blk_iter.cells != null && blk_iter.cells.size() > 0) {
				
				for(DataCell cell: blk_iter.cells) {
					if(cell.getWeight() == 0) {
						continue;
					}
					
					if(ownerId.equals(cell.getSenderID())) {
						// deducting weight if current node is sender
						weight -= cell.getWeight();
					} else if(ownerId.equals(cell.getReceiverID())){
						//incrementing weight if current node is receiver
						weight += cell.getWeight();
					}
				}
			}

			blk_iter = blk_iter.getParentBlock();
		}
		
		return weight;
	}

	//to return block ID
	public String getBlockID(){
		return uBlokckID;
	}
	
	//to return parent Block
	public Block getParentBlock(){
		return parentBlock;
	}

	//to return block id of the parent node
	public String getParentBlockID(){
		return this.parentBlock.getBlockID();
	}

	//to return id of the creator of the block
	public String getCreatorID(){
		return this.creatorID;
	}
	
	//to set a new parent block
	public void setParentBlock(Block newParentBlock){
		this.parentBlock = newParentBlock;
	}

	//to return creation time of the block
	public Timestamp getCreationTime(){
		return creationTime;
	}

	//to get the depth of the current block
	public int getDepth(){
		return this.depth;
	}

	//To check whether the block is genesys or not
	public boolean checkGenesis(){
		if(this.uBlokckID.equals("genesis")){
			return true;
		}
		return false;
	}	

	//Do not see any use of the follwing two function
	public void printBlock(String ident){
		System.out.println(ident+"Block UID:" + this.uBlokckID);
		System.out.println(ident+"Creation Time:" + this.creationTime);
		System.out.println(ident+"Creator ID:" + this.creatorID);
		System.out.println(ident+"Previous Block UID:" + this.parentBlock.getBlockID());
	}
	
	public boolean matchBlockID(String newID){
		if(this.uBlokckID.equals(newID)){
			return true;
		}
		return false;
	}
	
	public boolean verifyHash() {
		return proof.verifyProof(this);
	}
	
	//Increases nonce value until hash target is reached.
	public void mineBlock() {
		uBlokckID = proof.generateProof(this);
	}
}
