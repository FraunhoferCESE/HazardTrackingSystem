package org.fraunhofer.plugins.hts.document;

public class RunBuilder {
	
	public boolean isBold;
	public String text;
	public String fontFamily = "Arial";
	public int fontSize = 8;
	public boolean strikethrough = false;
	RunBuilder() {}
	
	public RunBuilder strikethrough() {
		this.strikethrough = true;
		return this;
	}
	public RunBuilder text(String text) {
		this.text = text;
		return this;
	}
	
	public RunBuilder bold(boolean isBold) {
		this.isBold = isBold;
		return this;
	}
	
	public RunBuilder fontSize(int fontSize) {
		this.fontSize = fontSize;
		return this;
	}
	
}
