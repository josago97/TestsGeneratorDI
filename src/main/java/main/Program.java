package main;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class Program {	
	public static void main(String[] args) throws IOException {
		
		long evosuiteTime = 0;
		long lastTime = System.currentTimeMillis();
		
		Properties.readArguments(args);
		
		boolean isMultipleClasses = Properties.PREFIX != null || Properties.TARGET != null;
		JokerClassCreator jokerClassCreator = null;
		Evosuite evosuite = new Evosuite();
		Path directory = null;
		List<String> classesToTest = new ArrayList<String>();
		List<JokerClassData> jokerClasses = new ArrayList<JokerClassData>();
		String projectClasspath;
		
		try {
			if(Properties.TARGET != null) {
				projectClasspath = Properties.TARGET;
			} else {
				projectClasspath = Properties.PROJECT_CLASSPATH;
			}
			
			jokerClassCreator = new JokerClassCreator(projectClasspath);
			
			if(isMultipleClasses) {
				if(Properties.TARGET != null)
					classesToTest = Arrays.asList(evosuite.getTestableClasses(Properties.TARGET));
				else
					classesToTest = Arrays.asList(evosuite.getTestableClasses(Properties.PREFIX, Properties.PROJECT_CLASSPATH));			
				
			}else {
				classesToTest.add(Properties.CLASS);
			}
			
			jokerClasses = jokerClassCreator.createJokerClasses(classesToTest);
			
			System.out.println("Generating tests for " + classesToTest.size() + " classes:");
			for(String className : classesToTest) System.out.println(className);
			System.out.println();
			
			if(jokerClasses.size() > 0) {
				directory = Paths.get(projectClasspath);
				jokerClassCreator.compile(jokerClasses, directory);
				EvosuiteTestsModifier modifier = new EvosuiteTestsModifier();
				
				for(int i = classesToTest.size() - 1; i >= 0; i--) {
					String className = classesToTest.get(i);
					System.out.println("Generating tests for " + className);
					
					JokerClassData jokerClass = jokerClasses.stream()
							.filter(x -> x.getOriginalClassName().equals(className))
							.findFirst()
							.orElse(null);
					
					long time = System.currentTimeMillis();
					lastTime = time;
					
					try {
						if(jokerClass != null) {
							evosuite.generateClassTests(jokerClass.getClassName(), directory.toAbsolutePath().toString());
						}else {
							evosuite.generateClassTests(className, directory.toAbsolutePath().toString());
						}
						
					} catch(Exception e) {
						e.printStackTrace();
						if(jokerClass == null)classesToTest.remove(className);
					}
					
					time = System.currentTimeMillis();
					evosuiteTime += time - lastTime;
					lastTime = time;
					
					if(jokerClass != null) {
						try {
							modifier.modify(jokerClass);	
						} catch(Exception e) {
							e.printStackTrace();
							classesToTest.remove(className);
						}
					}
				}	
			} else {

				long time = System.currentTimeMillis();
				lastTime = time;
				
				if(Properties.TARGET != null) {
					evosuite.generateProjectTests(Properties.TARGET);
				} else if(Properties.PREFIX != null) {
					evosuite.generatePackageTests(Properties.PREFIX, Properties.PROJECT_CLASSPATH);
				} else {
					evosuite.generateClassTests(Properties.CLASS, Properties.PROJECT_CLASSPATH);
				}
				
				time = System.currentTimeMillis();
				evosuiteTime += time - lastTime;
				lastTime = time;
			}
			
			System.out.println(classesToTest.size() + " tests generated for:");
			for(String className : classesToTest) System.out.println(className);
			System.out.println();
			
		} catch (Exception e){
			e.printStackTrace();
		}
		
		try {
			if(jokerClassCreator != null) jokerClassCreator.deleteFiles(jokerClasses, directory);
		}catch(Exception e){}
		
		
		System.out.println();
		
		System.out.println("Evosuite time (seconds): " + evosuiteTime / 1000.0);
		
		System.out.println();
	}
}
