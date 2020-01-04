package com.nvada.blocklite.proof;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import com.nvada.blocklite.Block;
import com.nvada.blocklite.utils.StringUtil;

public abstract class ProofBase implements Proofable {
	
	protected int nonce = 0;
	protected int mainDiff = 5;
	protected int subDiff = 0;
	
	protected static int blockCount = 0;
	
	public int getNonce() {
		return nonce;
	}
	
	public int getMainDiff() {
		return mainDiff;
	}

	public void setMainDiff(int mainDiff) {
		this.mainDiff = mainDiff;
	}

	public int getSubDiff() {
		return subDiff;
	}

	public void setSubDiff(int subDiff) {
		this.subDiff = subDiff;
	}
	
	public int difficulty() {
		int base = 1;
		
		for(int i=0; i<mainDiff; i++) {
			base = base * 10;
		}
		
		return (int)((mainDiff + subDiff * 0.1) * base);
	}
	
	//Calculate new hash based on blocks contents
	@Override
	public String calculateHash(Block block) {
        String calculatedhash = StringUtil.applySha256( block.getCreatorID() + block.getParentBlockID() + block.getCreationTime().getTime() + Integer.toString(nonce) );
		return calculatedhash;
	}
	
	@Override
	public boolean verifyProof(Block block) {
		String uBlokckID = block.getBlockID();
		String hash = calculateHash(block);
		return uBlokckID.equals(hash);
	}
	
	public int countOfZero(String str) {
		int count = 0;
		if(str != null) {
			char[] chars = str.toCharArray();
			for(char c : chars) {
				if(c == '0') {
					count++;
				}
			}
		}
		return count;
	}
	
	protected int hashValue(String hash) {
		if(hash == null || hash.length()<1) {
			return 0;
		}
		
		int i = 0;
		int digit;
		int result = 0;
		int max = hash.length();
		
		while (i < max) {
			digit = Character.digit(hash.charAt(i++), 16);
			
			if (digit >= 0) {
				result += digit;
			} else {
				result += 0xf;
			}
		}
		
		return result;
	}
	
	protected boolean hashMatchTarget(String hash, String target) {
		
		if(hash.substring( 0, mainDiff).equals(target) && countOfZero(hash) >= (mainDiff + subDiff) )  {
			return true;
		}
		
		return false;
	}

	protected boolean hashMatchTarget(String hash,float target) {
		return (hashValue(hash) < target * this.nonce);
	}
	
	public String formatStamp(Timestamp stamp) {
		DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(stamp);
	}
	
	// adjust difficulty
	public int proper_difficulty(Block block) {
		Block prev = block.getParentBlock();
        if (prev == null) {
            return block.getProof().difficulty();
        }
        
        Block prev_prev = prev.getParentBlock();;
        if (prev_prev == null) {
            return prev.getProof().difficulty();
        }
        
        // ten minute
        int TargetSpacing = 10 * 60;
        
        // one week
        int TargetTimespan = 7 * 24 * 60 * 60;
        int Interval = TargetTimespan / TargetSpacing;
        int ActualSpacing = (int)(prev.getCreationTime().getTime() - prev_prev.getCreationTime().getTime())/1000;
        
        // difficulty = prev_difficulty x (1007x10x60 + 2 x prev_Interval) / (1009x10x60)
        // if prev_Interval > 10 * 60, then increase difficulty
        // if prev_Interval < 10 * 60, then decrease difficulty
    
        return prev.getProof().difficulty() * ((Interval - 1) * TargetSpacing + 2 * ActualSpacing) / ((TargetSpacing + 1) * TargetSpacing);
    }
}
