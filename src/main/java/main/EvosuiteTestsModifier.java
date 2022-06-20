package main;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

public class EvosuiteTestsModifier {

	private static final Charset ENCODING = StandardCharsets.UTF_8;
	private static final String INJECT_METHOD_CALL = "injectValue(%s, %s, %s);";
	private static final String INJECT_METHOD = "	protected void injectValue(Object object, String fieldName, Object value) throws Throwable {\n"
			+ "		Class clazz = object.getClass();\n"
			+ "		java.lang.reflect.Field field = clazz.getDeclaredField(fieldName);\n"
			+ "		field.set(object, value);\n"
			+ "	}\n";
	
	
	public void modify(List<JokerClassData> jokerClasses) throws FileNotFoundException, IOException {
		
		for(JokerClassData jokerClass : jokerClasses) {
			modify(jokerClass);
		}
	}
	
	
	public void modify(JokerClassData jokerClass) throws FileNotFoundException, IOException {
		String path = Properties.TEST_FOLDER + "/" + jokerClass.getPackageName().replace('.', '/');
		List<File> testFiles = Utils.listFiles(path, ".java", false);
		
		for(File file : testFiles) {
			String fileName = FilenameUtils.removeExtension(file.getName());
			
			if(fileName.contains(jokerClass.getName())) {
				if(fileName.endsWith("est")) {
					modifyTests(jokerClass, file);
				}else if(fileName.endsWith("scaffolding")) {
					modifyScaffolding(jokerClass, file);
				}
			}
		}
	}
	
	private void modifyScaffolding(JokerClassData jokerClass, File file) throws FileNotFoundException, IOException {
		String content = readFile(file);
		content = changeClassName(jokerClass, content);
		
		if(!content.contains(INJECT_METHOD)) {		
			int lastBracketIndex = content.lastIndexOf('}');
			
			content = content.substring(0, lastBracketIndex - 1) + "\n\n" + INJECT_METHOD + content.substring(lastBracketIndex);
			
			List<String> parts = new ArrayList<>(Arrays.asList(content.split(Pattern.quote("}"))));
			parts.add(parts.size() - 1, INJECT_METHOD);
			
			writeFile(file, content);
		}
		
		changeFileName(jokerClass, file);
	}
	
	private void modifyTests(JokerClassData jokerClass, File file) throws FileNotFoundException, IOException {
		String content = readFile(file);
		content = changeClassName(jokerClass, content);
		
		String expression = jokerClass.getOriginalName() + " \\w+ =";
		Pattern pattern = Pattern.compile(expression);
		Matcher matcher = pattern.matcher(content);
		matcher.find();
		String varName = matcher.group().split(Pattern.quote(" "))[1];
		
		for(String field : jokerClass.getInjectFields()) {
			String uselessInstruction = varName + "." + field + " = " + varName + "." + field;
			String lineSeparator = System.getProperty("line.separator");
			String[] lines = Arrays.stream(content.split(Pattern.quote(lineSeparator)))
				.filter(s -> !s.contains(uselessInstruction))
				.toArray(String[]::new);
			
			content = String.join(lineSeparator, lines);
			
			String expression2 = varName + "." + field + " = .*;";
			Pattern pattern2 = Pattern.compile(expression2);
			Matcher matcher2 = pattern2.matcher(content);
			
			while(matcher2.find()) {		
				String value =  matcher2.group().split(Pattern.quote("="))[1].replace(";", "").trim();
				int startIndex = matcher2.start();
				int endIndex = matcher2.end();
				
				content = content.substring(0, startIndex)
						+ String.format(INJECT_METHOD_CALL, varName, "\"" + field + "\"", value)
						+ content.substring(endIndex);

				matcher2 = pattern2.matcher(content);
			}
		}
		
		writeFile(file, content);
		changeFileName(jokerClass, file);
	}
	
	private String changeClassName(JokerClassData jokerClass, String content) {
		String name = jokerClass.getName();
		String originalName = jokerClass.getOriginalName();
		
		return content.replaceAll(name, originalName).replaceAll(toCamelCase(name), toCamelCase(originalName));
	}
	
	private String toCamelCase(String s){
		String result;
		
		if(s != null && s.length() > 0) {
			result = s.substring(0, 1).toLowerCase();
			
			if(s.length() > 1) {
				result += s.substring(1);
			}
		}
		else {
			result = s;
		}
		
		return result;
	}
	
	private String readFile(File file) throws IOException {
		InputStream stream = new FileInputStream(file);
		String content =  IOUtils.toString(stream, ENCODING);
		stream.close();
		
		return content;
	}
	
	private void writeFile(File file, String content) throws IOException {
		OutputStream stream = new FileOutputStream(file);
		IOUtils.write(content, stream, ENCODING);
		stream.close();
	}
	
	
	private void changeFileName(JokerClassData jokerClass, File file) throws IOException {
		String name = jokerClass.getName();
		String originalName = jokerClass.getOriginalName();
		Path source = file.toPath();
		Path newdir = source.getParent().resolve(file.getName().replaceAll(name, originalName));
		
		Files.move(source, newdir, StandardCopyOption.REPLACE_EXISTING);
	}

}
