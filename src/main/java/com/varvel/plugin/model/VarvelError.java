package com.varvel.plugin.model;

public class VarvelError {
	private String rank;
	private String subtype;
	private String file;
	private String line;
	private String description;


	public VarvelError() {
	}

	public VarvelError(String rank, String subtype, String file, String line,
			String description) {
		this.rank = rank;
		this.subtype = subtype;
		this.file = file;
		this.line = line;
		this.description = description;
	}


	public String getRank() {
		return rank;
	}

	public void setRank(String rank) {
		this.rank = rank;
	}

	public String getSubtype() {
		return subtype;
	}

	public void setSubtype(String subtype) {
		this.subtype = subtype;
	}

	public String getFile() {
		return file;
	}

	public void setFile(String file) {
		this.file = file;
	}

	public String getLine() {
		return line;
	}

	public void setLine(String line) {
		this.line = line;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public String toString() {
		return "VarvelError [rank=" + rank + ", type=" + subtype + ", file="
				+ file + ", line=" + line + ", description=" + description
				+ "]";
	}
}
