package com.varvel.plugin.language;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import org.sonar.api.server.profile.BuiltInQualityProfilesDefinition;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

import com.varvel.plugin.rules.VarvelRulesDefinition;

public class DefaultProfileDefinition implements
		BuiltInQualityProfilesDefinition {
	private static final Logger LOGGER = Loggers.get(DefaultProfileDefinition.class);
	private static final String QUALITY_PROFILE_NAME = "Varvel Quality Profile";

	private static String rule_key = "";

	@Override
	public void define(Context context) {
		NewBuiltInQualityProfile profile = context.createBuiltInQualityProfile(QUALITY_PROFILE_NAME, 
				VarvelLanguage.KEY);

		BufferedReader br = null;
		try {
			br = new BufferedReader(new InputStreamReader(
					this.getClass().getResourceAsStream("/rules/default-profile.xml"), 
					StandardCharsets.UTF_8.name()));
			String str;
			while ((str = br.readLine()) != null) {
				rule_key = str.trim();
				if (!rule_key.equals("")) {
					profile.activateRule(VarvelRulesDefinition.REPO_KEY, rule_key);
				}
			}
		} catch (NullPointerException e) {
			LOGGER.debug("Unable to read \'default-profile.xml\' from resources.");
			LOGGER.error(e.getMessage());
		} catch(IllegalArgumentException e) {
			LOGGER.debug("Rule " + rule_key + " already activated in " + VarvelRulesDefinition.REPO_NAME);
			LOGGER.error(e.getMessage());
		} catch (Exception e) {
			LOGGER.debug("Rule " + rule_key + " does not exist for "+ VarvelRulesDefinition.REPO_NAME);
			LOGGER.error(e.getMessage());
		} finally {
			try {
				br.close();
			} catch (IOException e) {
				LOGGER.debug(e.getMessage());
			}
		}

		profile.done();
	}

	// Checking whether quality rules are readable or not ...
	public static void main(String[] args) {
		File file = new File("src/main/resources/rules/default-profile.xml");
		if (file.exists()) {
			BufferedReader br = null;
			try {
				br = new BufferedReader(new FileReader(file));
				String str;
				while ((str = br.readLine()) != null) {
					rule_key = str.trim();
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
			System.out.println("Unable to read default profile.");
		}
	}
}
