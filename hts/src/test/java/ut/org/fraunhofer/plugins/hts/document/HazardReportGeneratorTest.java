package ut.org.fraunhofer.plugins.hts.document;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import org.fraunhofer.plugins.hts.db.Hazard_Group;
import org.fraunhofer.plugins.hts.db.Hazards;
import org.fraunhofer.plugins.hts.db.Mission_Payload;
import org.fraunhofer.plugins.hts.db.Review_Phases;
import org.fraunhofer.plugins.hts.db.Subsystems;
import org.fraunhofer.plugins.hts.document.HazardReportGenerator;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;

public class HazardReportGeneratorTest {

	private Hazards testHazard;
	private Mission_Payload testPayload;
	private Review_Phases testReviewPhase;
	private Subsystems[] testSubsystems;
	private Hazard_Group[] testGroups;
	private File outputDirectory;

	@Before
	public void setUp() {
		// outputDirectory = Files.createTempDir();
		outputDirectory = new File(System.getProperty("user.dir") + System.getProperty("file.separator") + "test_out");

		Subsystems testSub1 = mock(Subsystems.class);
		when(testSub1.getLabel()).thenReturn("Propulsion");
		Subsystems testSub2 = mock(Subsystems.class);
		when(testSub2.getLabel()).thenReturn("Structure");
		Subsystems testSub3 = mock(Subsystems.class);
		when(testSub3.getLabel()).thenReturn("Propellants");
		testSubsystems = new Subsystems[] { testSub1, testSub2, testSub3 };

		Hazard_Group testGroup1 = mock(Hazard_Group.class);
		when(testGroup1.getLabel()).thenReturn("Fire/Explosion");
		Hazard_Group testGroup2 = mock(Hazard_Group.class);
		when(testGroup2.getLabel()).thenReturn("Pressure");

		testGroups = new Hazard_Group[] { testGroup1, testGroup2 };

		testPayload = mock(Mission_Payload.class);
		when(testPayload.getName()).thenReturn("MERV");

		testReviewPhase = mock(Review_Phases.class);
		when(testReviewPhase.getLabel()).thenReturn("Phase 0");

		testHazard = mock(Hazards.class);
		when(testHazard.getHazardNum()).thenReturn("PROP-08-MPS");
		when(testHazard.getInitiationDate()).thenReturn(new Date(System.currentTimeMillis() - (60 * 413)));
		when(testHazard.getRevisionDate()).thenReturn(new Date(System.currentTimeMillis()));
		when(testHazard.getMissionPayload()).thenReturn(testPayload);
		when(testHazard.getPreparer()).thenReturn("Lucas Layman");
		when(testHazard.getEmail()).thenReturn("lucas.m.layman@nasa.gov");
		when(testHazard.getReviewPhase()).thenReturn(testReviewPhase);
		when(testHazard.getSubsystems()).thenReturn(testSubsystems);
		when(testHazard.getHazardGroups()).thenReturn(testGroups);
		when(testHazard.getTitle()).thenReturn(
				"Contamination in the MPS Hydrogen System Leads to Malfunction, Damage or Fire/Explosion");
		when(testHazard.getHazardDesc())
				.thenReturn(
						"Main propulsion system contamination could affect propellant quality or cause damage to valves, lines, or other internal equipment or cause blockage affecting USE performance. Contamination reaching the USE turbo pumps could cause cavitation or structural failure resulting in an immediate explosion resulting in loss of Vehicle and Life. Contamination could also consist of FOD (which includes process and nonprocess) or internal system failures (metal particles, bolts, etc). Contamination in the pressurization system can marginalize MPS operation, causing it to fail or migrate into the LH2.)");

	}

	@Test
	public void testCreateWordDocuments() throws IOException {
		List<Hazards> hazardList = Lists.newArrayList(testHazard);

		HazardReportGenerator test = new HazardReportGenerator();
		test.createWordDocuments(hazardList, outputDirectory, true);

	}

}
