package edu.utwente.vb;

import java.io.File;

import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.antlr.stringtemplate.StringTemplate;
import org.antlr.stringtemplate.StringTemplateGroup;
import org.apache.log4j.Logger;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.Files;

import static java.lang.System.out;

public class ParserTestGenerator{	
	public ParserTestGenerator() throws IOException, URISyntaxException {
		URL templateGroupFile = getClass().getResource("/dogfood.stg");
		
		assert templateGroupFile != null;
		
		out.println("template group: " + templateGroupFile);
		
		File dogfoodStg = new File(templateGroupFile.toURI());
		
		StringTemplateGroup group =  new StringTemplateGroup(new FileReader(dogfoodStg));
		StringBuffer testFunctions = new StringBuffer();
		
		//Setup is klaar, itereer nu over de files heen
		for(File f : TestUtilities.getTestFiles()){
			StringTemplate functionTemplate;
			
			Map<String, String> values = getParameters(f);
			values.put("rule", "program");
			values.put("filename", f.getName());
			//
			if(values.containsKey("expected")){
				 functionTemplate = group.getInstanceOf("testWithExpected");
			} else {
				functionTemplate = group.getInstanceOf("testFunction");
			}
			functionTemplate.setAttributes(values);

			out.println("- " + values.get("testname") + " for " + f);
			
			testFunctions.append(functionTemplate.toString());
		}
		
		StringTemplate clazzSource = group.getInstanceOf("testclass");
		clazzSource.setAttribute("classname", "TestParserWithExamples");
		clazzSource.setAttribute("functions", testFunctions.toString());
		
		
		//Nu Target file verzinnen;
		File parent = dogfoodStg.getParentFile().getParentFile().getParentFile();//targets/generated-sources/antlr3/
		//schrijven
		Files.write(clazzSource.toString(), new File(parent, "/src/test/java/edu/utwente/vb/TestParserWithExamples.java"), Charset.defaultCharset());
	}
	
	private Map<String, String> getParameters(File f) throws IOException{
		Map<String, String> values = Maps.newHashMap();
		//De commentaar header
		StringBuffer uitleg = new StringBuffer();
		
		for(String line: Files.readLines(f, Charset.defaultCharset())){
			
			if(line.length() > 3 && line.startsWith("#")){
				String rest = line.substring(1);
				String[] keyValue = rest.split(":", 2);
				if(keyValue.length == 2){
					values.put(keyValue[0].trim(), keyValue[1].trim());
				} else {
					uitleg.append(rest).append("\n");
				}
			}
		}
		
		values.put("uitleg", uitleg.toString());
		values.put("testname", testName(f));
		
		return values;
	}
	
	public static void main(String[] args) throws Exception{
		new ParserTestGenerator();
	}
	
	private String testName(File f){
		List<String> words = Lists.newArrayList();
		StringBuffer word = new StringBuffer();
		
		for(Character c : Lists.charactersOf(f.getName())){
			if(c.equals('_') || c.equals('-')){
				words.add(word.toString());
				word = new StringBuffer();
			} else if(c.equals('.')){
				break;//extensie begint
			} else {
				word.append(c);
			} 
		}
		words.add(word.toString());
				
		StringBuffer camelCase = new StringBuffer();
		for(String w : words){
			if(w.length() >=  2){
				camelCase.append(w.substring(0,1).toUpperCase());
				camelCase.append(w.substring(1));
			} else {
				camelCase.append(System.currentTimeMillis());
			}
		}
		
		return camelCase.toString();
	}
}
