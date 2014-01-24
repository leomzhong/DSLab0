import java.io.File;
import java.io.FileInputStream;
import java.util.List;

import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

/**
 * Configuration class
 * 
 * @author Ming Zhong
 * @author Pratik Shah
 */
public class Configuration {

	// TODO: Making these private
	public List<Node> configuration;
	public List<Rule> sendRules;
	public List<Rule> receiveRules;
	
	private long lastModifiedTime;

	public Configuration() {
		this.lastModifiedTime = 0;
	}

	/**
	 * Load the configuration file to the Configuration object
	 * 
	 * @param fileName
	 *            configuration file name
	 * @return Configuration object representing the configuration file
	 */
	public static Configuration loadConfigFile(String fileName) {
		Yaml test = new Yaml(new Constructor(Configuration.class));
		FileInputStream stream = null;
		try {
			stream = new FileInputStream(new File(fileName));
		} catch (Exception ex) {
			ex.printStackTrace();
			System.out.println(ex.getMessage());
			return null;
		}
		Configuration config = (Configuration) test.load(stream);
		return config;
	}

	/**
	 * Get the send rules list
	 * 
	 * @return The send rules in the Configuration object
	 */
	public List<Rule> getSendRules() {
		return this.sendRules;
	}

	/**
	 * Get the receive rules list
	 * 
	 * @return The receive rules in the Configuration object
	 */
	public List<Rule> getReceiveRules() {
		return this.receiveRules;
	}

	/**
	 * Update the send rules list
	 * 
	 * @param fileName
	 *            configuration file name
	 */
	public void updateSendRules(String fileName) {
		File file = null;
		try {
			file = new File(fileName);
			if (lastModifiedTime != 0 && lastModifiedTime == file.lastModified())
				return;
		} catch (Exception ex) {
			Log.error("updateSendRules", "The file does not exist", ex);
			return;
		}
		lastModifiedTime = file.lastModified();
		Configuration newConfig = Configuration.loadConfigFile(fileName);
		if (newConfig != null) {
			this.sendRules = newConfig.getSendRules();
		}
	}

	/**
	 * Update the receive rules list
	 * 
	 * @param fileName
	 *            configuration file name
	 */
	public void updateReceiveRules(String fileName) {
		File file = null;
		try {
			file = new File(fileName);
			if (lastModifiedTime != 0 && lastModifiedTime == file.lastModified()) {
				return;
			}
		} catch (Exception ex) {
			Log.error("updateReceiveRules", "The file does not exist", ex);
			return;
		}
		lastModifiedTime = file.lastModified();
		Configuration newConfig = Configuration.loadConfigFile(fileName);
		if (newConfig != null) {
			this.receiveRules = newConfig.getReceiveRules();
		}
	}

	/**
	 * Get the node from its name
	 * 
	 * @param nodeName
	 *            name of the node we want to retrieve
	 * @return null if the node does not exist
	 */
	public Node getNode(String nodeName) {
		if (nodeName == null)
			return null;
		for (Node node : configuration) {
			if (nodeName.equals(node.getName()))
				return node;
		}
		return null;
	}

	/**
	 * Get the server port of the node
	 * 
	 * @param nodeName
	 *            name of the node
	 * @return 0 if the node does not exist;
	 */
	public int getNodePort(String nodeName) {
		Node targetNode = getNode(nodeName);
		if (targetNode == null)
			return -1;
		return targetNode.getPort();
	}

	/**
	 * Match the message against send rules
	 * 
	 * @param message
	 *            The message to match
	 * @return true on match; otherwise false
	 */
	public Rule matchSendRule(Message message) {
		return matchRule(message, true);
	}

	/**
	 * Match the message against receive rules
	 * 
	 * @param message
	 *            The message to match
	 * @return true on match; otherwise false
	 */
	public Rule matchReceiveRule(Message message) {
		return matchRule(message, false);
	}

	/**
	 * Helper function to match a message to a list of rules
	 * 
	 * @param message
	 *            The message to match
	 * @param send
	 *            whether the message should be matched against send rules
	 * @return null on not matched
	 */
	private Rule matchRule(Message message, boolean send) {
		List<Rule> list = this.sendRules;
		if (!send) {
			list = this.receiveRules;
		}
		for (Rule rule : list) {
			if (rule.match(message))
				return rule;
		}
		return null;
	}

	public String toString() {
		StringBuilder result = new StringBuilder();
		for (Node node : this.configuration) {
			result.append(node.toString() + "\n");
		}

		result.append("Send Rules:\n");
		for (Rule rule : this.sendRules) {
			result.append(rule.toString() + "\n");
		}

		result.append("Receive Rules:\n");
		for (Rule rule : this.receiveRules) {
			result.append(rule.toString() + "\n");
		}

		return result.toString();
	}
}
