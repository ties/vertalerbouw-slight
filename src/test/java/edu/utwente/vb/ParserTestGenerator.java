package edu.utwente.vb;

import static java.lang.System.out;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Map;

import org.antlr.stringtemplate.StringTemplate;
import org.antlr.stringtemplate.StringTemplateGroup;

import com.google.common.io.Files;

public class ParserTestGenerator{	
	public ParserTestGenerator() throws IOException, URISyntaxException {
		URL templateGroupFile = getClass().getResource("/dogfood.stg");
		
		assert templateGroupFile != null;
		
		out.println("template group: " + templateGroupFile);
		
		File dogfoodStg = new File(templateGroupFile.toURI());
		
		StringTemplateGroup group =  new StringTemplateGroup(new FileReader(dogfoodStg));
		StringBuffer testFunctions = new StringBuffer();
		
		//Setup is klaar, itereer nu over de files heen
		for(File f : FileUtilities.getTestFiles()){
			StringTemplate functionTemplate;
			
			Map<String, String> values = TestUtilities.getParameters(f);
			values.put("rule", "program");
			values.put("filename", f.getName());
			//
			if(values.containsKey("expected")){
				 functionTemplate = group.getInstanceOf("testParserWithExpected");
			} else {
				functionTemplate = group.getInstanceOf("testParser");
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
		
	public static void main(String[] args) throws Exception{
		new ParserTestGenerator();
	}
	
}
