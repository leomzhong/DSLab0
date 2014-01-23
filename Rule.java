/**
 * Class representing the rule in the configuration file
 * 
 * @author Ming Zhong
 * @author Pratik Shah
 * 
 */
public class Rule {

	public static final int DROP_ACTION = 0x01;
	public static final int DUPLICATE_ACTION = 0x01 << 1;
	public static final int DELAY_ACTION = 0x01 << 2;

	public static final String DROP_STRING = "drop";
	public static final String DUPLICATE_STRING = "duplicate";
	public static final String DELAY_STRING = "delay";

	public String action;
	public String src;
	public String dest;
	public String kind;
	public int seqNum;
	public String duplicate;

	public Rule() {
	}

	/**
	 * Match a message against this rule
	 * 
	 * @param message
	 *            The message to match
	 * @return
	 */
	public boolean match(Message message) {
		if (src != null) {
			if (!src.equals(message.get_source()))
				return false;
		}
		if (dest != null) {
			if (!dest.equals(message.get_dest()))
				return false;
		}
		if (kind != null) {
			if (!kind.equals(message.get_kind()))
				return false;
		}
		if (seqNum != 0) {
			if (seqNum != message.get_seqNum())
				return false;
		}
		if (duplicate != null) {
			if (duplicate.equals("yes") && (!message.get_duplicate()))
				return false;
			if (duplicate.equals("no") && message.get_duplicate())
				return false;
		}
		return true;
	}

	/**
	 * Get the action of this rule
	 * 
	 * @return The action code of this rule
	 */
	public int getAction() {
		if (this.action.equals(DROP_STRING))
			return DROP_ACTION;
		else if (this.action.equals(DUPLICATE_STRING))
			return DUPLICATE_ACTION;
		else if (this.action.equals(DELAY_STRING))
			return DELAY_ACTION;
		else
			return -1;
	}

	public String toString() {
		StringBuilder result = new StringBuilder();
		result.append("Rule: [action:" + this.action);
		result.append("; src:" + this.src);
		result.append("; dest:" + this.dest);
		result.append("; kind:" + this.kind);
		result.append(" seqNum:" + this.seqNum);
		result.append(" duplicate:" + this.duplicate + "]");
		return result.toString();
	}
}
