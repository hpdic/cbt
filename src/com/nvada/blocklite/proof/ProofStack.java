package com.nvada.blocklite.proof;


import java.util.ArrayList;

import com.nvada.blocklite.Block;
import com.nvada.blocklite.data.DataCell;
import com.nvada.blocklite.log.Logger;

public class ProofStack extends ProofBase {
	
	public ProofStack(int mDiff, int sDiff) {
		this.nonce = 1;
		this.mainDiff = mDiff;
		this.subDiff = sDiff;
	}
	
	//Increases nonce value until hash target is reached.
	public String generateProof(Block block) {
		
		String uBlokckID = block.getBlockID();
		
		float weights = block.getOwnerWeight();
		
		if(weights < 0) {
			Logger.getInstance().logError("tune invalid weight: " + weights + " to 0");
			weights = 0;
		}
		
		float target = (weights + 1.0f) / this.difficulty() * 1000;
		// System.out.println("\ntarget: " + target);
			
		
		if(hashMatchTarget(uBlokckID, target)) {
			Logger.getInstance().logBlockMine("Block Added!!! : millis = " + this.nonce + " ms, hash = "  + uBlokckID);
		} else {
			while(false == hashMatchTarget(uBlokckID, target) ) {
				// System.out.println(" " + target + " x " + this.nonce + " = " + target * this.nonce);
				this.nonce++;
				uBlokckID = calculateHash(block);
			}
			blockCount += 1;
			
			ArrayList<DataCell> cells = block.getCells();
			Logger.getInstance().logBlockMine("Block Mined!!! : time = " +this.nonce + " ms, diffcult " + mainDiff + "." + subDiff + ", hash = "  + uBlokckID + ", cell counts = " + cells.size() + ", block NO." + blockCount);
		}
		
		return uBlokckID;
	}
}
