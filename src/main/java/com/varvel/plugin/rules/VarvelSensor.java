package com.varvel.plugin.rules;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.Sensor;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.batch.sensor.issue.NewIssue;
import org.sonar.api.batch.sensor.issue.NewIssueLocation;
import org.sonar.api.config.Configuration;
import org.sonar.api.rule.RuleKey;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

import com.varvel.plugin.language.VarvelLanguage;
import com.varvel.plugin.model.VarvelError;

public class VarvelSensor implements Sensor {

	private static final Logger LOGGER = Loggers.get(VarvelSensor.class);

	private static final String REPORT_PATH_KEY = "sonar.varvel.reportPath";
	private static final String DEFAULT_REPORT_PATH = "outputs/report.csv";

	private Configuration configuration;
	private FileSystem fileSystem;
	private SensorContext context;

	private String sourceCodeDir = "";

	protected String reportPathKey() {
		return REPORT_PATH_KEY;
	}

	public String getReportPath() {
		Optional<String> reportPath = configuration.get(reportPathKey());
		if (reportPath.isPresent()) {
			return reportPath.get();
		} else {
			return DEFAULT_REPORT_PATH;
		}
	}

	public void setSourceDir() {
		Optional<String> vapBasePath = configuration.get("sonar.varvel.source");
		if (vapBasePath.isPresent()) {
			sourceCodeDir = vapBasePath.get();
		} else {
			sourceCodeDir = "";
		}
	}

	public VarvelSensor() {
	}
	
	public VarvelSensor(String sourceCodeDir) {
		this.sourceCodeDir = sourceCodeDir;
	}

	public VarvelSensor(Configuration configuration, FileSystem fileSystem) {
		this.configuration = configuration;
		this.fileSystem = fileSystem;
	}

	public void describe(SensorDescriptor descriptor) {
		descriptor.onlyOnLanguage(VarvelLanguage.KEY).name("VARVEL custom sensor for sonar.");
	}

	public void execute(SensorContext context) {
		this.context = context;
		setSourceDir();

		File file = new File(getReportPath());

		if (file.exists()) {
			List<VarvelError> errorList = processReport(file);
			for (VarvelError error : errorList) {
				saveViolations(error);
			}
		} else {
			LOGGER.debug("CSV report file cannot be fetched under "
					+ getReportPath());
		}
	}

	private List<VarvelError> processReport( File report) {
		LOGGER.debug("Parsing 'Varvel' format");

		List<VarvelError> errorList = new ArrayList<VarvelError>();

		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(report));

			String text = null;
			int line = 0;

			while ((text = reader.readLine()) != null) {
				if (line == 0) { 		// Header row ignored
					line++;
					continue;
				} 
				
				String[] rowValues = text.split(",");
				
				String rank = rowValues[3].subSequence(1, rowValues[3].length() - 1).toString();
				String sub_type = rowValues[7].subSequence(1, rowValues[7].length() - 1).toString();
				String file = normalizeFilePath(rowValues[9].subSequence(1, rowValues[9].length() - 1).toString());
				String ln = rowValues[10].subSequence(1, rowValues[10].length() - 1).toString();
				String description = rowValues[21].subSequence(1, rowValues[21].length() - 1).toString();
				
				VarvelError varvelError = new VarvelError(rank, sub_type, file, ln, description);
				
				errorList.add(varvelError);
			}
		} catch (FileNotFoundException e) {
			LOGGER.debug("The file with the specified pathname cannot be accessed.");
			LOGGER.error(e.getMessage());
		} catch (IOException e) {
			LOGGER.error(e.getMessage());
		} finally {
			try {
				reader.close();
			} catch (Exception e) {
				LOGGER.debug(e.getMessage());
			}
		}

		return errorList;
	}

	private String normalizeFilePath(String path) {
		if (!sourceCodeDir.equals("")) {
			Path p = Paths.get(sourceCodeDir + "/" + path);
			p = p.normalize();
			path = "source/" + p.toString();
		} else if (path.startsWith("../")) {
			path = "source/" + path.replace("../", "");
		}
		return path;
	}

	private void saveViolations(VarvelError error) {
		InputFile inputFile = fileSystem.inputFile(
				fileSystem.predicates().and(fileSystem.predicates().hasRelativePath(error.getFile()),
				fileSystem.predicates().hasType(InputFile.Type.MAIN)));

		LOGGER.debug("inputFile null ? " + (inputFile == null));

		if (inputFile != null) {
			saveUniqueViolations(inputFile, 
					error.getLine(), 
					error.getSubtype().trim().concat("_").concat(error.getRank()),
					error.getDescription());
		} else {
			LOGGER.error("Not able to find a InputFile with " + error.getFile());
		}

	}

	private void saveUniqueViolations(InputFile inputFile, String line,
			String type, String description) {
		int lines = inputFile.lines();
		int lineNr = getLineAsInt(line, lines);

		NewIssue newIssue = context.newIssue().forRule(RuleKey.of(VarvelRulesDefinition.REPO_KEY, type));
		NewIssueLocation location = newIssue.newLocation().on(inputFile)
				.at(inputFile.selectLine(lineNr > 0 ? lineNr : 1))
				.message(description);

		newIssue.at(location);
		newIssue.save();
	}

	private int getLineAsInt(String line, int maxLine) {
		int lineNr = 0;
		if (line != null) {
			try {
				lineNr = Integer.parseInt(line);
				if (lineNr < 1) {
					lineNr = 1;
				} else if (lineNr > maxLine) { // https://jira.sonarsource.com/browse/SONAR-6792
					lineNr = maxLine;
				}
			} catch (java.lang.NumberFormatException nfe) {
				LOGGER.warn("Skipping invalid line number: {}", line);
				lineNr = -1;
			}
		}
		return lineNr;
	}
}
