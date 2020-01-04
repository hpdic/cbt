package com.nvada.blocklite;
import java.sql.Timestamp;
import java.util.Random;

import com.nvada.blocklite.data.Transaction;
import com.nvada.blocklite.log.Logger;
import com.nvada.blocklite.data.DataService.DataCellListener;
import com.nvada.blocklite.utils.MinnerTimer;
import com.nvada.blocklite.config.Constants;
import com.nvada.blocklite.data.DataCell;
import com.nvada.blocklite.data.DataService;

import java.util.ArrayList;
import java.util.PriorityQueue;
import java.util.HashMap;
import java.io.IOException;
import java.io.PrintWriter;

public class BlockChain implements InterChainProtocol {
	private int numPeers;
	private int mainDiff = 5;
	private int subDiff = 0;
	private int expectIntervalMills;
	private int blockIntervalMills;
	
	private double minPropDelay = 10;
	private double maxPropDelay = 500;
	private double qDelayParameter = 12.0/1024.0;
	
	private Boolean[] nodeTypes = null;
	private Boolean[][] connectionArray = null;
	
	private Double[] cpuPower = null;
	private Double[] txnMean = null;
	
	private Double[][] bottleNeck = null; 
	private Double[][] propagationDelay = null;
	
	private Double maxBottleNeck = 0.0;
	private Double maxPropagationDelay = 0.0;
	
	private int runMills;
	private long startMills;
	
	//Priortiy Queue of events to be executed and finished
	private PriorityQueue<Event> pendingEvents = new PriorityQueue<Event>();
	private PriorityQueue<Event> finishedEvents = new PriorityQueue<Event>();
	
	private Block genesisBlock  = null;
	private ArrayList<Node> nodeList = new ArrayList<Node>();
	
	private int txnCnt=0;
	private int blockCnt=0;
	private int staleCnt=0;
	private int totalCnt=0;
	
	private boolean runing = false;
	private boolean recoverWhenCrash = true;
	
	private int mainNodeId = 0;
	
	private DataService dataService = new DataService();
	
	public static int[][] DIFFCULT_MILLS = {
	{10,     30,    100,    500,    1000}, 
	{2000,   5000,  10000,  13000,  20000}, 
	{500000, 80000, 120000, 180000, 250000}};
	
	public BlockChain(int numPeers, int expectIntervalMills, int runMills) {
		if(numPeers > 0) {
			this.numPeers = numPeers;
		} else {
			this.numPeers = 1;
		}
		
		if(expectIntervalMills > 0) {
			this.expectIntervalMills = expectIntervalMills;
		} else {
			this.expectIntervalMills = 60 * 1000;
		}
		
		if(runMills > 0) {
			if(runMills > expectIntervalMills) {
				this.runMills = runMills;
			} else {
				this.runMills = 10 * expectIntervalMills;
			}
		}
	}
	
	@Override
	public void sendCell(DataCell cell) {
		dataService.addDataCell(cell);
	}
	
	@Override
	public void recvCell(DataCell cell) {
		dataService.addDataCell(cell);
	}
	
	@Override
	public void addDataCellListener(DataCellListener listener) {
		dataService.addDataCellListener(listener);
	}
	
	@Override
	public void stop() {
		this.runing = false;
	}
	
	public Node getMainNode() {
		return this.nodeList.get(mainNodeId);
	}
	
	public void setCrashOption(boolean recoverCrash) {
		this.recoverWhenCrash = recoverCrash;
	}
	
	public boolean run() {
		
		if(false == start() || false == selectDiffcult()) {
			this.runing = false;
			return false;
		}
		
		this.runing = true;
		
		createGenesisBlock();
		createConectionGraph();
		configPropagationDelay();
		initBlockAndvent();
		executeEvent();
		writeHistory();
		
		return true;
	}
	
	private boolean start() {
		minPropDelay = 10;
		maxPropDelay = 500;
		qDelayParameter = 12.0/1024.0;
		
		nodeList.removeAll(nodeList);
		pendingEvents.removeAll(pendingEvents);
		finishedEvents.removeAll(finishedEvents);
		
		this.startMills = System.currentTimeMillis();
		
		Timestamp start= new Timestamp(startMills);
		Logger.getInstance().logRun("start at: " + start);
		
		return true;
	}
	
	private boolean selectDiffcult() {
		int gap = 0;
		int minGap = Integer.MAX_VALUE;
		
		int[][] diffcultMills = MinnerTimer.readDiffcultsMills();
		
		if(diffcultMills == null) {
			diffcultMills = MinnerTimer.runMillsOfDiffcultsByPOW();
		}
		
		if(diffcultMills == null) {
			diffcultMills = DIFFCULT_MILLS;
		}
		
		if(diffcultMills.length < 1 || diffcultMills[0].length < 1) {
			Logger.getInstance().logError("select diffcult failed!!");
			return false;
		}
		
		this.mainDiff = diffcultMills.length - 1;
		this.subDiff = 0;
		
		for (int i=0; i<diffcultMills.length; i++ ) {
			for(int j=0; j<diffcultMills[i].length; j++ ) {
				gap = Math.abs(diffcultMills[i][j] - expectIntervalMills);
				if(gap < minGap) {
					minGap = gap;
					mainDiff = i;
					subDiff = j;
					blockIntervalMills = diffcultMills[i][j];
				}
			}
		}
		
		Logger.getInstance().logRun("select diffcult: " + mainDiff + "." + subDiff + ", expectIntervalMills: " +  expectIntervalMills + ", realIntervalMills: " + blockIntervalMills);
		
		return true;
	}
	
	//Genesys Block
	private void createGenesisBlock() {
		Timestamp genesisTime = new Timestamp(System.currentTimeMillis());
		this.genesisBlock = Block.generateGenesisBlock("genesis", genesisTime);
	
		//Generating numPeers number of nodes with randomly choosing fast and slow property
		//type true for fast nodes and false for lazy nodes
		this.nodeTypes = new Boolean[numPeers];
		Random randType = new Random(System.nanoTime());
		for(int i=0; i<numPeers; i++){
			nodeTypes[i] = (randType.nextInt()%2==0);;
			Timestamp creationTime = new Timestamp(System.currentTimeMillis());
			Node newNode = new Node(i, nodeTypes[i], creationTime, genesisBlock);
			nodeList.add(i, newNode);
		}
		
		Logger.getInstance().logRun("createGenesisBlock!!");
	}
	
	//to create a connencted graph with each node connected to a random number of other nodes
	private void createConectionGraph() {
		this.connectionArray = new Boolean[numPeers][numPeers];
		for(int i = 0; i<numPeers; i++){
			for(int j = 0; j<numPeers; j++){
				connectionArray[i][j]=false;
			}
		}
		
		// only one node
		if(numPeers <= 1) {
			Logger.getInstance().logRun("Blockchain with one node.");
			return;
		}
		
		Random connRand = new Random(System.nanoTime());
		Boolean[] tempConnection = new Boolean[numPeers];
		for(int i = 0; i<numPeers; i++){
			tempConnection[i] = false;
		}
		
		// ************** init first edge **************
		int n1Num = connRand.nextInt(numPeers);
		tempConnection[n1Num] = true;
		
		int n2Num = connRand.nextInt(numPeers);
		while(tempConnection[n2Num]){
			n2Num = connRand.nextInt(numPeers);
		}
		tempConnection[n2Num] = true;
		
		connectionArray[n1Num][n2Num] = true;
		connectionArray[n2Num][n1Num] = true;	
		Logger.getInstance().logRun("edge.1 (" + n1Num + ", " + n2Num + ") = true");
		
		// only two node
		if(numPeers <= 2) {
			Logger.getInstance().logRun("Blockchain with two node.");
			return;
		}
		
		// ************** add some edge to complete MST (Minimum Spanning Tree) **************
		
		int nEdge = 1;
		int newNum = 0;
		int oldNum = 0;
		while (nEdge < numPeers -1){
			newNum = connRand.nextInt(numPeers);
			while(tempConnection[newNum]) {
				newNum = connRand.nextInt(numPeers);
			}
			oldNum = connRand.nextInt(numPeers);
			while(!tempConnection[oldNum]){
				oldNum = connRand.nextInt(numPeers);
			}
	
			connectionArray[newNum][oldNum] = true;
			connectionArray[oldNum][newNum] = true;
			tempConnection[newNum] = true;
			nEdge++;
			Logger.getInstance().logRun("edge." + nEdge + " (" + newNum + ", " + oldNum + ") = true");
		}
		
		// ************** convert MST to network topology by add extra edges **************
	
		int maxRemainingEdges = numPeers * (numPeers-1) / 2 - nEdge + 1;
		int remainingEdges = connRand.nextInt(maxRemainingEdges);
		while(remainingEdges > 0){
			int i = connRand.nextInt(numPeers);
			int j = connRand.nextInt(numPeers);
			if(i!=j && !connectionArray[i][j]){
				connectionArray[i][j] = true;
				connectionArray[j][i] = true;
				nEdge++;
				remainingEdges--;
				Logger.getInstance().logRun("edge." + nEdge + " (" + i + ", " + j + ") = true");
			}
		}
		
		Logger.getInstance().logRun("createConectionGraph!!");
	}
	
	private void configPropagationDelay() {
		//Creating a 2D array to store Propagation delay between each pair of nodes
		this.propagationDelay = new Double[numPeers][numPeers];
		
		//Creating a array to store the bottle neck link between each pair of nodes
		this.bottleNeck = new Double[numPeers][numPeers];
		
		Node ni = null;
		Node nj = null;
		Random randProp = new Random(System.nanoTime());
		
		for(int i=0; i<numPeers; i++){			
			for(int j=0; j<numPeers; j++){
				if(i > j || connectionArray[i][j] == false) {
					continue;
				}
				
				ni = nodeList.get(i);
				nj = nodeList.get(j);
				
				// ************ config propagation delay ************
				// maintain the symmetry of the propagation delay
				ni.connect2Node(nj);
				nj.connect2Node(ni);
				propagationDelay[i][j] = minPropDelay + randProp.nextDouble()*(maxPropDelay - minPropDelay);
				propagationDelay[j][i] = propagationDelay[i][j];
				
				this.maxPropagationDelay += propagationDelay[i][j];
				
				// ************ config bottle neck ************
				if(ni.getType() && nj.getType()) {
					// both fast node
					bottleNeck[i][j] = 5.0;
					bottleNeck[j][i] = 5.0;
				} else {
					// one or both are slow node
					bottleNeck[i][j] = 100.0;
					bottleNeck[j][i] = 100.0;
				}
				
				this.maxBottleNeck += bottleNeck[i][j];
			}
		}
	
		//Assigning mean to generate T_k later for each node from an exponential distribution
		this.cpuPower = new Double[numPeers];
		
		//Assigning mean to generate transaction later for each node from an exponential distribution
		this.txnMean = new Double[numPeers];

		for(int i=0; i<numPeers; i++){
			cpuPower[i] = 1/(10 + randProp.nextDouble()*1);
			
			// deterministic: constant value for more experiments
			txnMean[i] = 1/(50 + randProp.nextDouble()*50);
		}
		
		Logger.getInstance().logRun("configPropagationDelay!!");
	}
	
	private void initBlockAndvent() {
		//Every node here tries to generate a block on the genesis block
		for(int i=0; i<numPeers; i++){
			//*****master node change to make deterministic
			Timestamp nextBlockTime = nextBlockTimestamp(this.startMills, cpuPower[i]);
			
			//register a new block generation event
			Block newBlock = nodeList.get(i).generateBlock(genesisBlock, nextBlockTime, mainDiff, subDiff);
			Event newEvent = Event.generateBlockEvent(newBlock, nextBlockTime, i);
			nodeList.get(i).nextBlockTime = nextBlockTime;
			pendingEvents.add(newEvent);
		}
	
		//To generate initial set of transactions to start the simulator
		for(int i=0; i<numPeers; i++) {
			generateNextTranscation(nodeList.get(i), new Timestamp(this.startMills));
		}
		
		Logger.getInstance().logRun("initBlockAndvent!!");
	}
	
	private void executeEvent() {
		
		long currenMills;
		long simlatorMills = 0;
		Event nextEvent = null;
		DataCell cell = null;
		Timestamp nextEventTime = null;
		
		while(runing) {
			
			checkMainNode();
			
			if(this.getMainNode().isCrash()) {
				Logger.getInstance().logNode(this.getMainNode().getUID() + " crashed, blockchain stop ...");
				break;
			}
			
			currenMills = System.currentTimeMillis();
			
			long maxMills = this.startMills + this.runMills;
			if(currenMills > maxMills) {
				break;
			}
			
			cell = dataService.getDataCell(false);
			
			if(cell != null) {
				Timestamp nextTxnTime = nextDataCellTransferTimestamp(currenMills, 0, randInt(numPeers, 0));
				Event newEvent = Event.generateDataCellEvent(cell, nextTxnTime);
				pendingEvents.add(newEvent);
			}
			
			nextEvent = pendingEvents.peek();
			
			if( null == nextEvent|| nextEvent.getEventTimestamp().getTime() > currenMills) {
				try {
					Thread.sleep(50);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				
				continue;
			}
			
			nextEvent = pendingEvents.poll();
			finishedEvents.add(nextEvent);
			nextEventTime = nextEvent.getEventTimestamp();
			simlatorMills = nextEventTime.getTime();
			
			Logger.getInstance().logEvent(nextEvent.getEventName() + ": " + nextEventTime);
			
			if(nextEvent.getEventType() == Event.RECEIVE_BLOCK_EVENT) {
				hanldeReceiveBlockEvent(nextEvent);
			} else if(nextEvent.getEventType() == Event.GENERATE_BLOCK_EVENT) {
				hanldeGenerateBlockEvent(nextEvent);
			} else if(nextEvent.getEventType() == Event.RECEIVE_DATA_CELL_EVENT) {
				hanldeReceiveDataCellEvent(nextEvent);
			} else if(nextEvent.getEventType() == Event.GENERATE_DATA_CELL_EVENT){
				hanldeGenerateDataCellEvent(nextEvent);
			} else {
				Logger.getInstance().logError("Error: Wrong Eventtype Detected.");
			}
			
		}//All types of events execution finished here
		
		long actualMills = System.currentTimeMillis() - startMills;
		
		Logger.getInstance().logRun("run end at: " + new Timestamp(System.currentTimeMillis()) + ", total time: " + actualMills);
		Logger.getInstance().logRun("Simulator mills: "+ simlatorMills);
		Logger.getInstance().logRun("total Block: " +  totalCnt + ", stale block: "+ staleCnt + ", sucess block: "+ blockCnt+", Transaction Count: " + txnCnt);
		Logger.getInstance().logRun("Generate Block: " + (this.runMills*1.0f/blockCnt) + " ms, Generate Transaction : " + simlatorMills*1.0f/txnCnt + " ms");
	}
	
	private void checkMainNode() {
		
		Node mainNode = this.nodeList.get(mainNodeId);
		
		if(mainNode.isCrash() && this.recoverWhenCrash) {
			this.mainNodeId = randInt(this.nodeList.size(), mainNodeId);
			Logger.getInstance().logNode("mainNode " + mainNode.getUID() + " crashed, select " + this.nodeList.get(mainNodeId).getUID() + " as mainNode!");
			Logger.getInstance().logNode(mainNode.getUID() + " recover ...");
			mainNode.recover();
		}
	}
	
	private void hanldeReceiveBlockEvent(Event event) {
		//Code to execute receive Block event
		int currentNum = event.getReceiverNodeId();
		Node currentNode = nodeList.get(currentNum);

		Block currentBlock = event.getEventBlock();
		String currentBlockID = currentBlock.getBlockID();
		
		if(currentNode.checkForwarded(currentBlockID)) {
			return;
		} else {
			currentNode.addForwarded(currentBlockID);
		}
		
		Logger.getInstance().logBlockMine("\n" + currentNode.getUID() + " recev block !");
		if(currentNode.addBlock(currentBlock)) {
			Logger.getInstance().logBlockMine(currentNode.getUID() + " addPending Blocks!");
			//check if any pending blocks can be added
			currentNode.addPendingBlocks();
		}
		
		Timestamp eventTime = event.getEventTimestamp();
							
		if(currentBlock.getDepth() > currentNode.probParentBlock.getDepth()) {
			Logger.getInstance().logBlockHistory(currentNode.getUID() + " depth " + currentNode.probParentBlock.getDepth() + " to " + currentBlock.getDepth());
			
			//updating the probable parent block
			currentNode.probParentBlock = currentBlock;
			currentNode.calculateWeight();
			
			//to Generate the next block for the sending node
			Timestamp newBlockTime = nextBlockTimestamp(eventTime.getTime(), cpuPower[currentNum]);
			currentNode.nextBlockTime = newBlockTime;
			Block newBlock = currentNode.generateBlock(currentBlock, newBlockTime, mainDiff, subDiff);
			Event newEvent = Event.generateBlockEvent(newBlock, newBlockTime, currentNum);
			pendingEvents.add(newEvent);			
		}
		
		// transfer the block to other peers nodes
		transferBlock2Peers(currentNum, currentBlock, eventTime);
		
		//Timestamp of the next event to be executed
		Logger.getInstance().logBlockHistory("Block "+currentNum+" received "+currentBlockID+" at depth "+ currentBlock.getDepth());
	}
	
	private void hanldeGenerateBlockEvent(Event nextEvent) {
		//Code to execute generate Block
		int creatorNum = nextEvent.getCreatorNodeId();
		Node currentNode = nodeList.get(creatorNum);
		Block currentBlock = nextEvent.getEventBlock();
		Timestamp nextBlockTime = currentNode.nextBlockTime;
		
		Timestamp nextEventTime = nextEvent.getEventTimestamp();
		
		if (!( nextBlockTime.after(nextEventTime) || nextBlockTime.before(nextEventTime) )) { 
			// Only execute this if the node still decides to execute it
			// adding pending transaction to the new block
			currentNode.addPendingTx2NewBlock(currentBlock);
			 
			 //finally adding the block in the current node
			Logger.getInstance().logBlockMine("\n\n------------ " + currentNode.getUID()+ " start Mining ~ ~");
			boolean addBlockSuccess = currentNode.addBlock(currentBlock);
			
			//adding a status message to keep track that the newly created block is going to be added in current node 
			currentNode.addForwarded(currentBlock.getBlockID());
			currentNode.probParentBlock = currentBlock;
			currentNode.calculateWeight();
			currentBlock.verifyHash();
			
			totalCnt++;
			if(addBlockSuccess) { //if adding block in current node is successful, transfer the block to other peers nodes
				blockCnt += 1;
				Logger.getInstance().logBlockCreate("Block created "+currentBlock.getBlockID()+" by "+ creatorNum+" Time: " + currentBlock.getCreationTime());
				String intervalTime = String.valueOf(currentBlock.getCreationTime().getTime() - currentBlock.getParentBlock().getCreationTime().getTime());
				Logger.getInstance().logBlockHistory("Node "+creatorNum+" created Block "+currentBlock.getBlockID()+ " at Depth "+ currentBlock.getDepth() + " ON "+currentBlock.getParentBlockID()+" add interval:"+ intervalTime);
				
				transferBlock2Peers(creatorNum, currentBlock, nextEventTime);
				
				// generate block success, reward 
				Timestamp nextTxnTime = nextDataCellTransferTimestamp(currentBlock.getCreationTime().getTime(), 0, randInt(numPeers, 0));
				Transaction mfee = new Transaction(currentNode.getUID()+"_mining_fee", Constants.GOD_ID, currentNode.getUID(), 50, nextTxnTime);
				Event newEvent = Event.generateDataCellEvent(mfee, nextTxnTime);
				pendingEvents.add(newEvent);
			} else {
				staleCnt++; // stale block
			}
			
			//to Generate the next block for the sending node
			Timestamp newBlockTime = nextBlockTimestamp(nextEventTime.getTime(), cpuPower[creatorNum]);

			Block newBlock =currentNode.generateBlock(currentBlock, newBlockTime, mainDiff, subDiff);
			
			//Generate new block by the sending node for the purpose of next transaction
			Event newEvent = Event.generateBlockEvent(newBlock, newBlockTime, creatorNum);
			pendingEvents.add(newEvent);
		}
	}
	
	private void hanldeReceiveDataCellEvent(Event nextEvent) {
		// Code to execute receive Transaction
		int receiverNum = nextEvent.getReceiverNodeId();
		int senderNum = nextEvent.getSenderNodeId();
		
		Node currentNode = nodeList.get(receiverNum);
		DataCell newTxn = nextEvent.getEventDataCell();
		
		String newTxnID = newTxn.getDtuId();
		
		Timestamp nextEventTime = nextEvent.getEventTimestamp();
		
		// Only execute if it has not already forwarded the same transaction earlier
		if(!(currentNode.checkForwarded(newTxnID))) { 
			currentNode.addForwarded(newTxnID);
			boolean addSuccess = currentNode.addDataCell(newTxn);

			if(addSuccess) {
				Logger.getInstance().logTxReceive("Node." + receiverNum + ", Receive DataCell "+ newTxnID+" "+newTxn.getSenderID()+" "+"send "+ newTxn.getReceiverID() + " $" + newTxn.getWeight());
			}
			
			// transfer the new transcation to other peers nodes
			transferTranscation2Peers(receiverNum, senderNum, newTxn, nextEventTime);
		}
	}
	
	private void hanldeGenerateDataCellEvent(Event nextEvent) {
		//Code to handle generate Transaction event
		txnCnt += 1;
		DataCell cell = nextEvent.getEventDataCell();
		String senderID = cell.getSenderID();
		
		int senderNum = 0;
		float newAmount = 0;
		Node currNode = null;
		boolean isTranscation = false;
		
		switch(cell.type()) {
		case Blank:
		case Message:
		case Jpeg:
			isTranscation = false;
			break;
		case Transaction:
			isTranscation = true;
			break;
		}
		
		if(isTranscation == false || senderID.equals(Constants.GOD_ID)) {
			senderNum = new Random(System.nanoTime()).nextInt(numPeers);
			newAmount = cell.getWeight();
			currNode = nodeList.get(senderNum);
		} else {
			senderNum = Integer.parseInt(senderID.split("_")[1]);
			currNode = nodeList.get(senderNum);
			
			Random updateRand = new Random(System.nanoTime());
			newAmount = updateRand.nextFloat()*currNode.getCurrOwned();
			cell.setWeight(newAmount);
		}
		
		//random to generate an amount for the transaction
		currNode.addForwarded(cell.getDtuId());
		
		Timestamp nextEventTime = nextEvent.getEventTimestamp();
		
		//Adding the transaction at the sender end.
		if(currNode.addDataCell(cell)) {			//proceeding only when the transaction is successfully added
			if (newAmount!=0){
				Logger.getInstance().logTxReceive(senderID + " sends money: " + cell.getWeight()+ " to " + cell.getReceiverID()+" , owned money now: "+ nodeList.get(senderNum).getCurrOwned());
				// transfer the new transcation to other peers nodes
				transferTranscation2Peers(senderNum, -1, cell, nextEventTime);
			}
			//generateNextTranscation(currNode, nextEventTime);
		}
	}
	
	private void generateNextTranscation(Node node, Timestamp txnTime) {
		int senderNum = node.getId();
		
		//to Generate the next transaction for the sending node
		Timestamp nextTxnTime = nextDataCellGenerateTimestamp(txnTime.getTime(), txnMean[senderNum]);
		node.nextTxnTime = nextTxnTime;
		
		int rcvNum = randInt(numPeers, senderNum);
		String receiverID = nodeList.get(rcvNum).getUID();

		Transaction newTransaction = node.generateTxn(receiverID, 0, nextTxnTime);
		Event newEvent = Event.generateDataCellEvent(newTransaction, nextTxnTime);
		pendingEvents.add(newEvent);
		
		Logger.getInstance().logTxCreate("Transcation created "+newTransaction.getDtuId()+" by Node."+ senderNum + " Time:"+newTransaction.getDtuTime() );
	}
	
	// transfer the new block to other peers nodes 
	private void transferBlock2Peers(int senderIdx, Block newBlock, Timestamp nextEventTime) {
		Node currentNode = nodeList.get(senderIdx);
		
		for(int i=0; i<numPeers; i++){
			Node nextNode = currentNode.connectedNodeAt(i);
			if(nextNode == null){
				break;
			}
			int nextNodeIdx = nextNode.getId();
			Timestamp receiveTime = nextBlockTransferTimestamp(nextEventTime.getTime(), senderIdx, nextNodeIdx);
			Event newEvent = Event.receiveBlockEvent(newBlock, receiveTime, nextNodeIdx, senderIdx);
			pendingEvents.add(newEvent);
		}
	}
	
	// transfer the new transcation to other peers nodes 
	private void transferTranscation2Peers(int senderIdx, int superIdx, DataCell cell, Timestamp nextEventTime) {
		Node currentNode = nodeList.get(senderIdx);
		for(int i=0; i<numPeers; i++){
			Node nextNode = currentNode.connectedNodeAt(i);							
			if(nextNode == null){
				break;
			} 	
			int nextNodeNum = nextNode.getId();
			if (nextNodeNum != superIdx) {
				Timestamp receiveTime = nextDataCellTransferTimestamp(nextEventTime.getTime(), senderIdx, nextNodeNum);
				Event newEvent = Event.receiveDataCellEvent(cell, receiveTime, nextNodeNum, senderIdx);
				pendingEvents.add(newEvent);
			}
		}
	}
	
	private void writeHistory() {
		double sum = 0;
		for(int i=0; i<numPeers; i++){ 
			float value = nodeList.get(i).getCurrOwned();
			sum = sum + value;
			Logger.getInstance().logTxReceive(nodeList.get(i).getUID() + " " + value); //storing all the transactions by all peers
		}
		
		Logger.getInstance().logTxReceive("Total money :"+sum); // total transaction
		
		String logRoot = Logger.getInstance().getLogPath();
	
		for(int i=0; i<numPeers; i++){ // Note: all node's block-chain is stored in node.blockchain arraylist
			HashMap<String, Block> tempBlockChain = nodeList.get(i).blockMap; //getting each node's chain
			String root = "genesis";
			String fileName = logRoot + "/file_"+i+".txt";
			try{
				PrintWriter writer = new PrintWriter(fileName,"UTF-8");
				writer.println("Node "+i+", Details:");
				writer.println("Type : "+(nodeTypes[i]?"fast":"slow"));
				writer.println("CPU power : "+cpuPower[i]);
				writer.println("Connected to :");
				for(int j=0; j<numPeers; j++){
					if(connectionArray[i][j]){
						writer.println("NID: "+ nodeList.get(j).getUID() +", PD: "+ propagationDelay[i][j] +", BS: "+ bottleNeck[i][j]);
					}
				}
				writer.println("\nStored Tree:");
	
				printTree(writer ,root, tempBlockChain);
				writer.close();
			}
			catch (IOException e){
				e.printStackTrace();
			}			
		}
	}
	
	public Timestamp nextBlockTimestamp(long baseTime, double mean) {
		double nextTimeOffset = randFloat();
		double nextTimeGap = -1*Math.log(nextTimeOffset)/mean;
		return new Timestamp(baseTime + this.blockIntervalMills + (int)(nextTimeGap % 10 - 5 ) * 1000);
	}
	
	public Timestamp nextDataCellGenerateTimestamp(long baseTime, double mean) {
		double nextTimeOffset = randFloat();
		double nextTimeGap = -1*Math.log(nextTimeOffset)/mean;
		nextTimeGap = 60 * 1000/6 + (int)(nextTimeGap % 10 - 5);
		
		return new Timestamp(baseTime + (long)nextTimeGap);
	}
	
	public Timestamp nextBlockTransferTimestamp(long baseTime, int senderIdx, int nextNodeIdx) {
		long transferDelay = transferMillis(senderIdx, nextNodeIdx);
		long msgDelay = Math.round(1000.0/bottleNeck(senderIdx, nextNodeIdx));
		return new Timestamp(baseTime+ transferDelay + msgDelay);
	}
	
	public Timestamp nextDataCellTransferTimestamp(long baseTime, int senderIdx, int nextNodeIdx) {
		return new Timestamp(baseTime + transferMillis(senderIdx, nextNodeIdx));
	}
	
	private double bottleNeck(int i, int j) {
		if(connectionArray[i][j] && bottleNeck[i][j] != null){
			return bottleNeck[i][j];
		}
		
		// if Node.i not connected to Node.j, bottleNeck would be the max
		return this.maxBottleNeck;
	}
	
	private double propagationDelay(int i, int j) {
		if(connectionArray[i][j] && propagationDelay[i][j] != null){
			return propagationDelay[i][j];
		}
		
		// if Node.i not connected to Node.j, propagation delay would be the max
		return this.maxPropagationDelay;
	}
	
	private long transferMillis(int senderIdx, int nextNodeIdx) {
		double qDelayP1 = randFloat();
		long qDelay = (long)((-1*Math.log(qDelayP1)*bottleNeck(senderIdx, nextNodeIdx))/qDelayParameter);
		long pDelay = Math.round(propagationDelay(senderIdx, nextNodeIdx));
	
		return (qDelay + pDelay);
	}
	
	public float randFloat() {
		Random randNext = new Random(System.nanoTime());
		float nextValue = randNext.nextFloat();
		while (nextValue == 0.0){
			nextValue = randNext.nextFloat();
		}
		return nextValue;
	}
	
	public int randInt(int base, int except) {
		if(base <= 1) {
			return 0;
		}
		Random receiveRand = new Random(System.nanoTime());
		int nextValue = receiveRand.nextInt(base);
		while(nextValue == except){
			nextValue = receiveRand.nextInt(base);
		}
		return nextValue;
	}
	
	public static void printTree(PrintWriter writer,String root, HashMap<String, Block> blockChain){		
		Block rootBlock = blockChain.get(root);
		if(rootBlock != null){
			ArrayList<String> childList = blockChain.get(root).getChildList();
			int childListSize = childList.size();
			int i = 0;
			while(i<childListSize){
				String newRoot = childList.get(i);
				printTree(writer, newRoot, blockChain);
				i++;
			}
			Block parent = blockChain.get(root).getParentBlock();
			if(parent != null){
				writer.println(root+","+parent.getBlockID());
			}					
		}
	}
}


