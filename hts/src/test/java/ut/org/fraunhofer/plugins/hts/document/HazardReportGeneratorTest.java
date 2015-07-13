package ut.org.fraunhofer.plugins.hts.document;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.xmlbeans.XmlException;
import org.fraunhofer.plugins.hts.document.HazardReportGenerator;
import org.fraunhofer.plugins.hts.model.ControlGroups;
import org.fraunhofer.plugins.hts.model.Hazard_Causes;
import org.fraunhofer.plugins.hts.model.Hazard_Controls;
import org.fraunhofer.plugins.hts.model.Hazard_Group;
import org.fraunhofer.plugins.hts.model.Hazards;
import org.fraunhofer.plugins.hts.model.Review_Phases;
import org.fraunhofer.plugins.hts.model.Risk_Categories;
import org.fraunhofer.plugins.hts.model.Risk_Likelihoods;
import org.fraunhofer.plugins.hts.model.Subsystems;
import org.fraunhofer.plugins.hts.model.Transfers;
import org.fraunhofer.plugins.hts.model.VerificationStatus;
import org.fraunhofer.plugins.hts.model.VerificationType;
import org.fraunhofer.plugins.hts.model.Verifications;
import org.fraunhofer.plugins.hts.service.CauseService;
import org.fraunhofer.plugins.hts.service.ControlService;
import org.fraunhofer.plugins.hts.service.HazardService;
import org.fraunhofer.plugins.hts.service.TransferService;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.google.common.collect.Lists;

public class HazardReportGeneratorTest {

	private static List<Review_Phases> validReviewPhases;
	private static List<Risk_Categories> validRiskCategories;
	private static List<Risk_Likelihoods> validRiskLikelihoods;

	private TransferService mockTransferService;
	private CauseService mockHazardCauseService;
	private HazardService mockHazardService;
	private ProjectManager mockProjectManager;
	private ControlService mockHazardControlService;

	private Hazard_Causes mockCause1, mockCause2, mockCause3, mockCauseToHazard, mockCauseToCause, deletedCause;

	private Hazard_Controls mockControl1, mockControlToCause, mockControlToControl, deletedControl;

	private Verifications mockVerification1, mockVerification2, deletedVerification;

	private void initializeMockCauses() {
		Risk_Likelihoods mockRiskLikelihoods = mock(Risk_Likelihoods.class);
		when(mockRiskLikelihoods.getValue()).thenReturn("C - Occassional");
		when(mockRiskLikelihoods.getID()).thenReturn(33333);

		Risk_Categories mockRiskCategories = mock(Risk_Categories.class);
		when(mockRiskCategories.getValue()).thenReturn("I - Catastrophic");
		when(mockRiskCategories.getID()).thenReturn(11111);

		mockCause1 = mock(Hazard_Causes.class);
		when(mockCause1.getCauseNumber()).thenReturn(1);
		when(mockCause1.getTitle()).thenReturn(
				"Improper feed system design leads to incorrect pressures or temperatures during USE run");
		when(mockCause1.getDescription())
				.thenReturn(
						"The Upper Stage feed system must be designed in order to provide correct propellant interface parameters to the USE for operation. Any errors in the design that cause these parameters to be violated could result in USE failures including cavitation/fire/ explosion.");
		when(mockCause1.getEffects()).thenReturn("Really terrible, horrible things happen.");
		when(mockCause1.getAdditionalSafetyFeatures()).thenReturn("We will do extreme inspections on the feed.");
		when(mockCause1.getRiskCategory()).thenReturn(mockRiskCategories);
		when(mockCause1.getRiskLikelihood()).thenReturn(mockRiskLikelihoods);
		when(mockCause1.getTransfer()).thenReturn(0);

		mockCause2 = mock(Hazard_Causes.class);
		when(mockCause2.getCauseNumber()).thenReturn(2);
		when(mockCause2.getTitle()).thenReturn("Depleted propellants");
		when(mockCause2.getDescription())
				.thenReturn(
						"If propellants are depleted before MECO, cavitation of turbo pumps could occur if USE is not shutdown.");
		when(mockCause2.getEffects()).thenReturn("It's not so bad.");
		when(mockCause2.getAdditionalSafetyFeatures()).thenReturn("None.");
		when(mockCause2.getRiskCategory()).thenReturn(mockRiskCategories);
		when(mockCause2.getRiskLikelihood()).thenReturn(mockRiskLikelihoods);
		when(mockCause2.getTransfer()).thenReturn(0);

		mockCause3 = mock(Hazard_Causes.class);
		when(mockCause3.getCauseNumber()).thenReturn(3);
		when(mockCause3.getTitle()).thenReturn("Loss of fuel and/or oxidizer supply");
		when(mockCause3.getDescription())
				.thenReturn(
						"Total loss of fuel and/or oxidizer supply would be caused by a failed close prevalve during USE operation. This could be from failure of the prevalve or of the pneumatic system/command to the prevalve. A single dual coil pneumatic valve controls the pressure to both prevalves. If this pneumatic valve (HF/OF -1000) is inadvertently activated the prevalves will close.\n\nShould there be a total loss of the fuel or oxidizer supply from the Upper Stage to the J-2X during operation, this would unload the Fuel or Oxidizer Turbopump and the pump could experience rotor overspeed which may induce pump or turbine component failure (including component rotational burst), and possibly FTP/OTP structural disintegration. This structural disintegration will cause turbopump loss of function, generation of shrapnel, uncontained USE damage and possibly fire/explosion, leading to loss of mission, and/or a loss of crew/vehicle. In addition loss of fuel supply only would cause the USE gas generator to run LO2 rich.");
		when(mockCause3.getEffects()).thenReturn("end of the world.");
		when(mockCause3.getAdditionalSafetyFeatures()).thenReturn(
				"Analysis, simulation, and demonstration to 5 9's reliability, which doesn't make sense but whatever.");
		when(mockCause3.getRiskCategory()).thenReturn(mockRiskCategories);
		when(mockCause3.getRiskLikelihood()).thenReturn(mockRiskLikelihoods);
		when(mockCause3.getTransfer()).thenReturn(0);
		when(mockCause3.getControls()).thenReturn(new Hazard_Controls[0]);

		mockCauseToHazard = mock(Hazard_Causes.class);
		when(mockCauseToHazard.getCauseNumber()).thenReturn(4);
		when(mockCauseToHazard.getTitle()).thenReturn("Fuel supply pressure too low or too high");
		when(mockCauseToHazard.getDescription()).thenReturn("TRansferred to a relevant Hazard here");
		when(mockCauseToHazard.getTransfer()).thenReturn(12345);
		when(mockCauseToHazard.getEffects()).thenReturn("Itching and burning.");
		when(mockCauseToHazard.getAdditionalSafetyFeatures()).thenReturn("gold Bond Medical powder helps.");
		when(mockCauseToHazard.getRiskCategory()).thenReturn(mockRiskCategories);
		when(mockCauseToHazard.getRiskLikelihood()).thenReturn(mockRiskLikelihoods);
		when(mockCauseToHazard.getControls()).thenReturn(new Hazard_Controls[0]);

		mockCauseToCause = mock(Hazard_Causes.class);
		when(mockCauseToCause.getCauseNumber()).thenReturn(5);
		// when(mockCauseToCause.getTitle()).thenReturn("Excessive POGO creates surges");
		when(mockCauseToCause.getDescription()).thenReturn(
				"This cause is described in another hazard cause. Look it up.");
		when(mockCauseToCause.getTransfer()).thenReturn(9876);
		when(mockCauseToCause.getEffects()).thenReturn("Catastrohpic structure failure.");
		when(mockCauseToCause.getAdditionalSafetyFeatures()).thenReturn("MECO when POGO is detected.");
		when(mockCauseToCause.getRiskCategory()).thenReturn(mockRiskCategories);
		when(mockCauseToCause.getRiskLikelihood()).thenReturn(mockRiskLikelihoods);

		deletedCause = mock(Hazard_Causes.class);
		when(deletedCause.getCauseNumber()).thenReturn(6);
		when(deletedCause.getTitle()).thenReturn("Excessive POGO creates surges");
		when(deletedCause.getDescription()).thenReturn(
				"POGO causes excessive cavitation, leading to structural collapse.");
		when(deletedCause.getEffects()).thenReturn("Catastrohpic structure failure.");
		when(deletedCause.getAdditionalSafetyFeatures()).thenReturn("MECO when POGO is detected.");
		when(deletedCause.getRiskCategory()).thenReturn(mockRiskCategories);
		when(deletedCause.getRiskLikelihood()).thenReturn(mockRiskLikelihoods);
		when(deletedCause.getDeleteReason()).thenReturn("This cause is no longer valid. It is handled elsewhere.");
		when(deletedCause.getControls()).thenReturn(new Hazard_Controls[0]);

	}

	private void initializeMockControls() {
		ControlGroups mockControlGroup = mock(ControlGroups.class);
		when(mockControlGroup.getLabel()).thenReturn("Design");

		mockControl1 = mock(Hazard_Controls.class);
		when(mockControl1.getControlGroup()).thenReturn(mockControlGroup);
		when(mockControl1.getControlNumber()).thenReturn(1);
		when(mockControl1.getDescription()).thenReturn(
				"Numerous safety valves conforming to NASA-5713 will be in place around the tank.");
		when(mockControl1.getID()).thenReturn(11111);
		when(mockControl1.getTransfer()).thenReturn(0);
		when(mockControl1.getCauses()).thenReturn(new Hazard_Causes[] { mockCause1 });

		deletedControl = mock(Hazard_Controls.class);
		when(deletedControl.getControlNumber()).thenReturn(2);
		when(deletedControl.getDeleteReason()).thenReturn(
				"This control is superceded by other controls and should be REMOVED.");
		when(deletedControl.getDescription()).thenReturn(
				"This control has been deleted and shouldn't be displayed in the hazard report");
		when(deletedControl.getControlGroup()).thenReturn(null);
		when(deletedControl.getID()).thenReturn(22222);
		when(deletedControl.getTransfer()).thenReturn(0);
		when(deletedControl.getCauses()).thenReturn(new Hazard_Causes[] { mockCause2 });

		mockControlToCause = mock(Hazard_Controls.class);
		when(mockControlToCause.getControlGroup()).thenReturn(null);
		when(mockControlToCause.getControlNumber()).thenReturn(3);
		when(mockControlToCause.getDeleteReason()).thenReturn(null);
		when(mockControlToCause.getDescription()).thenReturn(null);
		when(mockControlToCause.getID()).thenReturn(33333);
		when(mockControlToCause.getTransfer()).thenReturn(934875);

		mockControlToControl = mock(Hazard_Controls.class);
		when(mockControlToControl.getControlGroup()).thenReturn(null);
		when(mockControlToControl.getControlNumber()).thenReturn(4);
		when(mockControlToControl.getDeleteReason()).thenReturn(null);
		when(mockControlToControl.getDescription()).thenReturn(null);
		when(mockControlToControl.getID()).thenReturn(44444);
		when(mockControlToControl.getTransfer()).thenReturn(44447777);

		when(mockCause1.getControls()).thenReturn(new Hazard_Controls[] { mockControl1, mockControlToCause });
		when(mockCause2.getControls()).thenReturn(new Hazard_Controls[] { mockControlToControl, deletedControl });
		when(mockCauseToCause.getControls()).thenReturn(new Hazard_Controls[] { mockControlToControl });

	}

	private void initializeMockVerifications() {
		VerificationStatus mockVStatus = mock(VerificationStatus.class);
		when(mockVStatus.getLabel()).thenReturn("Open to Safety Tracking List");
		VerificationType mockVType = mock(VerificationType.class);
		when(mockVType.getLabel()).thenReturn("Simulation");
		
		mockVerification1 = mock(Verifications.class);
		when(mockVerification1.getVerificationNumber()).thenReturn(1);
		when(mockVerification1.getVerificationDesc()).thenReturn("Expansive Monte Carlo simulation of pressure distributions around rings 1, 2, and 5.");
		when(mockVerification1.getVerificationStatus()).thenReturn(mockVStatus);
		when(mockVerification1.getVerificationType()).thenReturn(mockVType);
		when(mockVerification1.getResponsibleParty()).thenReturn("George Washington (george.washington@nasa.gov)");
		when(mockVerification1.getEstCompletionDate()).thenReturn(new Date(System.currentTimeMillis()));
		when(mockVerification1.getControls()).thenReturn(new Hazard_Controls[] {mockControl1, mockControlToCause, mockControlToControl});
		when(mockVerification1.getDeleteReason()).thenReturn(null);
		
		mockVerification2 = mock(Verifications.class);
		when(mockVerification2.getVerificationNumber()).thenReturn(2);
		when(mockVerification2.getVerificationDesc()).thenReturn("This second verification contains no verification data other than the description. It is associated with all the controls in the hazard though.");
		when(mockVerification2.getVerificationStatus()).thenReturn(null);
		when(mockVerification2.getVerificationType()).thenReturn(null);
		when(mockVerification2.getResponsibleParty()).thenReturn(null);
		when(mockVerification2.getEstCompletionDate()).thenReturn(null);
		when(mockVerification2.getControls()).thenReturn(new Hazard_Controls[] {mockControl1, mockControlToCause, mockControlToControl});
		when(mockVerification1.getDeleteReason()).thenReturn(null);
		
		deletedVerification = mock(Verifications.class);
		when(deletedVerification.getVerificationNumber()).thenReturn(3);
		when(deletedVerification.getVerificationDesc()).thenReturn("This verification has been deleted and should not appear.");
		when(deletedVerification.getVerificationStatus()).thenReturn(mockVStatus);
		when(deletedVerification.getVerificationType()).thenReturn(mockVType);
		when(deletedVerification.getResponsibleParty()).thenReturn("George Washington (george.washington@nasa.gov)");
		when(deletedVerification.getEstCompletionDate()).thenReturn(new Date(System.currentTimeMillis()));
		when(deletedVerification.getControls()).thenReturn(new Hazard_Controls[] {mockControl1, mockControlToCause, mockControlToControl});
		when(deletedVerification.getDeleteReason()).thenReturn("Remove because it no longer applies.");
		
		when(mockControl1.getVerifications()).thenReturn(new Verifications[] {mockVerification1, mockVerification2, deletedVerification});
		when(mockControlToCause.getVerifications()).thenReturn(new Verifications[] {mockVerification1, mockVerification2, deletedVerification});
		when(mockControlToControl.getVerifications()).thenReturn(new Verifications[] {mockVerification1, mockVerification2, deletedVerification});
	}

	private void initializeTransfers() {
		mockTransferService = mock(TransferService.class);

		// Cause to Hazard
		Hazards transferDestinationHazard = mock(Hazards.class);
		when(transferDestinationHazard.getID()).thenReturn(99999);
		when(transferDestinationHazard.getHazardNumber()).thenReturn("MERV-PRESS-02");
		when(transferDestinationHazard.getHazardTitle()).thenReturn(
				"Failure to Maintain Liquid Hydrogen Propellant Tank Pressure leads to USE operational failure");

		Transfers mockCauseToHazardTransfer = mock(Transfers.class);
		when(mockCauseToHazardTransfer.getTargetID()).thenReturn(99999);
		when(mockCauseToHazardTransfer.getTargetType()).thenReturn("HAZARD");

		when(mockTransferService.getTransferByID(mockCauseToHazard.getTransfer()))
				.thenReturn(mockCauseToHazardTransfer);
		when(mockHazardService.getHazardById("99999")).thenReturn(transferDestinationHazard);

		// Cause to Cause
		Hazards transferCauseHazard = mock(Hazards.class);
		when(transferCauseHazard.getHazardNumber()).thenReturn("MERV-PROP-04");

		Hazard_Causes transferDestinationCause = mock(Hazard_Causes.class);
		when(transferDestinationCause.getID()).thenReturn(88888);
		when(transferDestinationCause.getCauseNumber()).thenReturn(1);
		when(transferDestinationCause.getHazards()).thenReturn(new Hazards[] { transferCauseHazard });
		when(transferDestinationCause.getTitle()).thenReturn(
				"POGO causes dynamic oscillations attaining a resonant frequency with other US components or USE");

		Transfers mockCausetoCauseTransfer = mock(Transfers.class);
		when(mockCausetoCauseTransfer.getTargetID()).thenReturn(88888);
		when(mockCausetoCauseTransfer.getTargetType()).thenReturn("CAUSE");

		when(mockTransferService.getTransferByID(mockCauseToCause.getTransfer())).thenReturn(mockCausetoCauseTransfer);
		when(mockHazardCauseService.getHazardCauseByID(transferDestinationCause.getID())).thenReturn(
				transferDestinationCause);

		// Control to Cause
		Transfers mockControltoCauseTransfer = mock(Transfers.class);
		when(mockControltoCauseTransfer.getTargetID()).thenReturn(88888);
		when(mockControltoCauseTransfer.getTargetType()).thenReturn("CAUSE");
		when(mockTransferService.getTransferByID(mockControlToCause.getTransfer())).thenReturn(
				mockControltoCauseTransfer);

		// Control to Control
		Hazard_Controls transferDestinationControl = mock(Hazard_Controls.class);
		when(transferDestinationControl.getID()).thenReturn(78435514);
		when(transferDestinationControl.getHazard()).thenReturn(new Hazards[] { transferCauseHazard });
		when(transferDestinationControl.getControlGroup()).thenReturn(null);
		when(transferDestinationControl.getControlNumber()).thenReturn(42);
		when(transferDestinationControl.getDescription()).thenReturn(
				"There are many things that are involved in this control");
		when(transferDestinationControl.getDeleteReason()).thenReturn(null);
		when(transferDestinationControl.getTransfer()).thenReturn(0);

		Transfers mockControlToControlTransfer = mock(Transfers.class);
		when(mockControlToControlTransfer.getTargetID()).thenReturn(78435514);
		when(mockControlToControlTransfer.getTargetType()).thenReturn("CONTROL");
		when(mockTransferService.getTransferByID(mockControlToControl.getTransfer())).thenReturn(
				mockControlToControlTransfer);
		when(mockHazardControlService.getHazardControlByID(78435514)).thenReturn(transferDestinationControl);

	}

	@Before
	public void setUp() {
		mockHazardService = mock(HazardService.class);
		mockHazardCauseService = mock(CauseService.class);
		mockHazardControlService = mock(ControlService.class);

		initializeMockCauses();
		initializeMockControls();
		initializeMockVerifications();
		initializeTransfers();

		Project mockProject1 = mock(Project.class);
		when(mockProject1.getName()).thenReturn("Project named AAAAA");
		Project mockProject2 = mock(Project.class);
		when(mockProject2.getName()).thenReturn("Project named BBBBB");
		mockProjectManager = mock(ProjectManager.class);
		when(mockProjectManager.getProjectObj(new Long(1111))).thenReturn(mockProject1);
		when(mockProjectManager.getProjectObj(new Long(0000))).thenReturn(mockProject2);
	}

	@Test
	public void testCreateWordDocuments() throws IOException, XmlException {
		Risk_Categories mockRiskCategories = mock(Risk_Categories.class);
		when(mockRiskCategories.getValue()).thenReturn("I - Catastrophic");
		when(mockRiskCategories.getID()).thenReturn(11111);

		Risk_Likelihoods mockRiskLikelihoods = mock(Risk_Likelihoods.class);
		when(mockRiskLikelihoods.getValue()).thenReturn("C - Occassional");
		when(mockRiskLikelihoods.getID()).thenReturn(33333);

		Review_Phases testReviewPhase = mock(Review_Phases.class);
		when(testReviewPhase.getLabel()).thenReturn("Phase I");
		when(testReviewPhase.getID()).thenReturn(11111);

		Subsystems testSub1 = mock(Subsystems.class);
		when(testSub1.getLabel()).thenReturn("Propulsion");
		Subsystems testSub2 = mock(Subsystems.class);
		when(testSub2.getLabel()).thenReturn("Structure");
		Subsystems testSub3 = mock(Subsystems.class);
		when(testSub3.getLabel()).thenReturn("Propellants");
		Subsystems[] testSubsystems = new Subsystems[] { testSub1, testSub2, testSub3 };

		Hazard_Group testGroup1 = mock(Hazard_Group.class);
		when(testGroup1.getLabel()).thenReturn("Fire/Explosion");
		Hazard_Group testGroup2 = mock(Hazard_Group.class);
		when(testGroup2.getLabel()).thenReturn("Pressure");
		Hazard_Group[] testGroups = new Hazard_Group[] { testGroup1, testGroup2 };

		Hazards testHazard = mock(Hazards.class);
		when(testHazard.getHazardNumber()).thenReturn("MERV-PROP-01");
		when(testHazard.getInitiationDate()).thenReturn(new Date(System.currentTimeMillis() - (60 * 413)));
		when(testHazard.getRevisionDate()).thenReturn(new Date(System.currentTimeMillis()));
		when(testHazard.getProjectID()).thenReturn(new Long(1111));
		when(testHazard.getPreparer()).thenReturn("Lucas Layman");
		when(testHazard.getEmail()).thenReturn("lucas.m.layman@nasa.gov");
		when(testHazard.getReviewPhase()).thenReturn(testReviewPhase);
		when(testHazard.getSubsystems()).thenReturn(testSubsystems);
		when(testHazard.getHazardGroups()).thenReturn(testGroups);
		when(testHazard.getHazardTitle()).thenReturn(
				"Failures of Upper Stage during USE operations that lead to USE Cavitation/Fire/Explosion");
		when(testHazard.getHazardDescription())
				.thenReturn(
						"The Upper Stage is responsible for supply of propellants (LH2 and LO2) to the USE during operation. Failures after USE start that result in a decrease or termination of flow to the USE will cause USE turbopump over speed/cavitation/damage leading to an explosion. If Upper Stage fails to command USE shutdown, propellant depletion could occur also leading to cavitation. A decrease in Net Positive Suction Pressure (NPSP) or blockage in the feedlines could decrease flow and inadvertent closure of the prevalve would terminate flow. Ingestion of ullage gas in the feed line will also cause turbopump cavitation. Ullage gas could be ingested if a vortex forms or propellant is depleted. Contaminates that enter the USE inlet could cause significant damage.");
		when(testHazard.getHazardCauses()).thenReturn(
				new Hazard_Causes[] { mockCause1, mockCause2, mockCause3, mockCauseToHazard, mockCauseToCause,
						deletedCause });
		when(testHazard.getHazardControls()).thenReturn(
				new Hazard_Controls[] { mockControl1, mockControlToCause, mockControlToControl, deletedControl });

		List<Hazards> hazardList = Lists.newArrayList(testHazard);
		HazardReportGenerator test = new HazardReportGenerator(mockHazardService, mockHazardCauseService,
				mockTransferService, mockProjectManager, mockHazardControlService);

		final File template = new File(System.getProperty("user.dir") + System.getProperty("file.separator") + "src"
				+ System.getProperty("file.separator") + "main" + System.getProperty("file.separator") + "resources"
				+ System.getProperty("file.separator") + "Template.docx");

		List<byte[]> results = test.createWordDocument(hazardList, validReviewPhases, validRiskCategories,
				validRiskLikelihoods, template.toURI().toURL().openStream());

		File outputDirectory = new File(System.getProperty("user.dir") + System.getProperty("file.separator")
				+ "test_out");
		File reportFile = new File(outputDirectory + File.separator + hazardList.get(0).getHazardNumber() + ".docx");

		FileOutputStream out = new FileOutputStream(reportFile);
		out.write(results.get(0));
		out.close();
	}

	@Test
	public void testCreateWordDocMinimalInfo() throws MalformedURLException, XmlException, IOException {
		Risk_Categories mockRiskCategories = mock(Risk_Categories.class);
		when(mockRiskCategories.getValue()).thenReturn("I - Catastrophic");
		when(mockRiskCategories.getID()).thenReturn(11111);

		Risk_Likelihoods mockRiskLikelihoods = mock(Risk_Likelihoods.class);
		when(mockRiskLikelihoods.getValue()).thenReturn("A - Frequent");
		when(mockRiskLikelihoods.getID()).thenReturn(11111);

		Review_Phases testReviewPhase = mock(Review_Phases.class);
		when(testReviewPhase.getLabel()).thenReturn("Phase I");
		when(testReviewPhase.getID()).thenReturn(11111);

		Hazards testHazard = mock(Hazards.class);
		when(testHazard.getHazardNumber()).thenReturn("dfg");
		when(testHazard.getInitiationDate()).thenReturn(new Date(System.currentTimeMillis() - (60 * 413)));
		when(testHazard.getRevisionDate()).thenReturn(new Date(System.currentTimeMillis()));
		when(testHazard.getProjectID()).thenReturn(new Long(0000));
		when(testHazard.getPreparer()).thenReturn("admin");
		when(testHazard.getEmail()).thenReturn("admin@nasa.gov");
		when(testHazard.getReviewPhase()).thenReturn(testReviewPhase);
		when(testHazard.getSubsystems()).thenReturn(new Subsystems[0]);
		when(testHazard.getHazardGroups()).thenReturn(new Hazard_Group[0]);
		when(testHazard.getHazardTitle()).thenReturn("dfgdfgdfg");
		// when(testHazard.getHazardDesc())
		// .thenReturn(
		// "The Upper Stage is responsible for supply of propellants (LH2 and LO2) to the USE during operation. Failures after USE start that result in a decrease or termination of flow to the USE will cause USE turbopump over speed/cavitation/damage leading to an explosion. If Upper Stage fails to command USE shutdown, propellant depletion could occur also leading to cavitation. A decrease in Net Positive Suction Pressure (NPSP) or blockage in the feedlines could decrease flow and inadvertent closure of the prevalve would terminate flow. Ingestion of ullage gas in the feed line will also cause turbopump cavitation. Ullage gas could be ingested if a vortex forms or propellant is depleted. Contaminates that enter the USE inlet could cause significant damage.");
		when(testHazard.getHazardCauses()).thenReturn(new Hazard_Causes[0]);
		when(testHazard.getHazardControls()).thenReturn(new Hazard_Controls[0]);

		List<Hazards> hazardList = Lists.newArrayList(testHazard);
		HazardReportGenerator test = new HazardReportGenerator(mockHazardService, mockHazardCauseService,
				mockTransferService, mockProjectManager, mockHazardControlService);

		final File template = new File(System.getProperty("user.dir") + System.getProperty("file.separator") + "src"
				+ System.getProperty("file.separator") + "main" + System.getProperty("file.separator") + "resources"
				+ System.getProperty("file.separator") + "Template.docx");

		List<byte[]> results = test.createWordDocument(hazardList, validReviewPhases, validRiskCategories,
				validRiskLikelihoods, template.toURI().toURL().openStream());

		File outputDirectory = new File(System.getProperty("user.dir") + System.getProperty("file.separator")
				+ "test_out");
		File reportFile = new File(outputDirectory + File.separator + hazardList.get(0).getHazardNumber() + ".docx");

		FileOutputStream out = new FileOutputStream(reportFile);
		out.write(results.get(0));
		out.close();

	}

	@BeforeClass
	public static void oneTimeSetup() {
		initializeValidRiskLikelihoods();
		initializeValidRiskCategories();
		initializeValidReviewPhases();
	}

	private static void initializeValidReviewPhases() {
		validReviewPhases = new ArrayList<Review_Phases>();
		Review_Phases mockReviewPhase = mock(Review_Phases.class);
		when(mockReviewPhase.getLabel()).thenReturn("Phase I");
		when(mockReviewPhase.getID()).thenReturn(11111);
		validReviewPhases.add(mockReviewPhase);

		mockReviewPhase = mock(Review_Phases.class);
		when(mockReviewPhase.getLabel()).thenReturn("Phase II");
		when(mockReviewPhase.getID()).thenReturn(22222);
		validReviewPhases.add(mockReviewPhase);

		mockReviewPhase = mock(Review_Phases.class);
		when(mockReviewPhase.getLabel()).thenReturn("Phase III");
		when(mockReviewPhase.getID()).thenReturn(33333);
		validReviewPhases.add(mockReviewPhase);
	}

	private static void initializeValidRiskCategories() {
		validRiskCategories = new ArrayList<Risk_Categories>();
		Risk_Categories mockRiskCategories = mock(Risk_Categories.class);
		when(mockRiskCategories.getValue()).thenReturn("I - Catastrophic");
		when(mockRiskCategories.getID()).thenReturn(11111);
		validRiskCategories.add(mockRiskCategories);

		mockRiskCategories = mock(Risk_Categories.class);
		when(mockRiskCategories.getValue()).thenReturn("II - Critical");
		when(mockRiskCategories.getID()).thenReturn(22222);
		validRiskCategories.add(mockRiskCategories);

		mockRiskCategories = mock(Risk_Categories.class);
		when(mockRiskCategories.getValue()).thenReturn("III - Marginal");
		when(mockRiskCategories.getID()).thenReturn(33333);
		validRiskCategories.add(mockRiskCategories);

		mockRiskCategories = mock(Risk_Categories.class);
		when(mockRiskCategories.getValue()).thenReturn("IV - Negligible");
		when(mockRiskCategories.getID()).thenReturn(44444);
		validRiskCategories.add(mockRiskCategories);

	}

	private static void initializeValidRiskLikelihoods() {
		validRiskLikelihoods = new ArrayList<Risk_Likelihoods>();
		Risk_Likelihoods mockRiskLikelihoods = mock(Risk_Likelihoods.class);
		when(mockRiskLikelihoods.getValue()).thenReturn("A - Frequent");
		when(mockRiskLikelihoods.getID()).thenReturn(11111);
		validRiskLikelihoods.add(mockRiskLikelihoods);

		mockRiskLikelihoods = mock(Risk_Likelihoods.class);
		when(mockRiskLikelihoods.getValue()).thenReturn("B - Reasonably probable");
		when(mockRiskLikelihoods.getID()).thenReturn(22222);
		validRiskLikelihoods.add(mockRiskLikelihoods);

		mockRiskLikelihoods = mock(Risk_Likelihoods.class);
		when(mockRiskLikelihoods.getValue()).thenReturn("C - Occassional");
		when(mockRiskLikelihoods.getID()).thenReturn(33333);
		validRiskLikelihoods.add(mockRiskLikelihoods);

		mockRiskLikelihoods = mock(Risk_Likelihoods.class);
		when(mockRiskLikelihoods.getValue()).thenReturn("D - Remote");
		when(mockRiskLikelihoods.getID()).thenReturn(44444);
		validRiskLikelihoods.add(mockRiskLikelihoods);

		mockRiskLikelihoods = mock(Risk_Likelihoods.class);
		when(mockRiskLikelihoods.getValue()).thenReturn("E - Extremely improbable");
		when(mockRiskLikelihoods.getID()).thenReturn(55555);
		validRiskLikelihoods.add(mockRiskLikelihoods);

	}
}
