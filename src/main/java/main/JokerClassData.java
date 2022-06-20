package main;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import javassist.CtClass;

public class JokerClassData {

	private CtClass clazz;
	private String packageName;
	private String originalName;
	private String name;
	private List<String> injectFields;
	
	public JokerClassData(CtClass clazz, String originalClassName, String className, String packageName) {
		this.clazz = clazz;
		this.packageName = packageName;
		this.originalName = getName(originalClassName);
		this.name = getName(className);
		this.injectFields = new ArrayList<String>();
	}
	
	public CtClass getClazz() {
		return clazz;
	}
	
	public String getPackageName() {
		return packageName;
	}
	
	public String getOriginalName() {
		return originalName;
	}
	
	public String getOriginalClassName() {
		return packageName + "." + originalName;
	}
	
	public String getName() {
		return name;
	}
	
	public String getClassName() {
		return packageName + "." + name;
	}
	
	
	public List<String> getInjectFields(){
		return injectFields;
	}
	
	
	private String getName(String className) {
		String[] parts = className.split(Pattern.quote("."));
		
		return parts[parts.length - 1];
	}
}
