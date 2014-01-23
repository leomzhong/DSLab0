
public class Node {
	private String name;
	private String ip;
	private int port;

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getIp() {
		return ip;
	}
	public void setIp(String ip) {
		this.ip = ip;
	}
	public int getPort() {
		return port;
	}
	public void setPort(int port) {
		this.port = port;
	}
	
	public String toString() {
		StringBuilder result = new StringBuilder();
		result.append("node: [name:" + this.name + "; ip:" + this.ip + "; port:" + this.port + "]");
		return result.toString();
	}
}
