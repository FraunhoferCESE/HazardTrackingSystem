package ut.org.fraunhofer.plugins.hts.document;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.xmlbeans.XmlException;
import org.fraunhofer.plugins.hts.db.Hazard_Causes;
import org.fraunhofer.plugins.hts.db.Hazard_Group;
import org.fraunhofer.plugins.hts.db.Hazards;
import org.fraunhofer.plugins.hts.db.Mission_Payload;
import org.fraunhofer.plugins.hts.db.Review_Phases;
import org.fraunhofer.plugins.hts.db.Risk_Categories;
import org.fraunhofer.plugins.hts.db.Risk_Likelihoods;
import org.fraunhofer.plugins.hts.db.Subsystems;
import org.fraunhofer.plugins.hts.db.Transfers;
import org.fraunhofer.plugins.hts.db.service.HazardCauseService;
import org.fraunhofer.plugins.hts.db.service.HazardService;
import org.fraunhofer.plugins.hts.db.service.TransferService;
import org.fraunhofer.plugins.hts.document.HazardReportGenerator;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;

public class HazardReportGeneratorTest {

	private Hazards testHazard;
	private List<Review_Phases> testReviewPhases;
	private List<Risk_Categories> testRiskCategories;
	private List<Risk_Likelihoods> testRiskLikelihoods;
	private File outputDirectory;

	private TransferService mockTransferService;
	private HazardCauseService mockHazardCauseService;
	private HazardService mockHazardService;

	private Hazard_Causes mockCause1, mockCause2, mockCause3, mockCauseToHazard, mockCauseToCause;

	private void initializeMockCauses() {
		mockCause1 = mock(Hazard_Causes.class);
		when(mockCause1.getCauseNumber()).thenReturn(1);
		when(mockCause1.getTitle()).thenReturn(
				"Improper feed system design leads to incorrect pressures or temperatures during USE run");
		when(mockCause1.getDescription())
				.thenReturn(
						"The Upper Stage feed system must be designed in order to provide correct propellant interface parameters to the USE for operation. Any errors in the design that cause these parameters to be violated could result in USE failures including cavitation/fire/ explosion.");

		mockCause2 = mock(Hazard_Causes.class);
		when(mockCause2.getCauseNumber()).thenReturn(2);
		when(mockCause2.getTitle()).thenReturn("Depleted propellants");
		when(mockCause2.getDescription())
				.thenReturn(
						"If propellants are depleted before MECO, cavitation of turbo pumps could occur if USE is not shutdown.");

		mockCause3 = mock(Hazard_Causes.class);
		when(mockCause3.getCauseNumber()).thenReturn(3);
		when(mockCause3.getTitle()).thenReturn("Loss of fuel and/or oxidizer supply");
		when(mockCause3.getDescription())
				.thenReturn(
						"Total loss of fuel and/or oxidizer supply would be caused by a failed close prevalve during USE operation. This could be from failure of the prevalve or of the pneumatic system/command to the prevalve. A single dual coil pneumatic valve controls the pressure to both prevalves. If this pneumatic valve (HF/OF -1000) is inadvertently activated the prevalves will close.\n\nShould there be a total loss of the fuel or oxidizer supply from the Upper Stage to the J-2X during operation, this would unload the Fuel or Oxidizer Turbopump and the pump could experience rotor overspeed which may induce pump or turbine component failure (including component rotational burst), and possibly FTP/OTP structural disintegration. This structural disintegration will cause turbopump loss of function, generation of shrapnel, uncontained USE damage and possibly fire/explosion, leading to loss of mission, and/or a loss of crew/vehicle. In addition loss of fuel supply only would cause the USE gas generator to run LO2 rich.");

		mockCauseToHazard = mock(Hazard_Causes.class);
		when(mockCauseToHazard.getCauseNumber()).thenReturn(4);
		when(mockCauseToHazard.getTitle()).thenReturn("Fuel supply pressure too low or too high");
		when(mockCauseToHazard.getDescription())
				.thenReturn(
						"A fuel supply pressure from vehicle that is too low could result in too-low of an FTP inducer inlet pressure leading to excessive cavitation, potentially unloading the pump and progressing to rotor overspeed, which may induce pump or turbine component failure (including component rotational burst). This could result in FTP structural disintegration.\nA fuel supply pressure from vehicle that is too high could result in increased inducer inlet pressure, altering axial load on pump rotary components, but the balance piston will compensate for the alteration, and net axial thrust of the FTP will remain unchanged at zero.\nFTP structural disintegration will cause turbopump loss of function and possibly the generation of shrapnel. The cumulative effect is uncontained USE damage, and possiblyfire, leading to loss of mission, and/or a loss of crew/vehicle. (from USE-E-01 cause O)");
		when(mockCauseToHazard.getTransfer()).thenReturn(12345);

		mockCauseToCause = mock(Hazard_Causes.class);
		when(mockCauseToCause.getCauseNumber()).thenReturn(5);
		when(mockCauseToCause.getTitle()).thenReturn("Excessive POGO creates surges");
		when(mockCauseToCause.getDescription())
				.thenReturn(
						"POGO loads would create excessive stress and pressure/ flow fluctuations that could lead to cavitation");
		when(mockCauseToCause.getTransfer()).thenReturn(9876);

	}

	@Before
	public void setUp() {
		initializeMockCauses();

		testRiskCategories = new ArrayList<Risk_Categories>();
		Risk_Categories mockRiskCategories = mock(Risk_Categories.class);
		when(mockRiskCategories.getValue()).thenReturn("I - Catastrophic");
		when(mockRiskCategories.getID()).thenReturn(11111);
		testRiskCategories.add(mockRiskCategories);

		mockRiskCategories = mock(Risk_Categories.class);
		when(mockRiskCategories.getValue()).thenReturn("II - Critical");
		when(mockRiskCategories.getID()).thenReturn(22222);
		testRiskCategories.add(mockRiskCategories);

		mockRiskCategories = mock(Risk_Categories.class);
		when(mockRiskCategories.getValue()).thenReturn("III - Marginal");
		when(mockRiskCategories.getID()).thenReturn(33333);
		testRiskCategories.add(mockRiskCategories);

		mockRiskCategories = mock(Risk_Categories.class);
		when(mockRiskCategories.getValue()).thenReturn("IV - Negligible");
		when(mockRiskCategories.getID()).thenReturn(44444);
		testRiskCategories.add(mockRiskCategories);

		testRiskLikelihoods = new ArrayList<Risk_Likelihoods>();
		Risk_Likelihoods mockRiskLikelihoods = mock(Risk_Likelihoods.class);
		when(mockRiskLikelihoods.getValue()).thenReturn("A - Frequent");
		when(mockRiskLikelihoods.getID()).thenReturn(11111);
		testRiskLikelihoods.add(mockRiskLikelihoods);

		mockRiskLikelihoods = mock(Risk_Likelihoods.class);
		when(mockRiskLikelihoods.getValue()).thenReturn("B - Reasonably probable");
		when(mockRiskLikelihoods.getID()).thenReturn(22222);
		testRiskLikelihoods.add(mockRiskLikelihoods);

		mockRiskLikelihoods = mock(Risk_Likelihoods.class);
		when(mockRiskLikelihoods.getValue()).thenReturn("C - Occassional");
		when(mockRiskLikelihoods.getID()).thenReturn(33333);
		testRiskLikelihoods.add(mockRiskLikelihoods);

		mockRiskLikelihoods = mock(Risk_Likelihoods.class);
		when(mockRiskLikelihoods.getValue()).thenReturn("D - Remote");
		when(mockRiskLikelihoods.getID()).thenReturn(44444);
		testRiskLikelihoods.add(mockRiskLikelihoods);

		mockRiskLikelihoods = mock(Risk_Likelihoods.class);
		when(mockRiskLikelihoods.getValue()).thenReturn("E - Extremely improbable");
		when(mockRiskLikelihoods.getID()).thenReturn(55555);
		testRiskLikelihoods.add(mockRiskLikelihoods);
		testReviewPhases = new ArrayList<Review_Phases>();
		Review_Phases mockReviewPhase = mock(Review_Phases.class);
		when(mockReviewPhase.getLabel()).thenReturn("Phase I");
		when(mockReviewPhase.getID()).thenReturn(11111);
		testReviewPhases.add(mockReviewPhase);

		mockReviewPhase = mock(Review_Phases.class);
		when(mockReviewPhase.getLabel()).thenReturn("Phase II");
		when(mockReviewPhase.getID()).thenReturn(22222);
		testReviewPhases.add(mockReviewPhase);

		mockReviewPhase = mock(Review_Phases.class);
		when(mockReviewPhase.getLabel()).thenReturn("Phase III");
		when(mockReviewPhase.getID()).thenReturn(33333);
		testReviewPhases.add(mockReviewPhase);

		// outputDirectory = Files.createTempDir();
		outputDirectory = new File(System.getProperty("user.dir") + System.getProperty("file.separator") + "test_out");

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

		Mission_Payload testPayload = mock(Mission_Payload.class);
		when(testPayload.getName()).thenReturn("MERV");

		Review_Phases testReviewPhase = mock(Review_Phases.class);
		when(testReviewPhase.getLabel()).thenReturn("Phase I");
		when(testReviewPhase.getID()).thenReturn(11111);

		mockRiskCategories = mock(Risk_Categories.class);
		when(mockRiskCategories.getValue()).thenReturn("I - Catastrophic");
		when(mockRiskCategories.getID()).thenReturn(11111);

		mockRiskLikelihoods = mock(Risk_Likelihoods.class);
		when(mockRiskLikelihoods.getValue()).thenReturn("C - Occassional");
		when(mockRiskLikelihoods.getID()).thenReturn(33333);

		// --------------------- Mock transfer setup ------------------

		// Cause to Hazard
		Hazards transferDestinationHazard = mock(Hazards.class);
		when(transferDestinationHazard.getID()).thenReturn(99999);
		when(transferDestinationHazard.getHazardNum()).thenReturn("MERV-PRESS-02");
		when(transferDestinationHazard.getTitle()).thenReturn(
				"Failure to Maintain Liquid Hydrogen Propellant Tank Pressure leads to USE operational failure");

		Transfers mockCauseToHazardTransfer = mock(Transfers.class);
		when(mockCauseToHazardTransfer.getTargetID()).thenReturn(99999);
		when(mockCauseToHazardTransfer.getTargetType()).thenReturn("HAZARD");

		// Cause to Cause
		Hazards transferCauseHazard = mock(Hazards.class);
		when(transferCauseHazard.getHazardNum()).thenReturn("MERV-PROP-04");

		Hazard_Causes transferDestinationCause = mock(Hazard_Causes.class);
		when(transferDestinationCause.getID()).thenReturn(88888);
		when(transferDestinationCause.getCauseNumber()).thenReturn(1);
		when(transferDestinationCause.getHazards()).thenReturn(new Hazards[] { transferCauseHazard });
		when(transferDestinationCause.getTitle()).thenReturn(
				"POGO causes dynamic oscillations attaining a resonant frequency with other US components or USE");

		Transfers mockCausetoCauseTransfer = mock(Transfers.class);
		when(mockCausetoCauseTransfer.getTargetID()).thenReturn(88888);
		when(mockCausetoCauseTransfer.getTargetType()).thenReturn("CAUSE");

		mockTransferService = mock(TransferService.class);
		when(mockTransferService.getTransferByID(mockCauseToHazard.getTransfer()))
				.thenReturn(mockCauseToHazardTransfer);
		when(mockTransferService.getTransferByID(mockCauseToCause.getTransfer())).thenReturn(mockCausetoCauseTransfer);

		// -----------------------------------------------------------------------

		mockHazardService = mock(HazardService.class);
		when(mockHazardService.getHazardByID("99999")).thenReturn(transferDestinationHazard);

		mockHazardCauseService = mock(HazardCauseService.class);
		when(mockHazardCauseService.getHazardCauseByID(Integer.toString(transferDestinationCause.getID()))).thenReturn(
				transferDestinationCause);

		testHazard = mock(Hazards.class);
		when(testHazard.getHazardNum()).thenReturn("MERV-PROP-01");
		when(testHazard.getInitiationDate()).thenReturn(new Date(System.currentTimeMillis() - (60 * 413)));
		when(testHazard.getRevisionDate()).thenReturn(new Date(System.currentTimeMillis()));
		when(testHazard.getMissionPayload()).thenReturn(testPayload);
		when(testHazard.getPreparer()).thenReturn("Lucas Layman");
		when(testHazard.getEmail()).thenReturn("lucas.m.layman@nasa.gov");
		when(testHazard.getReviewPhase()).thenReturn(testReviewPhase);
		when(testHazard.getSubsystems()).thenReturn(testSubsystems);
		when(testHazard.getHazardGroups()).thenReturn(testGroups);
		when(testHazard.getRiskCategory()).thenReturn(mockRiskCategories);
		when(testHazard.getRiskLikelihood()).thenReturn(mockRiskLikelihoods);
		when(testHazard.getTitle()).thenReturn(
				"Failures of Upper Stage during USE operations that lead to USE Cavitation/Fire/Explosion");
		when(testHazard.getHazardDesc())
				.thenReturn(
						"The Upper Stage is responsible for supply of propellants (LH2 and LO2) to the USE during operation. Failures after USE start that result in a decrease or termination of flow to the USE will cause USE turbopump over speed/cavitation/damage leading to an explosion. If Upper Stage fails to command USE shutdown, propellant depletion could occur also leading to cavitation. A decrease in Net Positive Suction Pressure (NPSP) or blockage in the feedlines could decrease flow and inadvertent closure of the prevalve would terminate flow. Ingestion of ullage gas in the feed line will also cause turbopump cavitation. Ullage gas could be ingested if a vortex forms or propellant is depleted. Contaminates that enter the USE inlet could cause significant damage.");
		when(testHazard.getHazardCauses()).thenReturn(
				new Hazard_Causes[] { mockCause1, mockCause2, mockCause3, mockCauseToHazard, mockCauseToCause });

	}

	@Test
	public void testCreateWordDocuments() throws IOException, XmlException {

		List<Hazards> hazardList = Lists.newArrayList(testHazard);
		HazardReportGenerator test = new HazardReportGenerator(mockHazardService, mockHazardCauseService,
				mockTransferService);
		test.createWordDocuments(hazardList, testReviewPhases, testRiskCategories, testRiskLikelihoods,
				outputDirectory, true);

	}

}
