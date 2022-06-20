package main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;

public class Evosuite {
	
	
	public String[] getTestableClasses(String packageName, String classpath) throws Exception {
		String path = classpath + "/" + packageName.replace('.', '/');
		
		return Arrays.stream(getTestableClasses(path))
				.map(s -> packageName.isBlank() ? s : packageName + "." + s)
				.toArray(String[]::new);	
	}
	
	public String[] getTestableClasses(String classpath) throws Exception {
		return executeEvosuite("-listClasses -target \"" + classpath + "\"", false).split("\n");	
	}
	
	public void generateClassTests(String className, String projectPath) throws Exception {
		executeEvosuite("-class " + className + " -projectCP \"" + projectPath + Properties.DEPENDENCY_CLASSPATH + "\" " + Properties.OTHER_ARGUMENTS, true);
	}
	
	public void generateProjectTests(String projectClasspath) throws Exception {
		try {
			executeEvosuite("-target \"" + projectClasspath + Properties.DEPENDENCY_CLASSPATH + "\" " + Properties.OTHER_ARGUMENTS, true);
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public void generatePackageTests(String packageName, String projectPath) throws Exception {
		try {
			executeEvosuite("-prefix " + packageName + " -projectCP \"" + projectPath + Properties.DEPENDENCY_CLASSPATH + "\" " + Properties.OTHER_ARGUMENTS, true);
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	private String executeEvosuite(String arguments, boolean printOuput) throws Exception {
		String[] logs = new String[] {"", ""};
		
		String directory = System.getProperty("user.dir");
		String command = "java -jar \"" + directory + "/evosuite-1.2.0.jar\" " + arguments;
		
		ProcessBuilder processBuilder = new ProcessBuilder();
		processBuilder.command("cmd.exe", "/c", command);
        Process process = processBuilder.start();
        
        BufferedReader outReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
        
        Thread outWorker = new Thread() {
            public void run() {
            	String readline;
            	int i = 0;
            	
                try {
                	while ((readline = outReader.readLine()) != null) {
                    	logs[0] += readline + "\n";
                        if(printOuput)System.out.println(++i + " " + readline);
                    }
                	
                	outReader.close();
                } catch(IOException e) {
                    e.printStackTrace();
                }
            }
        };
        
        
        Thread errorWorker = new Thread() {
            public void run() {
            	String readline;
            	int i = 0;
            	
                try {
                	while ((readline = errorReader.readLine()) != null) {
                		logs[1] += readline + "\n";
                        if(printOuput)System.err.println(++i + " " + readline);
                    }
                	errorReader.close();
                } catch(IOException e) {
                    e.printStackTrace();
                }
            }
        };
        
        outWorker.start();
        errorWorker.start();
        process.waitFor();
        
        try {
        	outWorker.join(2000);
        }catch(Exception e){}
        
        try {
        	errorWorker.join(2000);
        }catch(Exception e){}
               
        return logs[0];
	}
}
