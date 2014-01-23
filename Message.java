import java.io.Serializable;

public class Message implements Serializable {
	public static final long serialVersionUID = 1l;

	private String source;
	private String dest;
	private int seqNum;
	private String kind;
	private boolean duplicate;
	private Object payload;

	public Message(String dest, String kind, Object data) {
		this.dest = dest;
		this.kind = kind;
		this.payload = data;
		this.duplicate = false;
	}

	/*
	 * Setter and getter for all the field
	 */
	public String get_source() {
		return source;
	}

	public void set_source(String source) {
		this.source = source;
	}

	public String get_dest() {
		return dest;
	}

	public void set_dest(String dest) {
		this.dest = dest;
	}

	public int get_seqNum() {
		return seqNum;
	}

	public void set_seqNum(int seqNum) {
		this.seqNum = seqNum;
	}

	public String get_kind() {
		return kind;
	}

	public void set_kind(String kind) {
		this.kind = kind;
	}

	public boolean get_duplicate() {
		return duplicate;
	}

	public void set_duplicate(boolean duplicate) {
		this.duplicate = duplicate;
	}

	public Object get_payload() {
		return payload;
	}

	public void set_payload(Object payload) {
		this.payload = payload;
	}

	public Message clone() {
		Message result = new Message(this.get_dest(), this.get_kind(),
				this.get_payload());
		result.set_source(this.get_source());
		result.set_seqNum(this.get_seqNum());
		result.set_duplicate(this.get_duplicate());
		return result;
	}

	public String toString() {
		StringBuilder result = new StringBuilder();
		result.append("source:" + this.source);
		result.append(" dest:" + this.dest);
		result.append(" seqNum:" + this.seqNum);
		result.append(" kind:" + this.kind);
		result.append(" duplicate:");
		if (duplicate)
			result.append("yes");
		else
			result.append("no");
		result.append("\npayload:" + this.payload.toString());
		return result.toString();
	}
}
