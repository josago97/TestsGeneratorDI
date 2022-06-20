package main;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public class Properties {
	
	private static final String CLASS_ARGUMENT = "-class"; 
	public static String CLASS;
	
	private static final String PROJECT_CLASSPATH_ARGUMENT = "-projectCP";
	public static String PROJECT_CLASSPATH;
	
	private static final String PREFIX_ARGUMENT = "-prefix"; 
	public static String PREFIX;
	
	private static final String TARGET_ARGUMENT = "-target"; 
	public static String TARGET;
	
	private static final String INJECT_ANNOTATION_ARGUMENT = "-inject_annotation";
	public static List<String> INJECT_ANNOTATION = Arrays.asList("javax.inject.Inject");
	
	private static final String PROBABILITY_FUNCTIONAL_MOCKING_ARGUMENT = "-Dp_functional_mocking";
	public static double PROBABILITY_FUNCTIONAL_MOCKING = 0.8;
	
	private static final String TEST_FOLDER_ARGUMENT = "-Dtest_dir";
	public static String TEST_FOLDER = "evosuite-tests";
	
	public static String OTHER_ARGUMENTS = "";
	public static String DEPENDENCY_CLASSPATH = "";
	
	public static void readArguments(String[] args) {
		
		for(int i = 0; i < args.length; i += 1) {
			String command = args[i];
			
			switch(command) {
				case CLASS_ARGUMENT:
					CLASS = args[++i];
					break;
				
				case PROJECT_CLASSPATH_ARGUMENT:
					PROJECT_CLASSPATH = getClasspath(args[++i]);
					break;
					
				case PREFIX_ARGUMENT:
					PREFIX = args[++i];
					break;
				
				case TARGET_ARGUMENT:
					TARGET = getClasspath(args[++i]);
					break;
					
				case INJECT_ANNOTATION_ARGUMENT:
					INJECT_ANNOTATION = Arrays.asList(args[++i].split(","));
					break;
					
				default:
					if(command.startsWith(PROBABILITY_FUNCTIONAL_MOCKING_ARGUMENT)) {
						PROBABILITY_FUNCTIONAL_MOCKING = Double.valueOf(getEvosuiteArgumentValue(PROBABILITY_FUNCTIONAL_MOCKING_ARGUMENT, command));
					}
					else if(command.startsWith(TEST_FOLDER_ARGUMENT)) {
						TEST_FOLDER = getEvosuiteArgumentValue(TEST_FOLDER_ARGUMENT, command);
						OTHER_ARGUMENTS += TEST_FOLDER_ARGUMENT + "=\"" + TEST_FOLDER + "\" ";
					}else {
						OTHER_ARGUMENTS += command + " ";
					}
					break;
			}
		}
		
		OTHER_ARGUMENTS += PROBABILITY_FUNCTIONAL_MOCKING_ARGUMENT + "=" + PROBABILITY_FUNCTIONAL_MOCKING;
		OTHER_ARGUMENTS = OTHER_ARGUMENTS.trim();
	}
	
	private static String getEvosuiteArgumentValue(String key, String command) {
		String[] parts = command.split(Pattern.quote("="));
		
		return parts[1];
	}
	
	private static String getClasspath(String value) {
		String[] parts = value.split(Pattern.quote(";"));
		
		if(parts.length > 1) {
			DEPENDENCY_CLASSPATH = ";" + String.join(";", Arrays.copyOfRange(parts, 1, parts.length));
		}
		
		return parts[0];
	}
}
