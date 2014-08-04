package org.fraunhofer.plugins.hts.document;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;

import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTable.XWPFBorderType;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableCell.XWPFVertAlign;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.apache.xmlbeans.XmlException;
import org.fraunhofer.plugins.hts.db.Hazard_Causes;
import org.fraunhofer.plugins.hts.db.Hazard_Group;
import org.fraunhofer.plugins.hts.db.Hazards;
import org.fraunhofer.plugins.hts.db.Review_Phases;
import org.fraunhofer.plugins.hts.db.Risk_Categories;
import org.fraunhofer.plugins.hts.db.Risk_Likelihoods;
import org.fraunhofer.plugins.hts.db.Subsystems;
import org.fraunhofer.plugins.hts.db.Transfers;
import org.fraunhofer.plugins.hts.db.service.HazardCauseService;
import org.fraunhofer.plugins.hts.db.service.HazardService;
import org.fraunhofer.plugins.hts.db.service.TransferService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

public class HazardReportGenerator {

	private static final Logger log = LoggerFactory.getLogger(HazardReportGenerator.class);

	// XXX: Need to refactor the data structures so that this class is not
	// dependent on the service implementations
	private final HazardService hazardService;
	private final HazardCauseService causeService;
	private final TransferService transferService;

	DateFormat df = new SimpleDateFormat("MM/dd/yyyy");

	// XXX: Cannot create a footer from scratch per
	// https://issues.apache.org/bugzilla/show_bug.cgi?id=53009. Have to use a
	// workaround of creating a "template" doc and then basing hazards off that.

	// TODO: This needs to be changed prior to deployment, most likely
	final File template = new File(System.getProperty("user.dir") + System.getProperty("file.separator") + "src"
			+ System.getProperty("file.separator") + "main" + System.getProperty("file.separator") + "resources"
			+ System.getProperty("file.separator") + "Template.docx");

	public HazardReportGenerator(HazardService hazardService, HazardCauseService causeService,
			TransferService transferService) {
		this.hazardService = hazardService;
		this.causeService = causeService;
		this.transferService = transferService;
	}

	public List<File> createWordDocuments(List<Hazards> hazardList, List<Review_Phases> reviewPhases,
			List<Risk_Categories> riskCategories, List<Risk_Likelihoods> riskLikelihoods, File outputDirectory,
			boolean separateFiles) throws IOException, XmlException {

		checkNotNull(outputDirectory, "Output directory for hazard documents is null");
		if (hazardList == null || hazardList.isEmpty())
			return null;

		List<File> results = Lists.newArrayList();

		if (separateFiles) {
			for (Hazards h : hazardList) {
				File reportFile = new File(outputDirectory + File.separator + h.getHazardNum() + ".docx");
				FileOutputStream out = new FileOutputStream(reportFile);

				XWPFDocument doc = new XWPFDocument();

				FileInputStream in = new FileInputStream(template);
				doc = new XWPFDocument(in);
				in.close();

				createContentForHazard(doc, h, reviewPhases, riskCategories, riskLikelihoods);

				doc.write(out);
				out.close();
				results.add(reportFile);
				log.info("Writing hazard report to " + reportFile.getAbsolutePath());
			}

		}

		// TODO: Return list of temp files
		return null;
	}

	private void createContentForHazard(XWPFDocument doc, Hazards h, List<Review_Phases> reviewPhases,
			List<Risk_Categories> testRiskCategories, List<Risk_Likelihoods> testRiskLikelihoods) throws IOException,
			XmlException {
		createHeader(doc, h, reviewPhases);
		createHazardDescription(doc, h, testRiskCategories, testRiskLikelihoods);
	}

//	private void createFooter(XWPFDocument doc, Hazards h) throws IOException, XmlException {
//		// http://stackoverflow.com/questions/16442347/counting-pages-in-a-word-document
//
//		XWPFHeaderFooterPolicy headerFooterPolicy = doc.getHeaderFooterPolicy();
//		headerFooterPolicy.createFooter(STHdrFtr.DEFAULT);
//		XWPFFooter footer = headerFooterPolicy.getDefaultFooter();
//		// new ParagraphBuilder().text("test").createFooterText(footer);
//		XmlDocumentProperties documentProperties = doc.getDocument().documentProperties();
//
//		doc.createNumbering();
//	}

	private void createHeader(XWPFDocument doc, Hazards h, List<Review_Phases> reviewPhases) {
		// Remove the default paragraph in Template.docx
		doc.removeBodyElement(0);
		
		XWPFTable top = new TableBuilder().setWidth(730350).size(1, 2).createTable(doc);
		XWPFTableRow row;
		XWPFTableCell cell;

		row = top.getRow(0);
		// "Payload Hazard Report"
		cell = row.getCell(0);
		cell.setVerticalAlignment(XWPFVertAlign.CENTER);
		setGridSpan(cell, 3);
		new ParagraphBuilder().text("NASA Expendable Launch Vehicle (ELV)").bold(true).fontSize(14)
				.createCellText(cell);
		new ParagraphBuilder().text("Payload Safety Hazard Report").bold(true).fontSize(14).createCellText(cell);
		new ParagraphBuilder().text("(NPR 8715.7 and NASA-STD 8719.24)").fontSize(8).createCellText(cell);

		// Hazard report number and initiation date.
		// XXX: Can't do row spans, so these "two" cells are actually one cell
		// with a paragraph border.
		cell = row.getCell(1);
		new CellHeaderBuilder().text("1. Hazard Report #:").createCellHeader(cell);
		new ParagraphBuilder().text(h.getHazardNum()).bold(true).fontSize(10).leftMargin(0).alignment(ParagraphAlignment.CENTER)
				.bottomBorder().createCellText(cell);

		new CellHeaderBuilder().text("2. Initiation Date: ").createCellHeader(cell);
		new ParagraphBuilder().text(df.format(h.getInitiationDate())).createCellText(cell);

		// --------------------------------------
		row = new TableBuilder().size(1, 2).createTable(doc).getRow(0);

		// Payload and Payload Safety Engineer
		cell = row.getCell(0);
		setWidth(cell, 5000);
		setGridSpan(cell, 3);

		new CellHeaderBuilder().text("3. Mission/Payload Project Name:").createCellHeader(cell);
		new ParagraphBuilder().text(h.getMissionPayload().getName()).leftMargin(0).bottomBorder().createCellText(cell);

		new CellHeaderBuilder().text("Payload System Safety Engineer:").createCellHeader(cell);
		new ParagraphBuilder().text(h.getPreparer()).createCellText(cell);

		// Review Phase
		cell = row.getCell(1);
		new CellHeaderBuilder().text("4. Review Phase: ").createCellHeader(cell);

		for (Review_Phases phase : reviewPhases) {
			if (phase.getID() == h.getReviewPhase().getID())
				new ParagraphBuilder().text("\u2612\t\t" + phase.getLabel()).leftMargin(100).createCellText(cell);
			else
				new ParagraphBuilder().text("\u2610\t\t" + phase.getLabel()).leftMargin(100).createCellText(cell);
		}

		// --------------------------------------
		row = new TableBuilder().size(1, 3).createTable(doc).getRow(0);

		// Subsystems
		cell = row.getCell(0);
		setGridSpan(cell, 2);
		new CellHeaderBuilder().text("5. System/Subsystem: ").createCellHeader(cell);
		for (Subsystems s : h.getSubsystems()) {
			new ParagraphBuilder().text(s.getLabel()).createCellText(cell);
		}

		// Hazard groups
		cell = row.getCell(1);
		new CellHeaderBuilder().text("6. Hazard Group(s): ").createCellHeader(cell);
		for (Hazard_Group g : h.getHazardGroups()) {
			new ParagraphBuilder().text(g.getLabel()).createCellText(cell);
		}

		// Date
		cell = row.getCell(2);
		new CellHeaderBuilder().text("7. Date: ").createCellHeader(cell);
		new ParagraphBuilder().text(df.format(h.getRevisionDate())).createCellText(cell);

		// --------------------------------------
		row = new TableBuilder().size(1, 1).createTable(doc).getRow(0);

		// TODO: Applicable Safety Requirements
		cell = row.getCell(0);
		setGridSpan(cell, 4);

		new CellHeaderBuilder().text("8. Applicable Safety Requirements: ").createCellHeader(cell);
		new ParagraphBuilder().text("N/A").createCellText(cell);
	}

	private void createHazardDescription(XWPFDocument doc, Hazards h, List<Risk_Categories> testRiskCategories,
			List<Risk_Likelihoods> testRiskLikelihoods) {

		XWPFTableRow row;
		XWPFTableCell cell;

		// "Hazard"
		row = new TableBuilder().size(1, 1).createTable(doc).getRow(0);
		cell = row.getCell(0);
		cell.setColor("BBBBBB");
		cell.setVerticalAlignment(XWPFVertAlign.CENTER);
		setGridSpan(cell, 4);
		new CellHeaderBuilder().text("Hazard").bold().alignment(ParagraphAlignment.CENTER).beforeSpacing(50)
				.createCellHeader(cell);

		// --------------------------------------

		// Headers for Hazard Title and Risk Categories/Likelihoods
		row = new TableBuilder().size(1, 2).setInnerHBorder(XWPFBorderType.NONE).createTable(doc).getRow(0);
		cell = row.getCell(0);
		setGridSpan(cell, 2);
		new CellHeaderBuilder().text("9. Hazard title:").createCellHeader(cell);

		cell = row.getCell(1);
		setGridSpan(cell, 2);
		new CellHeaderBuilder().text("10. Hazard Category and risk Likelihood:").createCellHeader(cell);

		row = new TableBuilder().size(1, 3).setInnerHBorder(XWPFBorderType.NONE).createTable(doc).getRow(0);
		// HAzard Title
		cell = row.getCell(0);
		setGridSpan(cell, 2);
		new ParagraphBuilder().text(h.getTitle()).createCellText(cell);

		// Hazard category and risk likelihood
		cell = row.getCell(1);
		for (Risk_Categories category : testRiskCategories) {
			if (category.getID() == h.getRiskCategory().getID())
				new ParagraphBuilder().text("\u2612  " + category.getValue()).fontSize(6).createCellText(cell);
			else
				new ParagraphBuilder().text("\u2610  " + category.getValue()).fontSize(6).createCellText(cell);
		}

		cell = row.getCell(2);
		for (Risk_Likelihoods likelihood : testRiskLikelihoods) {
			if (likelihood.getID() == h.getRiskLikelihood().getID())
				new ParagraphBuilder().text("\u2612  " + likelihood.getValue()).leftMargin(100).fontSize(6)
						.createCellText(cell);
			else
				new ParagraphBuilder().text("\u2610  " + likelihood.getValue()).leftMargin(100).fontSize(6)
						.createCellText(cell);

		}
		// --------------------------------------

		// Hazard description
		row = new TableBuilder().size(1, 1).createTable(doc).getRow(0);
		cell = row.getCell(0);
		setGridSpan(cell, 4);
		new CellHeaderBuilder().text("11. Description of hazard:").createCellHeader(cell);
		new ParagraphBuilder().text(h.getHazardDesc()).createCellText(cell);

		// --------------------------------------
		// Cause summary
		row = new TableBuilder().size(1, 1).createTable(doc).getRow(0);
		cell = row.getCell(0);
		setGridSpan(cell, 4);
		new CellHeaderBuilder().text("12. Hazard causes:").createCellHeader(cell);

		for (Hazard_Causes cause : h.getHazardCauses()) {
			if (cause.getTransfer() == 0)
				new ParagraphBuilder().text(cause.getCauseNumber() + " \u2013 " + cause.getTitle()).leftMargin(350)
						.hangingIndent(300).createCellText(cell);
			else
				printCauseTransfer(cell, cause);
		}

	}

	private void printCauseTransfer(XWPFTableCell cell, Hazard_Causes cause) {
		Transfers transfer = transferService.getTransferByID(cause.getTransfer());

		if (transfer.getTargetType().equals("HAZARD")) {
			Hazards hazard = hazardService.getHazardByID(Integer.toString(transfer.getTargetID()));
			new ParagraphBuilder()
					.text(cause.getCauseNumber() + " (TRANSFER) " + hazard.getHazardNum() + " \u2013 "
							+ hazard.getTitle()).leftMargin(350).hangingIndent(300).createCellText(cell);
		} else if (transfer.getTargetType().equals("CAUSE")) {
			Hazard_Causes targetCause = causeService.getHazardCauseByID(Integer.toString(transfer.getTargetID()));
			Hazards hazard = targetCause.getHazards()[0];
			new ParagraphBuilder()
					.text(cause.getCauseNumber() + " (TRANSFER) " + hazard.getHazardNum() + ","
							+ targetCause.getCauseNumber() + " \u2013 " + targetCause.getTitle()).leftMargin(350)
					.hangingIndent(300).createCellText(cell);
		}

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

}
