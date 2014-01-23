import java.util.Scanner;

/**
 * Command Line Tool for testing the MessagePasser class
 * @author Ming Zhong
 * @author Pratik Shah
 *
 */
public class CLI {

	private static final String CLI_USAGE = "usage: java CLI [config_filename] [local_name]";

	private static final String CMD_PROMPT = "DS_LAB0>> ";
	private static final String SEND_CMD = "send";
	private static final String RECEIVE_CMD = "receive";
	private static final String QUIT_CMD = "quit";

	private static final String SEND_USAGE = "usage: send [dest] [kind] [data]";
	private static final String RECEIVE_USAGE = "usage: receive";

	private static final int SEND_CMD_ARG_NUM = 4;
	private static final int RECEIVE_CMD_ARG_NUM = 1;

	public static void main(String[] args) {
		if (args.length != 2) {
			System.out.println(CLI_USAGE);
			return;
		}
		Scanner input = new Scanner(System.in);
		MessagePasser passer = new MessagePasser(args[0], args[1]);

		/* the main loop to read user input */
		while (true) {
			System.out.print(CMD_PROMPT);
			String cmd = input.nextLine();

			/* split user input by space characters */
			String[] parsedCommand = cmd.split("\\s+");
			/* Continue if the command is blank */
			if (parsedCommand.length == 0) {
				continue;
			}

			/* Quit command */
			if (parsedCommand[0].equals(QUIT_CMD)) {
				System.exit(1);
			} else if (parsedCommand[0].equals(SEND_CMD)) {
				/* Send command */
				if (parsedCommand.length != SEND_CMD_ARG_NUM) {
					System.out.println(SEND_USAGE);
					continue;
				} else {
					Message newMessage = new Message(parsedCommand[1],
							parsedCommand[2], parsedCommand[3]);
					passer.send(newMessage);
				}
			} else if (parsedCommand[0].equals(RECEIVE_CMD)) {
				/* Recieve command */
				if (parsedCommand.length != RECEIVE_CMD_ARG_NUM) {
					System.out.println(RECEIVE_USAGE);
					continue;
				} else {
					Message receivedMessage = passer.receive();
					if (receivedMessage == null) {
						System.out.println("There is no message");
					} else {
						System.out.println("The message received is:");
						System.out.println(receivedMessage.toString());
					}
				}
			}
		}
	}
}
