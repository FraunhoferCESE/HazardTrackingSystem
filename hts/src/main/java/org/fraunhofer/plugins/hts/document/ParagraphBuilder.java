package org.fraunhofer.plugins.hts.document;

import java.util.List;

import org.apache.poi.xwpf.usermodel.Borders;
import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;

import com.google.common.collect.Lists;

/**
 * Helper class which for generating and formatting the Paragraph elements of a
 * .docx document.
 * 
 * This class follows the Builder pattern - {@link http://www.javaworld.com/article/2074938/core-java/too-many-parameters-in-java-methods-part-3-builder-pattern.html}
 * 
 * @author llayman
 *
 */
public class ParagraphBuilder {

	private ParagraphAlignment alignment = ParagraphAlignment.LEFT;
	private boolean isBold = false;
	private String fontFamily = "Arial";
	private int fontSize = 8;
	private String text = "";
	private boolean bottomBorder = false;
	private boolean topBorder = false;
	private int leftMargin = 50;
	private int topMargin = 0;
	private int hangingIndent = 0;
	private Borders borderType = Borders.SINGLE;
	private boolean strikethrough = false;
	private List<RunBuilder> customRuns = Lists.newArrayList();

	public ParagraphBuilder() {
	}

	public void createCellText(XWPFTableCell cell) {
		XWPFParagraph paragraph;
		// If no header is set, use the cell's default paragraph.
		if (cell.getParagraphs().get(0).getRuns().size() == 0)
			paragraph = cell.getParagraphs().get(0);
		else
			paragraph = cell.addParagraph();

		XWPFRun run = paragraph.createRun();
		run.setBold(isBold);
		run.setText(text);
		run.setFontFamily(fontFamily);
		run.setFontSize(fontSize);
		run.setStrike(strikethrough);
		
		for (RunBuilder xwpfRun : customRuns) {
			XWPFRun newrun = paragraph.createRun();
			newrun.setBold(xwpfRun.isBold);
			newrun.setText(xwpfRun.text);
			newrun.setFontFamily(xwpfRun.fontFamily);
			newrun.setFontSize(xwpfRun.fontSize);
			newrun.setStrike(xwpfRun.strikethrough);
		}
		
		paragraph.setAlignment(alignment);
		paragraph.setSpacingAfter(10);
		paragraph.setSpacingBefore(topMargin);
		paragraph.setIndentationLeft(leftMargin);
		paragraph.setIndentationRight(50);
		paragraph.setIndentationHanging(hangingIndent);
		if (bottomBorder)
			paragraph.setBorderBottom(borderType);
		if(topBorder)
			paragraph.setBorderTop(borderType);
	}

	public ParagraphBuilder addRun(RunBuilder run) {
		this.customRuns.add(run);
		return this;
	}
	
	public ParagraphBuilder borderType(Borders borderType) {
		this.borderType = borderType;
		return this;
	}
	
	public ParagraphBuilder strikethrough() {
		this.strikethrough = true;
		return this;
	}
	public ParagraphBuilder text(String text) {
		this.text = text;
		return this;
	}

	public ParagraphBuilder alignment(ParagraphAlignment _alignment) {
		this.alignment = _alignment;
		return this;
	}

	public ParagraphBuilder bold(boolean isBold) {
		this.isBold = isBold;
		return this;
	}

	public ParagraphBuilder fontSize(int fontSize) {
		this.fontSize = fontSize;
		return this;
	}

	public ParagraphBuilder bottomBorder() {
		this.bottomBorder = true;
		return this;
	}
	
	public ParagraphBuilder topBorder(boolean topBorder) {
		this.topBorder = topBorder;
		return this;
	}

	public ParagraphBuilder leftMargin(int margin) {
		this.leftMargin = margin;
		return this;
	}

	public ParagraphBuilder hangingIndent(int hangingIndent) {
		this.hangingIndent = hangingIndent;
		return this;
	}
	
	public ParagraphBuilder topMargin(int topMargin) {
		this.topMargin = topMargin;
		return this;
	}

}
