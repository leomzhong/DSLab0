import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Utility package for network communication
 * 
 * This class is ported from my previous work in 15-640 Distributed System
 * courses project. Yinsu Chu(yinsuc) is my team mate for that projects and we
 * both contributed to this class.
 */
public class NetworkUtil {

	/**
	 * Create a server socket
	 * 
	 * @param port
	 *            port to listen on
	 * @param caller
	 *            Caller of this function
	 * @return null of fail
	 */
	public static ServerSocket createServerSocket(int port, String caller) {
		ServerSocket socket = null;
		try {
			socket = new ServerSocket(port);
		} catch (Exception ex) {
			Log.error(caller, "failed to create server socket on port " + port,
					ex);
			if (socket != null) {
				try {
					socket.close();
				} catch (Exception nestedEx) {
					Log.error(caller, "failed to close socket on port " + port,
							nestedEx);
				}
			}
			return null;
		}
		return socket;
	}

	/**
	 * Create a socket with another host
	 * 
	 * @param host
	 *            host name of the remote host
	 * @param port
	 *            port to set up the connection with
	 * @param caller
	 *            Call of the function
	 * @return null on fail
	 */
	public static Socket createSocket(String host, int port, String caller) {
		Socket socket = null;
		try {
			socket = new Socket(host, port);
		} catch (Exception ex) {
			Log.error(caller,
					"failed to create socket to " + host + ":" + port, ex);
			if (socket != null) {
				try {
					socket.close();
				} catch (Exception nestedEx) {
					Log.error(caller, "failed to close socket to " + host + ":"
							+ port, nestedEx);
				}
			}
			return null;
		}
		return socket;
	}

	/**
	 * Receive a message from the socket
	 * 
	 * @param socket
	 *            Socket to retrieve message
	 * @param caller
	 *            Caller of the function
	 * @return null on fail
	 */
	public static Message receiveMessage(Socket socket, String caller) {
		InputStream input = null;
		ObjectInputStream objectInput = null;
		try {
			input = socket.getInputStream();
		} catch (Exception ex) {
			Log.error(caller, "failed to get input stream", ex);
			return null;
		}
		try {
			objectInput = new ObjectInputStream(input);
		} catch (Exception ex) {
			Log.error(caller, "failed to get object input stream", ex);
			if (objectInput != null) {
				try {
					objectInput.close();
				} catch (Exception nestedEx) {
					Log.error(caller, "failed to close object input stream",
							nestedEx);
				}
			}
			return null;
		}
		Message message = null;
		try {
			message = (Message) objectInput.readObject();
		} catch (Exception ex) {
			Log.error(caller, "failed to get incoming message", ex);
			if (objectInput != null) {
				try {
					objectInput.close();
				} catch (Exception nestedEx) {
					Log.error(caller, "failed to close object input stream",
							nestedEx);
				}
			}
			return null;
		}
		return message;
	}

	/**
	 * Send a message through a socket
	 * 
	 * @param socket
	 *            The socket to use to send the message
	 * @param message
	 *            The message to send
	 * @param caller
	 *            Caller of the function
	 * @return true on success, false on failure
	 */
	public static boolean sendMessage(Socket socket, Message message,
			String caller) {
		OutputStream output = null;
		ObjectOutputStream objectOutput = null;
		try {
			output = socket.getOutputStream();
		} catch (Exception ex) {
			Log.error(caller, "failed to get output stream", ex);
			return false;
		}
		try {
			objectOutput = new ObjectOutputStream(output);
		} catch (Exception ex) {
			Log.error(caller, "failed to create object output stream", ex);
			if (objectOutput != null) {
				try {
					objectOutput.close();
				} catch (Exception nestedEx) {
					Log.error(caller, "failed to close object output stream",
							nestedEx);
				}
			}
			return false;
		}
		try {
			objectOutput.writeObject(message);
		} catch (Exception ex) {
			Log.error(caller, "failed to send message", ex);
			if (objectOutput != null) {
				try {
					objectOutput.close();
				} catch (Exception nestedEx) {
					Log.error(caller, "failed to close object output stream",
							nestedEx);
				}
			}
			return false;
		}
		return true;
	}
}
