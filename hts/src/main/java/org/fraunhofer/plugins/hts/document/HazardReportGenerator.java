package org.fraunhofer.plugins.hts.document;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.poi.POIXMLDocumentPart;
import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFSettings;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTable.XWPFBorderType;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableCell.XWPFVertAlign;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.apache.xmlbeans.XmlException;
import org.fraunhofer.plugins.hts.model.CauseNumberComparator;
import org.fraunhofer.plugins.hts.model.ControlGroups;
import org.fraunhofer.plugins.hts.model.ControlNumberComparator;
import org.fraunhofer.plugins.hts.model.Hazard_Causes;
import org.fraunhofer.plugins.hts.model.Hazard_Controls;
import org.fraunhofer.plugins.hts.model.Hazard_Group;
import org.fraunhofer.plugins.hts.model.Hazards;
import org.fraunhofer.plugins.hts.model.Review_Phases;
import org.fraunhofer.plugins.hts.model.Risk_Categories;
import org.fraunhofer.plugins.hts.model.Risk_Likelihoods;
import org.fraunhofer.plugins.hts.model.Subsystems;
import org.fraunhofer.plugins.hts.model.Transfers;
import org.fraunhofer.plugins.hts.model.VerificationNumberComparator;
import org.fraunhofer.plugins.hts.model.Verifications;
import org.fraunhofer.plugins.hts.service.CauseService;
import org.fraunhofer.plugins.hts.service.ControlService;
import org.fraunhofer.plugins.hts.service.HazardService;
import org.fraunhofer.plugins.hts.service.TransferService;
import org.fraunhofer.plugins.hts.service.VerificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;

/**
 * This class generates .docx files containing hazard reports. The format is
 * based on NASA ELV Payload Safety Hazard Report Instructions - NF 1825
 * {@link https
 * ://github.com/FraunhoferCESE/HazardTrackingSystem/wiki/HazardReportForm}
 * 
 */
public class HazardReportGenerator {

	private static final Logger log = LoggerFactory.getLogger(HazardReportGenerator.class);

	// XXX: Need to refactor the data structures so that this class is not
	// dependent on the service implementations
	private final HazardService hazardService;
	private final CauseService causeService;
	private final ControlService controlService;
	private final VerificationService verificationService;
	private final TransferService transferService;
	private final ProjectManager projectManager;

	private final DateFormat df = new SimpleDateFormat("MM/dd/yyyy");

	/**
	 * Constructor for instantiating the class. The class requires various
	 * services to be passed in order to retrieve information about the hazards,
	 * causes, and transfers from the database.
	 * 
	 * @param hazardService
	 * @param causeService
	 * @param transferService
	 */
	public HazardReportGenerator(HazardService hazardService, CauseService causeService,
			TransferService transferService, ProjectManager projectManager, ControlService controlService,
			VerificationService verificationService) {
		this.hazardService = hazardService;
		this.causeService = causeService;
		this.transferService = transferService;
		this.projectManager = projectManager;
		this.controlService = controlService;
		this.verificationService = verificationService;
	}

	/**
	 * Creates byte arrays containing Word .docx files representing hazards in a
	 * list.
	 * 
	 * @param hazardList
	 *            the list of {@link Hazards} objects for which the files will
	 *            be created.
	 * @param reviewPhases
	 *            a list of all unique {@link Review_Phases} in the order in
	 *            which they will be displayed.
	 * @param riskCategories
	 *            a list of all unique {@link Risk_Categories} in the order in
	 *            which they will be displayed.
	 * @param riskLikelihoods
	 *            a list of all unique {@link Risk_Likelihoods} in the order in
	 *            which they will be displayed.
	 * @param inputStream
	 *            - Word document template containing desired header and footer.
	 *            Cannot create a footer from scratch per
	 *            https://issues.apache.org/bugzilla/show_bug.cgi?id=53009.
	 * @return a List of byte[]. Each entry in the list is the byte[]
	 *         representation of a Word .docx file. One byte[] is generated per
	 *         hazard report. <code>null</code> is returned if the hazardList or
	 *         inputStream is empty or <code>null</code>
	 * @throws IOException
	 * @throws XmlException
	 */
	public List<byte[]> createWordDocument(List<Hazards> hazardList, List<Review_Phases> reviewPhases,
			List<Risk_Categories> riskCategories, List<Risk_Likelihoods> riskLikelihoods, InputStream inputStream)
					throws XmlException, IOException {

		if (hazardList == null || hazardList.isEmpty())
			return null;

		List<byte[]> results = Lists.newArrayList();
		try {
			for (Hazards hazard : hazardList) {
				// Create a new Word docx using Apache POI. The inputStream is a
				// .dot file that serves as a template. All hazard data is
				// subsequently added to this template in order to create the
				// full document.
				//
				// Given how POI works, any changes to the doc (e.g., adding
				// text) modify the existing object in memory. Thus, you do not
				// need to return the doc object, but merely make changes to it.
				XWPFDocument doc = new XWPFDocument(inputStream);

				// Set the default zoom to 125%
				List<POIXMLDocumentPart> relations = doc.getPart().getRelations();
				for (Iterator<POIXMLDocumentPart> iterator = relations.iterator(); iterator.hasNext();) {
					POIXMLDocumentPart docPart = iterator.next();
					if (docPart.getClass().getName().equals("org.apache.poi.xwpf.usermodel.XWPFSettings")) {
						XWPFSettings settings = (XWPFSettings) docPart;
						settings.setZoomPercent(125);
						break;
					}
				}

				// Add content to the document
				createHeader(doc, hazard, reviewPhases);
				createHazardDescription(doc, hazard, riskCategories, riskLikelihoods);
				createCauses(doc, hazard);
				printOrphanControls(doc, hazard);
				printOrphanVerifications(doc, hazard);

				// Create the return object
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				doc.write(out);
				results.add(out.toByteArray());
				log.info("Writing byte array for " + hazard.getHazardNumber());
			}
		} finally {
			if (inputStream != null)
				inputStream.close();
		}

		return results;
	}

	private void printOrphanVerifications(XWPFDocument doc, Hazards hazard) {
		List<Verifications> orphanVerifications = hazardService.getOrphanVerifications(hazard);

		if (!orphanVerifications.isEmpty()) {
			// "Orhpan Verifications"
			XWPFTable tableHeader = new TableBuilder().size(1, 1).createTable(doc);
			XWPFTableCell cell = tableHeader.getRow(0).getCell(0);
			cell.setColor("BBBBBB");
			cell.setVerticalAlignment(XWPFVertAlign.CENTER);
			// Set table width to page width
			cell.getCTTc().addNewTcPr().addNewTcW().setW(BigInteger.valueOf(730150));
			setColSpan(cell, 4);
			new CellHeaderBuilder().text("Orphan Verifications").bold().alignment(ParagraphAlignment.CENTER)
					.beforeSpacing(50).createCellHeader(cell);

			XWPFTable verificationsTable = new TableBuilder().size(1, 1).setInnerHBorder(XWPFBorderType.NONE)
					.createTable(doc);
			cell = verificationsTable.getRow(0).getCell(0);
			cell.getCTTc().addNewTcPr().addNewTcW().setW(BigInteger.valueOf(730150));
			setColSpan(cell, 4);

			printVerifications(doc, orphanVerifications.toArray(new Verifications[orphanVerifications.size()]),
					verificationsTable);
		}
	}

	private void printOrphanControls(XWPFDocument doc, Hazards hazard) {
		List<Hazard_Controls> orphanControls = hazardService.getOrphanControls(hazard);

		if (!orphanControls.isEmpty()) {
			// "Orhpan Controls"
			XWPFTable controlTableHeader = new TableBuilder().size(1, 1).createTable(doc);
			XWPFTableCell cell = controlTableHeader.getRow(0).getCell(0);
			cell.setColor("BBBBBB");
			cell.setVerticalAlignment(XWPFVertAlign.CENTER);
			// Set table width to page width
			cell.getCTTc().addNewTcPr().addNewTcW().setW(BigInteger.valueOf(730150));
			setColSpan(cell, 4);
			new CellHeaderBuilder().text("Orphan Controls").bold().alignment(ParagraphAlignment.CENTER)
					.beforeSpacing(50).createCellHeader(cell);

			XWPFTable controlsTable = new TableBuilder().size(1, 1).setInnerHBorder(XWPFBorderType.NONE)
					.createTable(doc);
			cell = controlsTable.getRow(0).getCell(0);
			cell.getCTTc().addNewTcPr().addNewTcW().setW(BigInteger.valueOf(730150));
			setColSpan(cell, 4);

			printControls(doc, orphanControls.toArray(new Hazard_Controls[orphanControls.size()]), controlsTable);
		}
	}

	// private void createFooter(XWPFDocument doc, Hazards h) throws
	// IOException, XmlException {
	// //
	// http://stackoverflow.com/questions/16442347/counting-pages-in-a-word-document
	//
	// XWPFHeaderFooterPolicy headerFooterPolicy = doc.getHeaderFooterPolicy();
	// headerFooterPolicy.createFooter(STHdrFtr.DEFAULT);
	// XWPFFooter footer = headerFooterPolicy.getDefaultFooter();
	// // new ParagraphBuilder().text("test").createFooterText(footer);
	// XmlDocumentProperties documentProperties =
	// doc.getDocument().documentProperties();
	//
	// doc.createNumbering();
	// }

	/**
	 * Adds the header section of NF1825 to the document. The header section
	 * contains hazard number, phases affected, author, etc.
	 * 
	 * @param doc
	 *            the template document that hazard data is being added to
	 * @param h
	 *            the hazard that is currently being written to the document
	 * @param reviewPhases
	 *            a list of ALL possible Review Phases in the system. This is
	 *            NOT the review phase for the hazard.
	 */
	private void createHeader(XWPFDocument doc, Hazards h, List<Review_Phases> reviewPhases) {
		// Remove the default paragraph in Template.docx
		doc.removeBodyElement(0);

		XWPFTable top = new TableBuilder().size(1, 2).createTable(doc);
		XWPFTableRow row;
		XWPFTableCell cell;

		row = top.getRow(0);
		// "Payload Hazard Report"
		cell = row.getCell(0);
		cell.setVerticalAlignment(XWPFVertAlign.CENTER);
		setColSpan(cell, 3);
		new ParagraphBuilder().text("NASA Expendable Launch Vehicle (ELV)").bold(true).fontSize(14)
				.createCellText(cell);
		new ParagraphBuilder().text("Payload Safety Hazard Report").bold(true).fontSize(14).createCellText(cell);
		new ParagraphBuilder().text("(NPR 8715.7 and NASA-STD 8719.24)").fontSize(8).createCellText(cell);

		// Hazard report number and initiation date.
		// Can't do row spans, so these "two" cells in the Payload Form are
		// actually one cell with a paragraph border.
		cell = row.getCell(1);
		new CellHeaderBuilder().text("1. Hazard Report #:").createCellHeader(cell);
		new ParagraphBuilder().text(h.getHazardNumber()).bold(true).fontSize(10).leftMargin(0)
				.alignment(ParagraphAlignment.CENTER).bottomBorder().createCellText(cell);

		new CellHeaderBuilder().text("2. Initiation Date: ").createCellHeader(cell);

		String date = h.getInitiationDate() != null ? df.format(h.getInitiationDate()) : "";
		new ParagraphBuilder().text(date).createCellText(cell);

		// --------------------------------------
		row = new TableBuilder().size(1, 2).createTable(doc).getRow(0);

		// Payload and Payload Safety Engineer
		cell = row.getCell(0);
		cell.getCTTc().addNewTcPr().addNewTcW().setW(BigInteger.valueOf(5000));
		setColSpan(cell, 3);

		new CellHeaderBuilder().text("3. Project Name:").createCellHeader(cell);
		Project projectObj = projectManager.getProjectObj(h.getProjectID());
		String hProjectName = projectObj == null ? "" : projectObj.getName();

		new ParagraphBuilder().text(hProjectName).bottomBorder().createCellText(cell);

		new CellHeaderBuilder().text("Payload System Safety Engineer:").createCellHeader(cell);
		new ParagraphBuilder().text(h.getPreparer()).createCellText(cell);

		// Review Phase
		cell = row.getCell(1);
		new CellHeaderBuilder().text("4. Review Phase: ").createCellHeader(cell);

		for (Review_Phases phase : reviewPhases) {
			if (h.getReviewPhase() != null) {
				if (phase.getID() == h.getReviewPhase().getID())
					new ParagraphBuilder().text("\u2612\t\t" + phase.getLabel()).leftMargin(100).createCellText(cell);
				else
					new ParagraphBuilder().text("\u2610\t\t" + phase.getLabel()).leftMargin(100).createCellText(cell);
			}
		}

		// --------------------------------------
		row = new TableBuilder().size(1, 3).createTable(doc).getRow(0);

		// Subsystems
		cell = row.getCell(0);
		setColSpan(cell, 2);
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
		String revisionDate = h.getRevisionDate() != null ? df.format(h.getRevisionDate()) : "";
		new ParagraphBuilder().text(revisionDate).createCellText(cell);

		// --------------------------------------
		row = new TableBuilder().size(1, 1).createTable(doc).getRow(0);

		// TODO: Applicable Safety Requirements
		cell = row.getCell(0);
		setColSpan(cell, 4);

		new CellHeaderBuilder().text("8. Applicable Safety Requirements: ").createCellHeader(cell);
		new ParagraphBuilder().text("N/A").createCellText(cell);
	}

	/**
	 * Adds the "HAZARD" section of NF1825 to the document.
	 * 
	 * @param doc
	 *            the template document that hazard data is being added to
	 * @param h
	 *            the hazard that is currently being written to the document
	 * @param riskCategories
	 *            a list of ALL possible Risk Categories in the system.
	 * @param riskLikelihoods
	 *            a list of ALL possible Risk Likelihoods in the system.
	 */
	private void createHazardDescription(XWPFDocument doc, Hazards h, List<Risk_Categories> riskCategories,
			List<Risk_Likelihoods> riskLikelihoods) {

		XWPFTableRow row;
		XWPFTableCell cell;

		// "Hazard"
		row = new TableBuilder().size(1, 1).createTable(doc).getRow(0);
		cell = row.getCell(0);
		cell.setColor("BBBBBB");
		cell.setVerticalAlignment(XWPFVertAlign.CENTER);
		// Set table width to page width
		cell.getCTTc().addNewTcPr().addNewTcW().setW(BigInteger.valueOf(730150));
		setColSpan(cell, 4);
		new CellHeaderBuilder().text("Hazard").bold().alignment(ParagraphAlignment.CENTER).beforeSpacing(50)
				.createCellHeader(cell);

		// --------------------------------------

		// Headers for Hazard Title and Risk Categories/Likelihoods
		row = new TableBuilder().size(1, 2).setInnerHBorder(XWPFBorderType.NONE).createTable(doc).getRow(0);
		cell = row.getCell(0);
		setColSpan(cell, 2);
		new CellHeaderBuilder().text("9. Hazard title:").createCellHeader(cell);

		cell = row.getCell(1);
		setColSpan(cell, 2);
		new CellHeaderBuilder().text("10. Hazard Category and risk Likelihood:").createCellHeader(cell);

		row = new TableBuilder().size(1, 3).setInnerHBorder(XWPFBorderType.NONE).createTable(doc).getRow(0);
		// HAzard Title
		cell = row.getCell(0);
		setColSpan(cell, 2);
		new ParagraphBuilder().text(h.getHazardTitle()).createCellText(cell);

		// TODO: There should be a risk matrix here with Causes and their info.
		// The commented code below is incorrectn, but may be useful later on
		// when drawing individual causes.
		// Hazard category and risk likelihood
		// cell = row.getCell(1);
		// for (Risk_Categories category : testRiskCategories) {
		// if (category.getID() == h.getRiskCategory().getID())
		// new ParagraphBuilder().text("\u2612 " +
		// category.getValue()).fontSize(6).createCellText(cell);
		// else
		// new ParagraphBuilder().text("\u2610 " +
		// category.getValue()).fontSize(6).createCellText(cell);
		// }
		//
		// cell = row.getCell(2);
		// for (Risk_Likelihoods likelihood : testRiskLikelihoods) {
		// if (likelihood.getID() == h.getRiskLikelihood().getID())
		// new ParagraphBuilder().text("\u2612 " +
		// likelihood.getValue()).leftMargin(100).fontSize(6)
		// .createCellText(cell);
		// else
		// new ParagraphBuilder().text("\u2610 " +
		// likelihood.getValue()).leftMargin(100).fontSize(6)
		// .createCellText(cell);
		//
		// }
		// --------------------------------------

		// Hazard description
		row = new TableBuilder().size(1, 1).createTable(doc).getRow(0);
		cell = row.getCell(0);
		setColSpan(cell, 4);
		new CellHeaderBuilder().text("11. Description of hazard:").createCellHeader(cell);
		new ParagraphBuilder().text(h.getHazardDescription()).createCellText(cell);

		// --------------------------------------
		// Cause summary
		row = new TableBuilder().size(1, 1).createTable(doc).getRow(0);
		cell = row.getCell(0);
		setColSpan(cell, 4);
		new CellHeaderBuilder().text("12. Hazard causes:").createCellHeader(cell);

		for (Hazard_Causes cause : h.getHazardCauses()) {
			if (cause.getTransfer() == 0)
				new ParagraphBuilder().text("Cause " + cause.getCauseNumber() + " \u2013 " + cause.getTitle())
						.leftMargin(350).hangingIndent(300).createCellText(cell);
			else
				printCauseTransfer(cell, cause, false);
		}

	}

	/**
	 * This method adds all of the causes for a hazard to the hazard report.
	 * 
	 * @param doc
	 *            the document to which the causes will be added
	 * @param hazard
	 *            the hazard we are printing
	 */
	private void createCauses(XWPFDocument doc, Hazards hazard) {
		XWPFTableRow row;
		XWPFTableCell cell;

		// "Causes"
		row = new TableBuilder().size(1, 1).createTable(doc).getRow(0);
		cell = row.getCell(0);
		cell.setColor("BBBBBB");
		cell.setVerticalAlignment(XWPFVertAlign.CENTER);
		// Set table width to page width
		cell.getCTTc().addNewTcPr().addNewTcW().setW(BigInteger.valueOf(730150));
		setColSpan(cell, 4);
		new CellHeaderBuilder().text("Causes").bold().alignment(ParagraphAlignment.CENTER).beforeSpacing(50)
				.createCellHeader(cell);

		// ----------------------------------

		Hazard_Causes[] causes = hazard.getHazardCauses();
		Arrays.sort(causes, new CauseNumberComparator());

		for (Hazard_Causes cause : causes) {
			if (Strings.isNullOrEmpty(cause.getDeleteReason())) {
				if (cause.getTransfer() == 0) {
					// Headers for Cause Number Risk Categories/likelihoods,
					// description, effects and additional safety features

					row = new TableBuilder().size(1, 1).setInnerHBorder(XWPFBorderType.SINGLE).createTable(doc)
							.getRow(0);
					cell = row.getCell(0);
					setColSpan(cell, 4);
					new ParagraphBuilder().text("Cause " + cause.getCauseNumber() + " - " + cause.getTitle())
							.topMargin(50).bold(true).leftMargin(350).hangingIndent(300).createCellText(cell);

					// Risk severities and likelihood
					row = new TableBuilder().size(1, 2).setInnerHBorder(XWPFBorderType.SINGLE).createTable(doc)
							.getRow(0);
					cell = row.getCell(0);
					setColSpan(cell, 1);

					String riskSeverity = cause.getRiskCategory() == null ? "<TBD>"
							: cause.getRiskCategory().getValue();
					new ParagraphBuilder().text("Risk Severity: " + riskSeverity).topMargin(50).leftMargin(350)
							.hangingIndent(300).createCellText(cell);

					cell = row.getCell(1);
					setColSpan(cell, 3);
					String riskLikelihood = cause.getRiskLikelihood() == null ? "<TBD>"
							: cause.getRiskLikelihood().getValue();
					new ParagraphBuilder().text("Risk Likelihood: " + riskLikelihood).topMargin(50).leftMargin(350)
							.hangingIndent(300).createCellText(cell);
					// Cause description

					row = new TableBuilder().size(1, 1).setInnerHBorder(XWPFBorderType.SINGLE).createTable(doc)
							.getRow(0);
					cell = row.getCell(0);
					setColSpan(cell, 4);
					new CellHeaderBuilder().text("Cause Description: ").bold().createCellHeader(cell);
					new ParagraphBuilder().text(cause.getDescription()).leftMargin(350).hangingIndent(300)
							.createCellText(cell);

					// cause effects
					row = new TableBuilder().size(1, 1).setInnerHBorder(XWPFBorderType.SINGLE).createTable(doc)
							.getRow(0);
					cell = row.getCell(0);
					setColSpan(cell, 4);
					new CellHeaderBuilder().text("Effects: ").bold().createCellHeader(cell);
					new ParagraphBuilder().text(cause.getEffects()).leftMargin(450).hangingIndent(300)
							.createCellText(cell);

					// additional Safety Features
					row = new TableBuilder().size(1, 1).setInnerHBorder(XWPFBorderType.SINGLE).createTable(doc)
							.getRow(0);
					cell = row.getCell(0);
					setColSpan(cell, 4);
					new CellHeaderBuilder().text("Additional Safety Features:").bold().createCellHeader(cell);
					new ParagraphBuilder().text(cause.getAdditionalSafetyFeatures()).leftMargin(350).hangingIndent(300)
							.createCellText(cell);
				} else {
					// Headers for Cause Number Risk Categories/likelihoods,
					// description, effects and additional safety features
					row = new TableBuilder().size(1, 1).setInnerHBorder(XWPFBorderType.SINGLE).createTable(doc)
							.getRow(0);
					cell = row.getCell(0);
					setColSpan(cell, 4);
					printCauseTransfer(cell, cause, true);

					// Risk severities and likelihood
					row = new TableBuilder().size(1, 2).setInnerHBorder(XWPFBorderType.SINGLE).createTable(doc)
							.getRow(0);
					cell = row.getCell(0);
					setColSpan(cell, 1);
					new ParagraphBuilder().text("Risk Severity: N/A").topMargin(50).leftMargin(350).hangingIndent(30)
							.createCellText(cell);

					cell = row.getCell(1);
					setColSpan(cell, 3);
					new ParagraphBuilder().text("Risk Likelihood: N/A").topMargin(50).leftMargin(350).hangingIndent(300)
							.createCellText(cell);
					// Cause description

					row = new TableBuilder().size(1, 1).setInnerHBorder(XWPFBorderType.SINGLE).createTable(doc)
							.getRow(0);
					cell = row.getCell(0);
					setColSpan(cell, 4);
					new CellHeaderBuilder().text("Transfer Reason: ").bold().createCellHeader(cell);
					new ParagraphBuilder().text(cause.getDescription()).leftMargin(450).hangingIndent(300)
							.createCellText(cell);
				}

				// Print out the controls for this cause
				// "Controls:"
				XWPFTable controlsTable = new TableBuilder().size(1, 1).setInnerHBorder(XWPFBorderType.NONE)
						.createTable(doc);
				row = controlsTable.getRow(0);
				cell = row.getCell(0);
				setColSpan(cell, 4);
				new CellHeaderBuilder().text("Controls: ").bold().afterSpacing(0).createCellHeader(cell);

				printControls(doc, cause.getControls(), controlsTable);

			}
		}
	}

	private void printControls(XWPFDocument doc, Hazard_Controls[] controls, XWPFTable parentTable) {
		Arrays.sort(controls, new ControlNumberComparator());

		if (controls.length == 0) {
			XWPFTableCell cell = parentTable.createRow().getCell(0);
			setColSpan(cell, 4);
			new ParagraphBuilder().text("None").leftMargin(450).topMargin(150).hangingIndent(400).createCellText(cell);
		} else {
			for (int i = 0; i < controls.length; i++) {
				Hazard_Controls control = controls[i];
				if (Strings.isNullOrEmpty(control.getDeleteReason())) {

					XWPFTableCell cell = parentTable.getNumberOfRows() == 1 ? parentTable.getRow(0).getCell(0)
							: parentTable.createRow().getCell(0);
					setColSpan(cell, 4);

					if (control.getTransfer() != 0) {
						Transfers transfer = transferService.getTransferByID(control.getTransfer());

						if (transfer.getTargetType().equals("CONTROL")) {
							Hazard_Controls targetControl = controlService.getHazardControlByID(transfer.getTargetID());
							Hazards targetHazard = targetControl.getHazard()[0];

							if (targetHazard.getProjectID() != control.getHazard()[0].getProjectID()) {
								String targetHazNum = Strings.isNullOrEmpty(targetHazard.getHazardNumber()) ? "N/A"
										: targetHazard.getHazardNumber();
								new ParagraphBuilder()
										.text("Control " + getControlNumber(control)
												+ " (TRANSFER) WARNING: This control transfers to a hazard not in the current project. Hazard number: "
												+ targetHazNum)
										.leftMargin(350).hangingIndent(300).topMargin(150).createCellText(cell);
							} else {

								String hazardTitle = targetHazard.getHazardTitle() == null ? "<TBD> "
										: targetHazard.getHazardTitle();
								String hazardNumber = targetHazard.getHazardNumber() == null ? "<TBD> "
										: targetHazard.getHazardNumber();

								ParagraphBuilder controlTitle = new ParagraphBuilder()
										.text("Control " + getControlNumber(control) + " (TRANSFER): ").leftMargin(350)
										.hangingIndent(300).topMargin(150);

								RunBuilder run = new RunBuilder().text(hazardNumber + " \u2013 " + hazardTitle
										+ ", Control " + getControlNumber(targetControl));
								if (!targetHazard.getActive()
										|| !Strings.isNullOrEmpty(targetControl.getDeleteReason())) {
									run = run.strikethrough();
								}
								controlTitle.addRun(run).createCellText(cell);
							}

						} else if (transfer.getTargetType().equals("CAUSE")) {
							Hazard_Causes targetCause = causeService.getHazardCauseByID(transfer.getTargetID());
							Hazards targetHazard = targetCause.getHazards()[0];

							if (targetHazard.getProjectID() != control.getHazard()[0].getProjectID()) {
								String targetHazNum = Strings.isNullOrEmpty(targetHazard.getHazardNumber()) ? "N/A"
										: targetHazard.getHazardNumber();
								new ParagraphBuilder()
										.text("Control " + getControlNumber(control)
												+ " (TRANSFER) WARNING: This control transfers to a hazard not in the current project. Hazard number: "
												+ targetHazNum)
										.leftMargin(350).hangingIndent(300).topMargin(150).createCellText(cell);
							} else {
								String hazardNumber = targetHazard.getHazardNumber() == null ? "<TBD> "
										: targetHazard.getHazardNumber();
								ParagraphBuilder controlTitle = new ParagraphBuilder()
										.text("Control " + getControlNumber(control) + " (TRANSFER): ").leftMargin(350)
										.hangingIndent(300).topMargin(150);
								RunBuilder run = new RunBuilder().text(hazardNumber + ", Cause "
										+ targetCause.getCauseNumber() + " \u2013 " + targetCause.getTitle());
								if (!targetHazard.getActive()
										|| !Strings.isNullOrEmpty(targetCause.getDeleteReason())) {
									run = run.strikethrough();
								}
								controlTitle.addRun(run).createCellText(cell);
							}
						}
					} else {
						ControlGroups controlGroup = control.getControlGroup();
						if (controlGroup != null) {
							controlGroup.getLabel();

							new ParagraphBuilder()
									.text("Control " + getControlNumber(control) + " (" + controlGroup.getLabel() + ")"
											+ " - " + control.getDescription())
									.topMargin(150).leftMargin(450).hangingIndent(400).createCellText(cell);
						} else {
							new ParagraphBuilder()
									.text("Control " + getControlNumber(control) + " - " + control.getDescription())
									.topMargin(150).leftMargin(450).hangingIndent(300).createCellText(cell);
						}
					}

					// XXX: another hack due to the mysterious verification 0
					Verifications[] verifications = control.getVerifications();
					if (verifications != null) {
						List<Verifications> toPrint = Lists.newArrayList();
						for (int j = 0; j < verifications.length; j++) {
							if(verifications[j].getID() != 0)
								toPrint.add(verifications[j]);
						}
						verifications = toPrint.toArray(new Verifications[toPrint.size()]);
					}

					printVerifications(doc, control.getVerifications(), parentTable);
				}
			}
		}
	}

	private void printVerifications(XWPFDocument doc, Verifications[] verifications, XWPFTable parentTable) {

		SimpleDateFormat sdf = new SimpleDateFormat("MMMMM F yyyy");

		if (verifications == null || verifications.length == 0) {
			XWPFTableRow row = parentTable.createRow();
			XWPFTableCell cell = row.getCell(0);
			setColSpan(cell, 4);
			new ParagraphBuilder().text("No verifications").topMargin(150).leftMargin(650).hangingIndent(400)
					.createCellText(cell);
		} else {
			Arrays.sort(verifications, new VerificationNumberComparator());
			for (int i = 0; i < verifications.length; i++) {
				Verifications verification = verifications[i];
				if (Strings.isNullOrEmpty(verification.getDeleteReason())) {
					if (verification.getTransfer() == 0) {
						String verificationStatus = verification.getVerificationStatus() == null ? "<STATUS TBD>"
								: verification.getVerificationStatus().getLabel();
						String verificationType = verification.getVerificationType() == null ? ""
								: " (" + verification.getVerificationType().getLabel() + ")";
						String verificationRespParty = verification.getResponsibleParty() == null ? " <TBD> "
								: verification.getResponsibleParty();
						String estCompDate = verification.getEstCompletionDate() == null ? " <TBD> "
								: sdf.format(verification.getEstCompletionDate());

						XWPFTableCell cell = parentTable.getNumberOfRows() == 1 ? parentTable.getRow(0).getCell(0)
								: parentTable.createRow().getCell(0);
						// XWPFTableRow row = new TableBuilder().size(1,
						// 1).setInnerHBorder(XWPFBorderType.NONE)
						// .createTable(doc).getRow(0);
						setColSpan(cell, 4);
						new ParagraphBuilder()
								.text("Verification " + getVerificationNumber(verification) + verificationType + " - "
										+ verificationStatus)
								.bold(true).topMargin(150).leftMargin(650).hangingIndent(400).createCellText(cell);
						new ParagraphBuilder().text("Responsible party: " + verificationRespParty
								+ ", Estimated Completion Date: " + estCompDate).leftMargin(650).hangingIndent(400)
								.createCellText(cell);

						new ParagraphBuilder().text("Description: " + verification.getVerificationDesc())
								.leftMargin(650).hangingIndent(400).createCellText(cell);
					} else {
						Transfers transfer = transferService.getTransferByID(verification.getTransfer());
						if (transfer == null || !transfer.getTargetType().equals("VERIFICATION")) {
							log.warn("Unsupported Verification transfer target type");
						} else {
							Verifications target = verificationService.getVerificationByID(transfer.getTargetID());
							if (target == null) {
								log.warn("Unknown verification transfer target id: " + transfer.getTargetID());
							} else {
								String targetStatus = target.getVerificationStatus() == null ? "<STATUS TBD>"
										: target.getVerificationStatus().getLabel();
								String targetType = target.getVerificationType() == null ? ""
										: " (" + target.getVerificationType().getLabel() + ")";
								String targetRespParty = target.getResponsibleParty() == null ? " <TBD> "
										: target.getResponsibleParty();
								String targetEstCompDate = target.getEstCompletionDate() == null ? " <TBD> "
										: sdf.format(target.getEstCompletionDate());

								XWPFTableRow row = parentTable.createRow();
								XWPFTableCell cell = row.getCell(0);
								setColSpan(cell, 4);
								ParagraphBuilder vTitle = new ParagraphBuilder()
										.text("Verification " + getVerificationNumber(verification) + " (TRANSFER): ")
										.bold(true).topMargin(150).leftMargin(650).hangingIndent(400);
								RunBuilder run = new RunBuilder().text("Verification " + getVerificationNumber(target)
										+ targetType + " - " + targetStatus);
								if (!Strings.isNullOrEmpty(target.getDeleteReason())) {
									run = run.strikethrough();
								}
								vTitle.addRun(run).createCellText(cell);
								new ParagraphBuilder()
										.text("Responsible party: " + targetRespParty + ", Estimated Completion Date: "
												+ targetEstCompDate)
										.leftMargin(650).hangingIndent(400).createCellText(cell);

								new ParagraphBuilder().text("Transfer reason: " + verification.getVerificationDesc())
										.leftMargin(650).hangingIndent(400).createCellText(cell);
							}
						}
					}
				}
			}
		}

	}

	private String getVerificationNumber(Verifications v) {
		String result = "";
		if (v != null) {
			Hazard_Controls[] controls = v.getControls();
			if (controls == null || controls.length == 0) {
				result = "Orph." + v.getVerificationNumber();
			} else {
				Hazard_Causes[] causes = controls[0].getCauses();
				if (causes == null || causes.length == 0) {
					result = "Orph." + controls[0].getControlNumber() + "." + v.getVerificationNumber();
				} else {
					result = causes[0].getCauseNumber() + "." + controls[0].getControlNumber() + "."
							+ v.getVerificationNumber();
				}
			}
		}

		return result;
	}

	private String getControlNumber(Hazard_Controls c) {
		String result = "";
		if (c != null) {
			Hazard_Causes[] causes = c.getCauses();
			if (causes == null || causes.length == 0) {
				result = "Orph." + c.getControlNumber();
			} else {
				result = causes[0].getCauseNumber() + "." + c.getControlNumber();
			}
		}
		return result;
	}

	/**
	 * Helper method that writes out text for a transferred CAUSE title based on
	 * the target of the transfer.
	 * 
	 * @param cell
	 *            the cell which will contain the Cause title
	 * @param cause
	 *            the cause which is the transfer origin
	 */
	private void printCauseTransfer(XWPFTableCell cell, Hazard_Causes cause, boolean inSpecificCause) {
		Transfers transfer = transferService.getTransferByID(cause.getTransfer());

		if (transfer.getTargetType().equals("HAZARD")) {
			Hazards targetHazard = hazardService.getHazardById(Integer.toString(transfer.getTargetID()));
			if (targetHazard.getProjectID() != cause.getHazards()[0].getProjectID()) {
				String targetHazNum = Strings.isNullOrEmpty(targetHazard.getHazardNumber()) ? "N/A"
						: targetHazard.getHazardNumber();
				new ParagraphBuilder()
						.text("Cause " + cause.getCauseNumber()
								+ " (TRANSFER) WARNING: This cause transfers to a hazard not in the current project. Hazard number: "
								+ targetHazNum)
						.topMargin(50).bold(inSpecificCause).leftMargin(350).hangingIndent(300).createCellText(cell);
			} else {
				ParagraphBuilder causeTitle = new ParagraphBuilder()
						.text("Cause " + cause.getCauseNumber() + " (TRANSFER): ").topMargin(50).bold(inSpecificCause)
						.leftMargin(350).hangingIndent(300);
				RunBuilder run = new RunBuilder()
						.text(targetHazard.getHazardNumber() + " \u2013 " + targetHazard.getHazardTitle());
				if (!targetHazard.getActive()) {
					run = run.strikethrough();
				}
				causeTitle.addRun(run).createCellText(cell);
			}

		} else if (transfer.getTargetType().equals("CAUSE")) {
			Hazard_Causes targetCause = causeService.getHazardCauseByID(transfer.getTargetID());
			Hazards targetHazard = targetCause.getHazards()[0];

			if (targetHazard.getProjectID() != cause.getHazards()[0].getProjectID()) {
				String targetHazNum = Strings.isNullOrEmpty(targetHazard.getHazardNumber()) ? "N/A"
						: targetHazard.getHazardNumber();
				new ParagraphBuilder()
						.text("Cause " + cause.getCauseNumber()
								+ " (TRANSFER) WARNING: This cause transfers to a hazard not in the current project. Hazard number: "
								+ targetHazNum)
						.topMargin(50).bold(inSpecificCause).leftMargin(350).hangingIndent(300).createCellText(cell);
			} else {
				ParagraphBuilder causeTitle = new ParagraphBuilder()
						.text("Cause " + cause.getCauseNumber() + " (TRANSFER): ").topMargin(50).bold(inSpecificCause)
						.leftMargin(350).hangingIndent(300);

				RunBuilder run = new RunBuilder().text(targetHazard.getHazardNumber() + ", Cause "
						+ targetCause.getCauseNumber() + " \u2013 " + targetCause.getTitle());

				if (!targetHazard.getActive() || !Strings.isNullOrEmpty(targetCause.getDeleteReason())) {
					run.strikethrough();
				}
				causeTitle.addRun(run).createCellText(cell);
			}
		}

	}

	/**
	 * Helper method to set the number of columns spanned by a table cell. This
	 * is similar to the colspan in HTML table TD element.
	 * 
	 * @param cell
	 *            the cell whose column span should be set
	 * @param numCols
	 *            the number of columns which the cell should span.
	 */
	private void setColSpan(XWPFTableCell cell, int numCols) {
		if (cell.getCTTc().getTcPr() == null)
			cell.getCTTc().addNewTcPr();
		if (cell.getCTTc().getTcPr().getGridSpan() == null)
			cell.getCTTc().getTcPr().addNewGridSpan();
		cell.getCTTc().getTcPr().getGridSpan().setVal(BigInteger.valueOf(numCols));
	}

}
