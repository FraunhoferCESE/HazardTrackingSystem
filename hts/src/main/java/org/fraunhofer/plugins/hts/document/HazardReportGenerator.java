package org.fraunhofer.plugins.hts.document;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;

import org.apache.poi.xwpf.usermodel.Borders;
import org.apache.poi.xwpf.usermodel.BreakClear;
import org.apache.poi.xwpf.usermodel.BreakType;
import org.apache.poi.xwpf.usermodel.LineSpacingRule;
import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.TextAlignment;
import org.apache.poi.xwpf.usermodel.UnderlinePatterns;
import org.apache.poi.xwpf.usermodel.VerticalAlign;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableCell.XWPFVertAlign;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.fraunhofer.plugins.hts.db.Hazard_Group;
import org.fraunhofer.plugins.hts.db.Hazards;
import org.fraunhofer.plugins.hts.db.Review_Phases;
import org.fraunhofer.plugins.hts.db.Risk_Categories;
import org.fraunhofer.plugins.hts.db.Risk_Likelihoods;
import org.fraunhofer.plugins.hts.db.Subsystems;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTblWidth;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STTblWidth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

public class HazardReportGenerator {

	private final String defaultFontFamily = "Arial";
	private final int defaultFontSize = 8;
	private final ParagraphAlignment defaultParagraphAlignment = ParagraphAlignment.LEFT;

	private static final Logger log = LoggerFactory.getLogger(HazardReportGenerator.class);

	DateFormat df = new SimpleDateFormat("MM/dd/yyyy");

	public List<File> createWordDocuments(List<Hazards> hazardList, List<Review_Phases> reviewPhases,
			List<Risk_Categories> testRiskCategories, List<Risk_Likelihoods> testRiskLikelihoods, File outputDirectory,
			boolean separateFiles) throws IOException {

		checkNotNull(outputDirectory, "Output directory for hazard documents is null");
		if (hazardList == null || hazardList.isEmpty())
			return null;

		List<File> results = Lists.newArrayList();

		if (separateFiles) {
			for (Hazards h : hazardList) {
				XWPFDocument doc = new XWPFDocument();
				createContentForHazard(doc, h, reviewPhases, testRiskCategories, testRiskLikelihoods);

				File reportFile = new File(outputDirectory + File.separator + h.getHazardNum() + ".docx");
				log.info("Writing hazard report to " + reportFile.getAbsolutePath());

				FileOutputStream out = new FileOutputStream(reportFile);
				doc.write(out);
				out.close();
				results.add(reportFile);
			}

		}

		// TODO: Return list of temp files
		return null;
	}

	private void createContentForHazard(XWPFDocument doc, Hazards h, List<Review_Phases> reviewPhases, List<Risk_Categories> testRiskCategories, List<Risk_Likelihoods> testRiskLikelihoods) {
		createHeader(doc, h, reviewPhases);
		createHazardDescription(doc, h, testRiskCategories, testRiskLikelihoods);
	}

	private void createHazardDescription(XWPFDocument doc, Hazards h, List<Risk_Categories> testRiskCategories, List<Risk_Likelihoods> testRiskLikelihoods) {
		XWPFTable top = doc.createTable(1, 1);

		XWPFTableCell cell;

		// "Hazard"
		XWPFTableRow _1stRow = top.getRow(0);
		cell = _1stRow.getCell(0);
		cell.setColor("BBBBBB");
		cell.setVerticalAlignment(XWPFVertAlign.CENTER);
		setGridSpan(cell, 3);
		new CellHeaderBuilder().text("Hazard").bold().alignment(ParagraphAlignment.CENTER).beforeSpacing(50)
				.createCellHeader(cell);

		// --------------------------------------
		
		// HAzard Title
		XWPFTableRow _2ndRow = doc.createTable(1, 2).getRow(0);
		cell = _2ndRow.getCell(0);
		setGridSpan(cell, 2);
		new CellHeaderBuilder().text("9. Hazard title:").createCellHeader(cell);
		new ParagraphBuilder().text(h.getTitle()).createCellText(cell);
		
		// Hazard category and risk likelihood
		cell = _2ndRow.getCell(1);
		new CellHeaderBuilder().text("10. Hazard Category and risk Likelihood:").createCellHeader(cell);
		for (Risk_Categories category : testRiskCategories) {
			//TODO: left off here
		}
		
	}

	private void createHeader(XWPFDocument doc, Hazards h, List<Review_Phases> reviewPhases) {
		XWPFTable top = doc.createTable(1, 2);
		setWidth(top, 730350);

		XWPFTableRow _1stRow = top.getRow(0);

		XWPFTableCell cell;

		// "Payload Hazard Report"
		cell = _1stRow.getCell(0);
		cell.setVerticalAlignment(XWPFVertAlign.CENTER);
		setGridSpan(cell, 2);
		new ParagraphBuilder().text("NASA Expendable Launch Vehicle (ELV)").bold(true).fontSize(14)
				.createCellText(cell);
		new ParagraphBuilder().text("Payload Safety Hazard Report").bold(true).fontSize(14).createCellText(cell);
		new ParagraphBuilder().text("(NPR 8715.7 and NASA-STD 8719.24)").fontSize(8).createCellText(cell);

		// Hazard report number and initiation date.
		// XXX: Can't do row spans, so these "two" cells are actually one cell
		// with a paragraph border.
		cell = _1stRow.getCell(1);
		new CellHeaderBuilder().text("1. Hazard Report #:").createCellHeader(cell);
		new ParagraphBuilder().text(h.getHazardNum()).bold(true).alignment(ParagraphAlignment.CENTER).bottomBorder()
				.createCellText(cell);

		new CellHeaderBuilder().text("2. Initiation Date: ").createCellHeader(cell);
		new ParagraphBuilder().text(df.format(h.getInitiationDate())).createCellText(cell);

		// --------------------------------------
		XWPFTableRow _2ndRow = doc.createTable(1, 2).getRow(0);

		// Payload and Payload Safety Engineer
		cell = _2ndRow.getCell(0);
		setWidth(cell, 5000);
		setGridSpan(cell, 2);

		new CellHeaderBuilder().text("3. Mission/Payload Project Name:").createCellHeader(cell);
		new ParagraphBuilder().text(h.getMissionPayload().getName()).bottomBorder().createCellText(cell);

		new CellHeaderBuilder().text("Payload System Safety Engineer:").createCellHeader(cell);
		new ParagraphBuilder().text(h.getPreparer()).createCellText(cell);

		// Review Phase
		cell = _2ndRow.getCell(1);
		new CellHeaderBuilder().text("4. Review Phase: ").createCellHeader(cell);

		for (Review_Phases phase : reviewPhases) {
			if (phase.getID() == h.getReviewPhase().getID())
				new ParagraphBuilder().text("\u2612\t\t" + phase.getLabel()).leftMargin(100).createCellText(cell);
			else
				new ParagraphBuilder().text("\u2610\t\t" + phase.getLabel()).leftMargin(100).createCellText(cell);
		}

		// --------------------------------------
		XWPFTableRow _3rdRow = doc.createTable(1, 3).getRow(0);

		// Subsystems
		cell = _3rdRow.getCell(0);
		new CellHeaderBuilder().text("5. System/Subsystem: ").createCellHeader(cell);
		for (Subsystems s : h.getSubsystems()) {
			new ParagraphBuilder().text(s.getLabel()).createCellText(cell);
		}

		// Hazard groups
		cell = _3rdRow.getCell(1);
		new CellHeaderBuilder().text("6. Hazard Group(s): ").createCellHeader(cell);
		for (Hazard_Group g : h.getHazardGroups()) {
			new ParagraphBuilder().text(g.getLabel()).createCellText(cell);
		}

		// Date
		cell = _3rdRow.getCell(2);
		new CellHeaderBuilder().text("7. Date: ").createCellHeader(cell);
		new ParagraphBuilder().text(df.format(h.getRevisionDate())).createCellText(cell);

		// --------------------------------------
		XWPFTableRow _4thRow = doc.createTable(1, 1).getRow(0);

		// Applicable Safety Requirements
		cell = _4thRow.getCell(0);
		setGridSpan(cell, 3);

		new CellHeaderBuilder().text("8. Applicable Safety Requirements: ").createCellHeader(cell);
		new ParagraphBuilder().text("NOT YET AVAILABLE").createCellText(cell);

	}

	private class ParagraphBuilder {

		private ParagraphAlignment alignment = defaultParagraphAlignment;
		private boolean isBold = false;
		private String fontFamily = defaultFontFamily;
		private int fontSize = defaultFontSize;
		private String text = "";
		private boolean bottomBorder = false;
		private int leftMargin = 0;

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
			paragraph.setIndentationLeft(leftMargin);
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

	}

	private class CellHeaderBuilder {
		private String text;
		private boolean isBold = false;
		private ParagraphAlignment alignment = ParagraphAlignment.LEFT;
		private int beforeSpacing = 30;

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
		}

		public CellHeaderBuilder beforeSpacing(int beforeSpacing) {
			this.beforeSpacing = beforeSpacing;
			return this;
		}
	}

	private void setWidth(XWPFTable table, long val) {
		CTTblWidth width = table.getCTTbl().addNewTblPr().addNewTblW();
		width.setType(STTblWidth.DXA);
		width.setW(BigInteger.valueOf(val));

	}

	private void setWidth(XWPFTableCell cell, long val) {
		cell.getCTTc().addNewTcPr().addNewTcW().setW(BigInteger.valueOf(val));
	}

	private void setGridSpan(XWPFTableCell cell, int val) {
		if (cell.getCTTc().getTcPr() == null)
			cell.getCTTc().addNewTcPr();
		if (cell.getCTTc().getTcPr().getGridSpan() == null)
			cell.getCTTc().getTcPr().addNewGridSpan();
		cell.getCTTc().getTcPr().getGridSpan().setVal(BigInteger.valueOf(val));
	}

	public void create() throws IOException {
		XWPFDocument doc = new XWPFDocument();

		XWPFParagraph p1 = doc.createParagraph();
		p1.setAlignment(ParagraphAlignment.CENTER);
		p1.setBorderBottom(Borders.DOUBLE);
		p1.setBorderTop(Borders.DOUBLE);

		p1.setBorderRight(Borders.DOUBLE);
		p1.setBorderLeft(Borders.DOUBLE);
		p1.setBorderBetween(Borders.SINGLE);

		p1.setVerticalAlignment(TextAlignment.TOP);

		XWPFRun r1 = p1.createRun();
		r1.setBold(true);
		r1.setText("The quick brown fox");
		r1.setBold(true);
		r1.setFontFamily("Courier");
		r1.setUnderline(UnderlinePatterns.DOT_DOT_DASH);
		r1.setTextPosition(100);

		XWPFParagraph p2 = doc.createParagraph();
		p2.setAlignment(ParagraphAlignment.RIGHT);

		// BORDERS
		p2.setBorderBottom(Borders.DOUBLE);
		p2.setBorderTop(Borders.DOUBLE);
		p2.setBorderRight(Borders.DOUBLE);
		p2.setBorderLeft(Borders.DOUBLE);
		p2.setBorderBetween(Borders.SINGLE);

		XWPFRun r2 = p2.createRun();
		r2.setText("jumped over the lazy dog");
		r2.setStrike(true);
		r2.setFontSize(20);

		XWPFRun r3 = p2.createRun();
		r3.setText("and went away");
		r3.setStrike(true);
		r3.setFontSize(20);
		r3.setSubscript(VerticalAlign.SUPERSCRIPT);

		XWPFParagraph p3 = doc.createParagraph();
		p3.setWordWrap(true);
		p3.setPageBreak(true);

		// p3.setAlignment(ParagraphAlignment.DISTRIBUTE);
		p3.setAlignment(ParagraphAlignment.BOTH);
		p3.setSpacingLineRule(LineSpacingRule.EXACT);

		p3.setIndentationFirstLine(600);

		XWPFRun r4 = p3.createRun();
		r4.setTextPosition(20);
		r4.setText("To be, or not to be: that is the question: " + "Whether 'tis nobler in the mind to suffer "
				+ "The slings and arrows of outrageous fortune, " + "Or to take arms against a sea of troubles, "
				+ "And by opposing end them? To die: to sleep; ");
		r4.addBreak(BreakType.PAGE);
		r4.setText("No more; and by a sleep to say we end " + "The heart-ache and the thousand natural shocks "
				+ "That flesh is heir to, 'tis a consummation " + "Devoutly to be wish'd. To die, to sleep; "
				+ "To sleep: perchance to dream: ay, there's the rub; " + ".......");
		r4.setItalic(true);
		// This would imply that this break shall be treated as a simple line
		// break, and break the line after that word:

		XWPFRun r5 = p3.createRun();
		r5.setTextPosition(-10);
		r5.setText("For in that sleep of death what dreams may come");
		r5.addCarriageReturn();
		r5.setText("When we have shuffled off this mortal coil," + "Must give us pause: there's the respect"
				+ "That makes calamity of so long life;");
		r5.addBreak();
		r5.setText("For who would bear the whips and scorns of time,"
				+ "The oppressor's wrong, the proud man's contumely,");

		r5.addBreak(BreakClear.ALL);
		r5.setText("The pangs of despised love, the law's delay," + "The insolence of office and the spurns"
				+ ".......");

	}

}
