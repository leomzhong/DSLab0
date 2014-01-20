
// Implement this
public class Rule {
	
	public static final int DROP_ACTION = 0x01;
	public static final int DUPLICATE_ACTION = 0x01 << 1;
	public static final int DELAY_ACTION = 0x01 << 2;
	
	// Implement this
	public int getAction() {
		return -1;
	}

}
