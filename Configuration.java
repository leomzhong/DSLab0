import java.io.File;
import java.io.FileInputStream;
import java.util.List;

import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

// Implement this
public class Configuration {
	/*
	 * My idea is that:
	 * 
	 * 1. Should have a HashMap<String, Node> to find the node from its name; 2.
	 * Should have a list for send rules and a list for receive rules
	 */
	public List<Node> configuration;
	public List<Rule> sendRules;
	public List<Rule> receiveRules;

	public Configuration() {
	}

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

	public List<Rule> getSendRules() {
		return this.sendRules;
	}

	public List<Rule> getReceiveRules() {
		return this.receiveRules;
	}

	public void updateSendRules(String fileName) {
		Configuration newConfig = Configuration.loadConfigFile(fileName);
		if (newConfig != null) {
			this.sendRules = newConfig.getSendRules();
		}
	}

	public void updateReceiveRules(String fileName) {
		Configuration newConfig = Configuration.loadConfigFile(fileName);
		if (newConfig != null) {
			this.receiveRules = newConfig.getReceiveRules();
		}
	}

	public Node getNode(String nodeName) {
		if (nodeName == null)
			return null;
		for (Node node : configuration) {
			if (nodeName.equals(node.getName()))
				return node;
		}
		return null;
	}

	public int getNodePort(String nodeName) {
		Node targetNode = getNode(nodeName);
		if (targetNode == null)
			return 0;
		return targetNode.getPort();
	}

	public Rule matchSendRule(Message message) {
		return matchRule(message, true);
	}

	public Rule matchReceiveRule(Message message) {
		return matchRule(message, false);
	}

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
