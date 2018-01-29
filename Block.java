
public class Block {
	/**
	 * Demonstration of Java's 2's complement byte representation
	 * 
	 * @author Lynn Marshall 
	 * @version (1.00 May 10th, 2015)
	 */

	
	    // block number
	    private byte blockNoPart;
	    byte getBlockNum() {
	    	return blockNoPart;
	    }
	    /**
	     * Create part of a block number
	     */
	    public Block()
	    {
	        blockNoPart = 0;
	    }

	    /**
	     * Set and print a byte representing part of a block number
	     * to show that Java uses twos complement representation.
	     * Using Byte's toUnsignedInt is helpful to get the representation
	     * that we want.
	     */
	    public void setBlockNoPart(int value)
	    {
	        blockNoPart = (byte)value;
	        // probably not what you want
	        System.out.println("Stored value: " + blockNoPart);
	        
	        // need Java 1.8 for this to work
	        Byte b = new Byte(blockNoPart);
	        System.out.println("Convert to unsigned: " + Byte.toUnsignedInt(b));
	        
	        // otherwise could do something like this
	        int value2 = (int)blockNoPart;
	        if (value2<0)
	            value2+=256;
	        System.out.println("byte as int corrected: " + value2);
	        
	        // of course we want to have two bytes...
	        int b1 = value / 256;
	        int b2 = value % 256;
	        //CHanged b1 and b2 is this correct? 
	        System.out.println("Split into 2 parts (ints): " + b1 + " " + b2);
	    }
	}

	
	


