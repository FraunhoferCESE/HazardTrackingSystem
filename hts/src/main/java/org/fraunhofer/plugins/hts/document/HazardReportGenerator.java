package org.fraunhofer.plugins.hts.document;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
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
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.fraunhofer.plugins.hts.db.Hazard_Group;
import org.fraunhofer.plugins.hts.db.Hazards;
import org.fraunhofer.plugins.hts.db.Subsystems;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTblWidth;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STTblWidth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

public class HazardReportGenerator {
	private static final Logger log = LoggerFactory.getLogger(HazardReportGenerator.class);

	public List<File> createWordDocuments(List<Hazards> hazardList, File outputDirectory, boolean separateFiles)
			throws IOException {

		checkNotNull(outputDirectory, "Output directory for hazard documents is null");
		if (hazardList == null || hazardList.isEmpty())
			return null;

		List<File> results = Lists.newArrayList();

		if (separateFiles) {
			for (Hazards h : hazardList) {
				XWPFDocument doc = new XWPFDocument();
				createContentForHazard(doc, h);

				File reportFile = new File(outputDirectory + File.separator + h.getHazardNum() + ".docx");
				log.info("Writing hazard report to " + reportFile.getAbsolutePath());
				System.err.println(reportFile.getAbsolutePath());

				FileOutputStream out = new FileOutputStream(reportFile);
				doc.write(out);
				out.close();
				results.add(reportFile);
			}

		}

		return null;
	}

	private void createContentForHazard(XWPFDocument doc, Hazards h) {
		createHeader(doc, h);
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

	private void createHeader(XWPFDocument doc, Hazards h) {
		XWPFTable top = doc.createTable(1,2);
		setWidth(top, 1000000);
		
		XWPFTableRow _1stRow = top.getRow(0);

		// "Payload Hazard Report"
		XWPFParagraph docTitle = _1stRow.getCell(0).getParagraphs().get(0);
		setGridSpan(_1stRow.getCell(0), 2);
		XWPFRun rDocTitle = docTitle.createRun();
		rDocTitle.setBold(true);
		rDocTitle.setText("Payload Hazard Report");
		rDocTitle.setFontSize(16);
		docTitle.setAlignment(ParagraphAlignment.CENTER);

		// Hazard report number
		XWPFParagraph hrNum = _1stRow.getCell(1).getParagraphs().get(0);
		XWPFRun rHRNum = hrNum.createRun();
		rHRNum.setBold(true);
		rHRNum.setText(h.getHazardNum());
		rHRNum.setFontSize(14);
		hrNum.setAlignment(ParagraphAlignment.CENTER);

		// --------------------------------------
		XWPFTableRow _2ndRow = doc.createTable(1, 3).getRow(0);

		// Payload
		createCellHeader(_2ndRow.getCell(0), "Payload");
		setWidth(_2ndRow.getCell(0), 5000);
		_2ndRow.getCell(0).addParagraph().createRun().setText(h.getMissionPayload().getName());

		// Initiation date
		createCellHeader(_2ndRow.getCell(1), "Initiation Date");
		_2ndRow.getCell(1).addParagraph().createRun().setText(h.getInitiationDate().toString());

		// Last revision date
		createCellHeader(_2ndRow.getCell(2), "Last Revision");
		_2ndRow.getCell(2).addParagraph().createRun().setText(h.getRevisionDate().toString());

		// --------------------------------------
		XWPFTableRow _3rdRow = doc.createTable(1, 3).getRow(0);

		createCellHeader(_3rdRow.getCell(0), "Subsystems");
		XWPFParagraph subsystems = _3rdRow.getCell(0).addParagraph();
		XWPFRun subsystem;
		for (Subsystems s : h.getSubsystems()) {
			subsystem = subsystems.createRun();
			subsystem.setText(s.getLabel());
			subsystem.addBreak();
		}

		// Hazard Group
		createCellHeader(_3rdRow.getCell(1), "Hazard Groups");
		XWPFParagraph hazard_groups = _3rdRow.getCell(1).addParagraph();
		XWPFRun group;
		for (Hazard_Group g : h.getHazardGroups()) {
			group = hazard_groups.createRun();
			group.setText(g.getLabel());
			group.addBreak();
		}

		// Review Phase
		createCellHeader(_3rdRow.getCell(2), "Review Phase");
		_3rdRow.getCell(2).addParagraph().createRun().setText(h.getReviewPhase().getLabel());

		// --------------------------------------
		XWPFTableRow _4thRow = doc.createTable(1, 1).getRow(0);

		// Title
		setGridSpan(_4thRow.getCell(0), 3);
		createCellHeader(_4thRow.getCell(0), "Title");
		_4thRow.getCell(0).addParagraph().createRun().setText(h.getTitle());

	}

	private void createCellHeader(XWPFTableCell cell, String text) {
		XWPFParagraph p = cell.getParagraphs().get(0);
		XWPFRun rHeading = p.createRun();
		rHeading.setBold(true);
		rHeading.setText(text);
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
