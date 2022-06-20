package main;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.commons.io.FileUtils;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.Modifier;
import javassist.NotFoundException;

public class JokerClassCreator {
	
	private ClassPool pool;
	
	public JokerClassCreator(String classpath) throws NotFoundException {
		pool = ClassPool.getDefault();
		pool.insertClassPath(classpath);
	}
	
	public List<JokerClassData> createJokerClasses(List<String> classNames) throws NotFoundException {
		List<JokerClassData> result = new ArrayList<JokerClassData>();
		
		for(String className : classNames) {
			JokerClassData jokerClass = createJokerClass(className);
			
			if(jokerClass != null) result.add(jokerClass);
		}
		
		return result;
	}
	
	public JokerClassData createJokerClass(String className) throws NotFoundException {
		JokerClassData result = null;
		try {
			CtClass clazz = pool.get(className);
			List<String> fields = new ArrayList<String>();
			
			for(CtField field : clazz.getDeclaredFields()) {
				
				if(field.getModifiers() == Modifier.PRIVATE && Properties.INJECT_ANNOTATION.stream().anyMatch(x -> field.hasAnnotation(x))) {
					field.setModifiers(Modifier.PUBLIC);
					fields.add(field.getName());
				}
			}
			
			if(fields.size() != 0) {
				String originalClassName = clazz.getName();
				String newClassName = originalClassName + UUID.randomUUID().toString().replace("-", "").substring(0, 5);
				String packageName = clazz.getPackageName();
				
				clazz.replaceClassName(clazz.getName(), newClassName);
				
				result = new JokerClassData(clazz, originalClassName, newClassName, packageName);
				result.getInjectFields().addAll(fields);
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		return result;
	}
	
	public void compile(List<JokerClassData> jokerClasses, Path directory) throws CannotCompileException, IOException {
		
		for(JokerClassData jokerClass : jokerClasses) {
			compile(jokerClass, directory);
		}
	}
	
	public void compile(JokerClassData jokerClass, Path directory) throws CannotCompileException, IOException {
		jokerClass.getClazz().writeFile(directory.toString());
	}
	
	public void deleteFiles(List<JokerClassData> jokerClasses, Path directory) throws IOException {
		List<File> files = Utils.listFiles(directory.toAbsolutePath().toString(), ".class", true);
		
		for(JokerClassData jokerClass : jokerClasses) {
			File file = files.stream()
		     .filter(f -> f.getName().contains(jokerClass.getName()))
		     .findFirst()
		     .orElse(null);
			
			if(file != null) FileUtils.forceDelete(file);
		}
	}
}
