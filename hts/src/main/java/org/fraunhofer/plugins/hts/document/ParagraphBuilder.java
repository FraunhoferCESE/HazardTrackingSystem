package org.fraunhofer.plugins.hts.document;

import org.apache.poi.xwpf.usermodel.Borders;
import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;

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
	private int leftMargin = 50;
	private int topMargin = 0;
	private int hangingIndent = 0;

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
		paragraph.setAlignment(alignment);
		paragraph.setSpacingAfter(10);
		paragraph.setSpacingBefore(topMargin);
		paragraph.setIndentationLeft(leftMargin);
		paragraph.setIndentationRight(50);
		paragraph.setIndentationHanging(hangingIndent);
		if (bottomBorder)
			paragraph.setBorderBottom(Borders.SINGLE);
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
