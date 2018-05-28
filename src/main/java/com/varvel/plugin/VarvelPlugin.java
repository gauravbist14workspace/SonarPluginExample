package com.varvel.plugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.sonar.api.Plugin;
import org.sonar.api.config.PropertyDefinition;
import org.sonar.api.resources.Qualifiers;

import com.varvel.plugin.language.DefaultProfileDefinition;
import com.varvel.plugin.language.VarvelLanguage;
import com.varvel.plugin.rules.VarvelRulesDefinition;
import com.varvel.plugin.rules.VarvelSensor;

public class VarvelPlugin implements Plugin {

	public static final String SOURCE_FILE_SUFFIXES_KEY = "sonar.varvel.suffixes.sources";
	public static final String HEADER_FILE_SUFFIXES_KEY = "sonar.varvel.suffixes.headers";
	
	public static final String FILE_SUFFIXES_DEFAULT_VALUE = ".cxx,.cpp,.cc,.c";
	public static final String HEADER_SUFFIXES_DEFAULT_VALUE = ".h";

	public void define(Context context) {

		// language
		context.addExtensions(VarvelLanguage.class,DefaultProfileDefinition.class);
				
		// rules
		context.addExtensions(VarvelRulesDefinition.class, VarvelSensor.class);

		// properties elements
		context.addExtension(generalProperties());

	}

	private static List<PropertyDefinition> generalProperties() {
		return new ArrayList<PropertyDefinition>(Arrays.asList(
				PropertyDefinition.builder(SOURCE_FILE_SUFFIXES_KEY)
						.defaultValue(FILE_SUFFIXES_DEFAULT_VALUE).category("VARVEL").name("File Suffixes")
						.description("Comma-separated list of suffixes for files to analyze.")
						.build(),
				PropertyDefinition.builder(HEADER_FILE_SUFFIXES_KEY).multiValues(true)
						.defaultValue(HEADER_SUFFIXES_DEFAULT_VALUE).category("VARVEL").name("Header files suffixes")
						.description("Comma-separated list of suffixes for header files to analyze. Leave empty to use the default.")
						.build(),
				PropertyDefinition.builder("sonar.varvel.source")
						.category("VARVEL").name("Source code directory")
						.description("Source directory where Source c,cpp file(s) are present.")
						.onQualifiers(Qualifiers.PROJECT)
						.defaultValue("")
						.build()));
	}

}
