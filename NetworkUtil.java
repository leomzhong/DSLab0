import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class NetworkUtil {
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
	
	public static boolean sendMessage(Socket socket, Message message, String caller) {
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
