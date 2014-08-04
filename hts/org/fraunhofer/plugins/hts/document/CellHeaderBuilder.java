package org.fraunhofer.plugins.hts.document;

public class CellHeaderBuilder {

	private String text;
	private boolean isBold = false;
	private ParagraphAlignment alignment = ParagraphAlignment.LEFT;
	private int beforeSpacing = 70;
	
	public CellHeaderBuilder() {
		 }

	public void createCellHeader(XWPFTableCell cell) {
		 			XWPFParagraph p;
		 			if (cell.getParagraphs().get(0).getRuns().size() == 0)
		 				p = cell.getParagraphs().get(0);
		 
			p = cell.addParagraph();
			 			p.setAlignment(alignment);
			 			p.setSpacingBefore(beforeSpacing);
			 			p.setIndentationLeft(20);
			 			p.setSpacingAfter(100);
			 			XWPFRun rHeading = p.createRun();
			 			rHeading.setText(text.toUpperCase());
			 			rHeading.setFontFamily("Arial");
			 			rHeading.setFontSize(6);
			 			rHeading.setBold(isBold);
			 		}
			 
			 		public CellHeaderBuilder text(String text) {
			 			this.text = text;
			 			return this;
			 		}
			 
			 		public CellHeaderBuilder bold() {
			 			this.isBold = true;
			 			return this;
			 	}
			 
			 		public CellHeaderBuilder alignment(ParagraphAlignment alignment) {
			 			this.alignment = alignment;
			 			return this;
			 
			 public CellHeaderBuilder beforeSpacing(int beforeSpacing) {
							this.beforeSpacing = beforeSpacing;
							return this;
						}
		 
}
