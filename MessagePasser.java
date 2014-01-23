import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.concurrent.locks.ReentrantLock;

/**
 * MessagePasser class used to exchange message between processes
 * 
 * @author Ming Zhong
 * @author Pratik Shah
 * 
 */
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

	/**
	 * Listening thread generated only once for MessagePasser
	 * 
	 */
	protected class ListenThread implements Runnable {

		/* Maximum failure time we can tolerate */
		private int maxError = 3;

		public void run() {
			if (listenSocket == null) {
				return;
			}
			while (true) {
				Socket newSocket = null;
				try {
					newSocket = listenSocket.accept();
				} catch (Exception ex) {
					Log.error("ListenThread",
							"Cannot accept request, the thread exist", ex);
					maxError--;
					if (maxError < 0)
						break;
					continue;
				}

				// TODO: Do we need to remember this?
				ReceiveThread newReceiver = new ReceiveThread(newSocket);
				Thread newThread = new Thread(newReceiver);
				newThread.start();
			}
		}
	}

	/**
	 * Receive message thread generated for each connecting request
	 */
	protected class ReceiveThread implements Runnable {
		private Socket mySocket;

		public ReceiveThread(Socket socket) {
			this.mySocket = socket;
		}

		public void run() {

			/* Maximum failure time we can tolerate */
			int maxError = 3;

			while (true) {
				Message newMessage = NetworkUtil.receiveMessage(mySocket,
						"ReceiveThread");
				if (newMessage == null) {
					maxError--;
					if (maxError < 0) {
						Log.normal("ReceiveThread",
								"Too many failures, the socket is broken");
						return;
					}
					continue;
				}

				/* Update the receive rules */
				receiveBufferLock.lock();
				config.updateReceiveRules(configFileName);
				Rule rule = config.matchReceiveRule(newMessage);
				/* No rule is matched */
				if (rule == null) {
					receiveBuffer.add(newMessage);
					while (receiveDelayBuffer.size() != 0) {
						receiveBuffer.add(receiveDelayBuffer.poll());
					}
				} else {
					/* The message match the drop rule */
					if (rule.getAction() == Rule.DELAY_ACTION) {
						receiveDelayBuffer.add(newMessage);
					} else if (rule.getAction() == Rule.DUPLICATE_ACTION) {
						/* The message match the duplicate rule */
						receiveBuffer.add(newMessage);
						receiveBuffer.add(newMessage.clone());
						while (receiveDelayBuffer.size() != 0) {
							receiveBuffer.add(receiveDelayBuffer.poll());
						}
					} else if (rule.getAction() != Rule.DROP_ACTION) {
						Log.normal(
								"The rule's action is unknown, receive it normally",
								"MessagePasser");
					}
				}
				receiveBufferLock.unlock();
			}
		}
	}

	/**
	 * Retrieve a message from the receive buffer
	 * 
	 * @return The first message in the receive buffer
	 */
	public Message receive() {
		receiveBufferLock.lock();
		Message result = receiveBuffer.poll();
		receiveBufferLock.unlock();
		return result;
	}

	/**
	 * Ensure there is a socket to the message destination
	 */
	private boolean ensureSocket(Message message) {
		String dest = message.get_dest();
		/* Set up the connection if not done */
		if (this.socketMap.get(dest) == null) {
			Node destNode = config.getNode(dest);
			if (destNode == null) {
				Log.normal("The node does not exist", "send");
				return false;
			}
			Socket newSocket = NetworkUtil.createSocket(destNode.getIp(),
					destNode.getPort(), "send");
			if (newSocket == null) {
				return false;
			}
			this.socketMap.put(dest, newSocket);
		}
		return true;
	}

	/**
	 * Send a message
	 * 
	 * @param message
	 *            The message to send
	 */
	public void send(Message message) {
		if (message == null)
			return;
		String dest = message.get_dest();

		if (!ensureSocket(message)) {
			Log.normal("send", "Fail to connect with the destination");
			return;
		}

		/* Update the send rules */
		this.config.updateSendRules(this.configFileName);
		message.set_source(localName);
		Rule rule = this.config.matchSendRule(message);

		/* Deal with the message according to the rule matched */
		if (rule == null) {
			message.set_seqNum(this.seqNum++);
			boolean status = NetworkUtil.sendMessage(this.socketMap.get(dest),
					message, "send");
			if (!status) {
				this.socketMap.remove(dest);
			}
			while (sendDelayBuffer.size() != 0) {
				Message delayedMessage = sendDelayBuffer.poll();
				if (!ensureSocket(delayedMessage)) {
					Log.normal("send",
							"Fail to connect with the destination of delayed message");
					continue;
				}
				status = NetworkUtil.sendMessage(
						this.socketMap.get(delayedMessage.get_dest()),
						delayedMessage, "send");
				if (!status) {
					this.socketMap.remove(delayedMessage.get_dest());
				}
			}
		} else {
			if (rule.getAction() == Rule.DROP_ACTION) {
				return;
			} else if (rule.getAction() == Rule.DELAY_ACTION) {
				message.set_seqNum(this.seqNum++);
				sendDelayBuffer.add(message);
			} else if (rule.getAction() == Rule.DUPLICATE_ACTION) {
				message.set_seqNum(this.seqNum++);
				Message secondMessage = message.clone();
				secondMessage.set_duplicate(true);
				boolean status = NetworkUtil.sendMessage(this.socketMap.get(dest), message,
						"send");
				status |= NetworkUtil.sendMessage(this.socketMap.get(dest),
						secondMessage, "send");
				if (!status) {
					this.socketMap.remove(dest);
				}
				while (sendDelayBuffer.size() != 0) {
					Message delayedMessage = sendDelayBuffer.poll();
					if (!ensureSocket(delayedMessage)) {
						Log.normal("send",
								"Fail to connect with the destination of delayed message");
						continue;
					}
					status = NetworkUtil.sendMessage(
							this.socketMap.get(delayedMessage.get_dest()),
							delayedMessage, "send");
					if (!status) {
						this.socketMap.remove(delayedMessage.get_dest());
					}
				}
			} else {
				Log.normal(
						"The rule's action is unknown, send the message normally",
						"MessagePasser");
			}
		}
	}
}
