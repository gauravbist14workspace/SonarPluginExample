package com.varvel.plugin.rules;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.api.server.rule.RulesDefinitionXmlLoader;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

import com.varvel.plugin.language.VarvelLanguage;

public class VarvelRulesDefinition implements RulesDefinition {

	private static final Logger LOGGER = Loggers
			.get(VarvelRulesDefinition.class);

	public static final String REPO_KEY = "varvelRepo";
	public static final String REPO_NAME = "Varvel Repository";

	private RulesDefinitionXmlLoader xmlLoader = null;

	public VarvelRulesDefinition(RulesDefinitionXmlLoader xmlLoader) {
		this.xmlLoader = xmlLoader;
	}

	public void define(Context context) {
		Charset charset = StandardCharsets.UTF_8;
		NewRepository repository = context.createRepository(REPO_KEY, VarvelLanguage.KEY);
		repository.setName(REPO_NAME);
		
		try {
			InputStream xmlStream = getClass().getResourceAsStream("/rules/varvel-rules.xml");
			
			xmlLoader.load(repository, xmlStream, charset);
		} catch (NullPointerException e){
			LOGGER.debug("Unable to read rules.");
			LOGGER.debug(e.getMessage());
		} finally {
			repository.done();
		}
	}

	// testing purpose
	public static void main(String[] args) {
		File file = new File("src/main/resources/rules/varvel-rules.xml");
		if (file.exists()) {
			BufferedReader br = null;
			try {
				br = new BufferedReader(new FileReader(file));
				String str;
				while ((str = br.readLine()) != null) {
					String rule_key = str.trim();
					System.out.println(rule_key);
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		} else {
			System.out.println("Unable to read rules.");
		}
	}
}
