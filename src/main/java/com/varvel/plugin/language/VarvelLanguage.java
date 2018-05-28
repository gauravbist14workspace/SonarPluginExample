package com.varvel.plugin.language;

import java.util.ArrayList;
import java.util.List;

import org.sonar.api.config.Configuration;
import org.sonar.api.resources.AbstractLanguage;

import com.varvel.plugin.VarvelPlugin;

public class VarvelLanguage extends AbstractLanguage {

	public static final String KEY = "varvel_c";
	public static final String NAME = "Varvel C";

	private Configuration configuration;

	public VarvelLanguage(Configuration configuration) {
		super(KEY, NAME);
		this.configuration = configuration;
	}

	@Override
	public String[] getFileSuffixes() {
		String[] stringArray = chechIfEmptyIssues(configuration.getStringArray(VarvelPlugin.SOURCE_FILE_SUFFIXES_KEY));
		if (stringArray.length == 0)
			return VarvelPlugin.FILE_SUFFIXES_DEFAULT_VALUE.split(",");
		else
			return stringArray;
	}

	private String[] chechIfEmptyIssues(String[] stringArray) {
		List<String> nonEmpty = new ArrayList<String>();
		for(String str : stringArray){
			if(!str.trim().equals(""))
				nonEmpty.add(str);
		}
		return nonEmpty.toArray(new String[nonEmpty.size()]);
	}

}
