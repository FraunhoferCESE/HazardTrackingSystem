package org.fraunhofer.plugins.hts.document;

import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;

public class CellHeaderBuilder {

	private String text;
	private int fontSize = 6;
	private boolean isBold = false;
	private ParagraphAlignment alignment = ParagraphAlignment.LEFT;
	private int beforeSpacing = 70;
	private int afterSpacing = 100;
	private boolean pageBreak = false;

	public CellHeaderBuilder() {
	}

	public void createCellHeader(XWPFTableCell cell) {
		XWPFParagraph p;
		if (cell.getParagraphs().get(0).getRuns().size() == 0)
			p = cell.getParagraphs().get(0);
		else
			p = cell.addParagraph();

		p.setAlignment(alignment);
		p.setSpacingBefore(beforeSpacing);
		p.setSpacingAfter(afterSpacing);
		p.setPageBreak(pageBreak);
		p.setIndentationLeft(20);
		XWPFRun rHeading = p.createRun();
		rHeading.setText(text.toUpperCase());
		rHeading.setFontFamily("Arial");
		rHeading.setFontSize(fontSize);
		rHeading.setBold(isBold);
	}

	public CellHeaderBuilder text(String text) {
		this.text = text;
		return this;
	}
	
	public CellHeaderBuilder fontSize(int size) {
		this.fontSize = size;
		return this;
	}

	public CellHeaderBuilder bold() {
		this.isBold = true;
		return this;
	}

	public CellHeaderBuilder alignment(ParagraphAlignment alignment) {
		this.alignment = alignment;
		return this;
	}

	public CellHeaderBuilder beforeSpacing(int beforeSpacing) {
		this.beforeSpacing = beforeSpacing;
		return this;
	}
	
	public CellHeaderBuilder afterSpacing(int afterSpacing) {
		this.afterSpacing = afterSpacing;
		return this;
	}
	
	public CellHeaderBuilder pageBreak(boolean hasPageBreak) {
		this.pageBreak = hasPageBreak;
		return this;
	}

}
