import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.concurrent.locks.ReentrantLock;

public class MessagePasser {

	private String configFileName;
	private String localName;

	private Configuration config;

	private ServerSocket listenSocket;

	private LinkedList<Message> sendDelayBuffer;

	private ReentrantLock receiveBufferLock;
	private LinkedList<Message> receiveDelayBuffer;
	private LinkedList<Message> receiveBuffer;

	private HashMap<String, Socket> socketMap;

	private int seqNum;

	public MessagePasser(String configuration_filename, String local_name) {
		this.configFileName = configuration_filename;
		this.localName = local_name;
		this.config = Configuration.loadConfigFile(this.configFileName);
		this.sendDelayBuffer = new LinkedList<Message>();
		this.receiveBufferLock = new ReentrantLock();
		this.receiveDelayBuffer = new LinkedList<Message>();
		this.receiveBuffer = new LinkedList<Message>();
		this.socketMap = new HashMap<String, Socket>();
		int port = this.config.getNodePort(this.localName);
		this.listenSocket = NetworkUtil.createServerSocket(port,
				"MessagePasser");
		this.seqNum = 0;
		
		// TODO: Do we need to remember this thread?
		ListenThread listen = new ListenThread();
		Thread newListenThread = new Thread(listen);
		newListenThread.start();
	}

	protected class ListenThread implements Runnable {
		public void run() {
			if (listenSocket == null) {
				return;
			}
			while (true) {
				Socket newSocket = null;
				try {
					newSocket = listenSocket.accept();
				} catch (Exception ex) {
					Log.error("ListenThread", "Cannot accept request", ex);
					continue;
				}

				// TODO: Do we need to remember this?
				ReceiveThread newReceiver = new ReceiveThread(newSocket);
				Thread newThread = new Thread(newReceiver);
				newThread.start();
			}
		}
	}

	protected class ReceiveThread implements Runnable {
		private Socket mySocket;

		public ReceiveThread(Socket socket) {
			this.mySocket = socket;
		}

		public void run() {
			while (true) {
				Message newMessage = NetworkUtil.receiveMessage(mySocket,
						"ReceiveThread");
				if (newMessage == null)
					continue;
				/*
				 * Logic here: First update the Receive Rule. Then check the
				 * message against the rule 1. If it need to be delayed, get the
				 * receiveBufferLock and put it in 2. If it need to be dropped,
				 * drop it 3. If it need to duplicated, grap the
				 * receiveBufferLock and duplicate it (how to deal with the
				 * duplicate field in the message?) 4. If not matched, grap the
				 * receiveBufferLock and put it in, then add all message in
				 * receiveDelayBuffer.
				 */

				// TODO: Update RECEIVE RULE from config file
				Rule rule = config.matchReceiveRule(newMessage);
				if (rule == null) {
					receiveBufferLock.lock();
					receiveBuffer.add(newMessage);
					while (receiveDelayBuffer.size() != 0) {
						receiveBuffer.add(receiveDelayBuffer.poll());
					}
					receiveBufferLock.unlock();
				} else {
					if (rule.getAction() == Rule.DROP_ACTION) {
						continue;
					} else if (rule.getAction() == Rule.DELAY_ACTION) {
						receiveBufferLock.lock();
						receiveDelayBuffer.add(newMessage);
						receiveBufferLock.unlock();
					} else if (rule.getAction() == Rule.DUPLICATE_ACTION) {
						// TODO: How to deal with this?
					}
				}
			}
		}
	}
	
	public Message receive() {
		receiveBufferLock.lock();
		Message result = receiveBuffer.poll();
		receiveBufferLock.unlock();
		return result;
	}

	public void send(Message message) {
		if (message == null)
			return;
		String dest = message.get_dest();
		if (this.socketMap.get(dest) == null) {
			Node destNode = config.getNode(dest);
			Socket newSocket = NetworkUtil.createSocket(destNode.getIp(),
					destNode.getPort(), "send");
			this.socketMap.put(dest, newSocket);
		}

		// TODO: Update the Send Rules from the config file
		message.set_source(localName);
		Rule rule = this.config.matchSendRule(message);
		if (rule == null) {
			message.set_seqNum(this.seqNum++);
			NetworkUtil.sendMessage(this.socketMap.get(dest), message, "send");
			while (sendDelayBuffer.size() != 0) {
				NetworkUtil.sendMessage(this.socketMap.get(dest),
						sendDelayBuffer.poll(), "send");
			}
		} else {
			if (rule.getAction() == Rule.DROP_ACTION) {
				return;
			} else if (rule.getAction() == Rule.DELAY_ACTION) {
				message.set_seqNum(this.seqNum++);
				sendDelayBuffer.add(message);
			} else if (rule.getAction() == Rule.DUPLICATE_ACTION) {
				// TODO: How?
				// TODO: Remember to set the duplicate field and seqNum accordingly
			}
		}
	}
}
