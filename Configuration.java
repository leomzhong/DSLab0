
// Implement this
public class Configuration {
	/*
	 * My idea is that:
	 * 
	 * 1. Should have a HashMap<String, Node> to find the node from its name;
	 * 2. Should have a list for send rules and a list for receive rules
	 */

	// Implement this
	public static Configuration loadConfigFile(String filename) {
		return null;
	}
	
	// Implement this
	public void updateSendRules() {
		
	}
	
	// Implement this
	public void updateReceiveRules() {
		
	}
	
	// Implement this
	public Node getNode(String nodeName) {
		return null;
	}
	
	// Implement this
	public int getNodePort(String nodeName) {
		return 0;
	}
	
	public Rule matchSendRule(Message message) {
		return matchRule(message, true);
	}
	
	public Rule matchReceiveRule(Message message) {
		return matchRule(message, false);
	}
	
	// Implement this
	// go through the send rule list if the send set to true, otherwise go through receive rule
	private Rule matchRule(Message message, boolean send) {
		return null;
	}
}
