package com.nvada.blocklite.proof;

import com.nvada.blocklite.Block;

public interface Proofable {
	
	public boolean verifyProof(Block block);

	public String generateProof(Block block);
	
	public String calculateHash(Block block);
}
