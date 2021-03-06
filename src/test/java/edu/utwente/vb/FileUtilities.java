package edu.utwente.vb;

import java.io.File;
import java.io.FileFilter;
import java.net.URL;
import java.util.List;

import org.junit.Ignore;

import com.google.common.collect.Lists;

public class FileUtilities {
	
	public static List<File> getTestFiles() {
		URL testLoc = FileUtilities.class.getResource("/dogfood.stg");
		File testDir = new File(testLoc.getFile()).getParentFile();
		assert testDir.isDirectory();
		return Lists.newArrayList(testDir.listFiles(new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				return pathname.toString().endsWith(".ex");
			}
		}));
	}
}
