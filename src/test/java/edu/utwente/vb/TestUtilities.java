package edu.utwente.vb;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

import org.junit.Ignore;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.Files;

import edu.utwente.vb.example.util.Utils;

@Ignore
public class TestUtilities {
	
	public static List<File> getTestFiles() {
		URL testLoc = TestUtilities.class.getResource("/const.ex");
		File testDir = new File(testLoc.getFile()).getParentFile();
		assert testDir.isDirectory();
		return Lists.newArrayList(testDir.listFiles(new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				return pathname.toString().endsWith(".ex");
			}
		}));
	}
	
	public static Map<String, String> getParameters(File f) throws IOException{
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
		values.put("testname", Utils.camelCaseName(f.getName()));
		
		return values;
	}


}
