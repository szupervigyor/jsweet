package org.jsweet.input.typescriptdef.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.function.Consumer;
import java.util.logging.Logger;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

public class ProcessUtil {
	private final static Logger logger = Logger.getLogger("process");

	public static String TSD_COMMAND = "tsd";
	public static String MVN_COMMAND = "mvn";
	public static String BOWER_COMMAND = "bower";
	public static String EXTRA_PATH;
	
	public static void init() {
		// hack for Eclipse under Mac OSX
		if (!System.getenv("PATH").contains("/usr/local/bin") && new File("/usr/local/bin/tsd").exists()) {
			ProcessUtil.EXTRA_PATH = "/usr/local/bin";
			ProcessUtil.TSD_COMMAND = "/usr/local/bin/tsd";
			ProcessUtil.MVN_COMMAND = "/usr/local/bin/mvn";
			ProcessUtil.BOWER_COMMAND = "/usr/local/bin/bower";
		}
	}
	
	public static void runCmd(Consumer<String> stdoutConsumer, String... cmd) {
		runCmd(null, stdoutConsumer, cmd);
	}
	
	public static void runCmd(File directory, Consumer<String> stdoutConsumer, String... cmd) {
		System.out.println("run command: " + StringUtils.join(cmd, " "));

		String[] args;
		if (System.getProperty("os.name").startsWith("Windows")) {
			args = new String[] { "cmd", "/c" };
		} else {
			args = new String[0];
		}
		args = ArrayUtils.addAll(args, cmd);

		System.out.println("run command: '" + StringUtils.join(args, " ") + "' in directory " + directory);
		// logger.fine("run command: " + StringUtils.join(args, " "));
		int code = -1;
		try {

			ProcessBuilder processBuilder = new ProcessBuilder(args);
			processBuilder.redirectErrorStream(true);
			if (directory != null) {
				processBuilder.directory(directory);
			}
			if (!StringUtils.isBlank(EXTRA_PATH)) {
				processBuilder.environment().put("PATH", processBuilder.environment().get("PATH") + File.pathSeparator + EXTRA_PATH);
			}
			
			Process process = processBuilder.start();

			try (BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
				String line;
				while ((line = in.readLine()) != null) {
					if (stdoutConsumer != null) {
						stdoutConsumer.accept(line);
					} else {
						logger.info("OUT:" + line);
					}
				}
			}

			code = process.waitFor();
			if (code != 0) {
				throw new RuntimeException("error while exectuting: " + StringUtils.join(args, " ") + ", error code: "
						+ code);
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
