package com.nvada.blocklite.proof;

import java.util.ArrayList;

import com.nvada.blocklite.Block;
import com.nvada.blocklite.data.DataCell;
import com.nvada.blocklite.log.Logger;
import com.nvada.blocklite.utils.StringUtil;

public class ProofWork extends ProofBase {
	
	public ProofWork(int mDiff, int sDiff) {
		this.mainDiff = mDiff;
		this.subDiff = sDiff;
	}

	//Increases nonce value until hash target is reached.
	public String generateProof(Block block) {
		
		String uBlokckID = block.getBlockID();
		
		// Create a string with difficulty * "0"
		String target = StringUtil.getDificultyString(mainDiff);
		
		if(hashMatchTarget(uBlokckID, target) ) {
			Logger.getInstance().logBlockMine("Block Added!!! : nonce = " + nonce + ", hash = "  + uBlokckID);
		} else {
			
			while(!hashMatchTarget(uBlokckID, target)) {
				nonce ++;
				uBlokckID = calculateHash(block);
			}
			
			blockCount += 1;
			
			ArrayList<DataCell> cells = block.getCells();
			Logger.getInstance().logBlockMine("Block Mined!!! : nonce = " + nonce + ", diffcult " + mainDiff + "." + subDiff + ", hash = "  + uBlokckID + ", cell counts = " + cells.size() + ", block NO." + blockCount);
		}
		
		return uBlokckID;
	}
}
