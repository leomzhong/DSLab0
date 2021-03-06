/**
 * Log class for printing out system information
 * 
 * This class is ported from my previous work in 15-640 Distributed System
 * courses project. Yinsu Chu(yinsuc) is my team mate for that projects and we
 * both contributed to this class.
 * 
 */
public class Log {

	private static final String exceptionHeader = "[exception_message] ";

	public static void normal(String caller, String message) {
		System.out.println("[" + caller + "] " + message);
	}

	public static void error(String caller, String message, Exception exception) {
		System.err.println("[" + caller + "] " + message);
		if (exception != null && exception.getMessage() != null) {
			System.err.println(exceptionHeader + exception.getMessage());
		}
	}
}
