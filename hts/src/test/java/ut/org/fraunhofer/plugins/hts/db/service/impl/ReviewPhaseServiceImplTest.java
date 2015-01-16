package ut.org.fraunhofer.plugins.hts.db.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import net.java.ao.DBParam;
import net.java.ao.Query;

import org.fraunhofer.plugins.hts.db.Review_Phases;
import org.fraunhofer.plugins.hts.db.service.ReviewPhaseService;
import org.fraunhofer.plugins.hts.db.service.impl.ReviewPhaseServiceImpl;
import org.junit.Before;
import org.junit.Test;

import com.atlassian.activeobjects.external.ActiveObjects;

public class ReviewPhaseServiceImplTest {

	Review_Phases mockPhase;
	Review_Phases[] mockPhases;
	ActiveObjects mockActiveObjects;

	@Before
	public void setUp() {
		ReviewPhaseServiceImpl.reset();

		mockPhase = mock(Review_Phases.class);
		mockPhases = new Review_Phases[] { mockPhase };
		mockActiveObjects = mock(ActiveObjects.class);

		// Specify the mock object behavior. When ActiveObjects.create is
		// called, return our mockPhases

		when(mockActiveObjects.create(eq(Review_Phases.class), (DBParam[]) anyVararg())).thenReturn(mockPhase);
		when(mockActiveObjects.find(eq(Review_Phases.class))).thenReturn(mockPhases);
	}

	@Test
	public void testAdd() {
		// Create the test object and run the add() method
		ReviewPhaseService test = new ReviewPhaseServiceImpl(mockActiveObjects);
		Review_Phases result = test.add("blah", "desc");

		// 4. Verify that the return value is what we expect
		assertEquals(mockPhase, result);

		// 5. Verify that setLabel, setDescription, and save() were properly
		// called on our mock object according to the logic of the .add()
		// method. The lines below check that each of the methods setLabel(),
		// setDescription(), and save() were called once. For setLabel() and
		// setDescription(), these lines also check that they were called with
		// the arguments we sent.

		// Note that this is how you typically test void methods with no return
		// value as well -- the best you can do
		// is make sure that the logic inside the tested method was called in
		// the proper order with the proper parameters
		verify(mockPhase).setLabel("blah");
		verify(mockPhase).setDescription("desc");
		verify(mockPhase).save();
	}

	@Test
	public void testGetReviewPhaseByIDFoundMultiple() {
		Review_Phases mockPhase2 = mock(Review_Phases.class);
		Review_Phases[] mockPhases = { mockPhase, mockPhase2 };

		when(mockActiveObjects.find(eq(Review_Phases.class), any(Query.class))).thenReturn(mockPhases);

		ReviewPhaseService test = new ReviewPhaseServiceImpl(mockActiveObjects);
		Review_Phases result = test.getReviewPhaseByID("blah");

		assertTrue(ReviewPhaseServiceImpl.isInitialized());
		verify(mockActiveObjects).find(eq(Review_Phases.class), any(Query.class));
		assertEquals(result, mockPhase);

	}

	@Test
	public void testGetReviewPhaseByIDNoneFound() {
		Review_Phases[] mockPhases = {};

		when(mockActiveObjects.find(eq(Review_Phases.class), any(Query.class))).thenReturn(mockPhases);

		ReviewPhaseService test = new ReviewPhaseServiceImpl(mockActiveObjects);
		Review_Phases result = test.getReviewPhaseByID("blah");

		assertTrue(ReviewPhaseServiceImpl.isInitialized());
		verify(mockActiveObjects).find(eq(Review_Phases.class), any(Query.class));
		assertNull(result);

	}

	@Test
	public void testAllNoneFound() {
		Review_Phases[] mockPhases = {};

		when(mockActiveObjects.find(eq(Review_Phases.class))).thenReturn(mockPhases);

		ReviewPhaseService test = new ReviewPhaseServiceImpl(mockActiveObjects);
		List<Review_Phases> result = test.all();

		assertTrue(ReviewPhaseServiceImpl.isInitialized());
		assertTrue(result.isEmpty());
	}

	@Test
	public void testAllMultipleFound() {
		Review_Phases[] mockPhases = { mockPhase, mockPhase, mockPhase };
		ActiveObjects mockActiveObjects = mock(ActiveObjects.class);

		when(mockActiveObjects.find(eq(Review_Phases.class))).thenReturn(mockPhases);

		ReviewPhaseService test = new ReviewPhaseServiceImpl(mockActiveObjects);
		List<Review_Phases> result = test.all();

		assertTrue(ReviewPhaseServiceImpl.isInitialized());
		assertEquals(result.size(), 3);
	}

	@Test
	public void testInitializeTables() {
		Review_Phases[] mockPhases = {};
		when(mockActiveObjects.find(eq(Review_Phases.class))).thenReturn(mockPhases);

		ReviewPhaseServiceImpl test = new ReviewPhaseServiceImpl(mockActiveObjects);
		test.initializeTable();

		verify(mockActiveObjects, times(3)).create(eq(Review_Phases.class), (DBParam[]) anyVararg());

		verify(mockPhase).setLabel("Phase I");
		verify(mockPhase).setDescription("Phase I safety review");
		verify(mockPhase).setLabel("Phase II");
		verify(mockPhase).setDescription("Phase II safety review");
		verify(mockPhase).setLabel("Phase III");
		verify(mockPhase).setDescription("Phase III safety review");
		verify(mockPhase, times(3)).save();
	}

	@Test
	public void testInitializeTablesAlreadyInitialized() {
		Review_Phases[] mockPhases = {};
		when(mockActiveObjects.find(eq(Review_Phases.class))).thenReturn(mockPhases);

		ReviewPhaseServiceImpl test = new ReviewPhaseServiceImpl(mockActiveObjects);
		test.initializeTable();
		test.initializeTable();

		verify(mockActiveObjects, times(3)).create(eq(Review_Phases.class), (DBParam[]) anyVararg());

		verify(mockPhase).setLabel("Phase I");
		verify(mockPhase).setDescription("Phase I safety review");
		verify(mockPhase).setLabel("Phase II");
		verify(mockPhase).setDescription("Phase II safety review");
		verify(mockPhase).setLabel("Phase III");
		verify(mockPhase).setDescription("Phase III safety review");
		verify(mockPhase, times(3)).save();
	}

	@Test
	public void testIsInitialized() {
		assertFalse(ReviewPhaseServiceImpl.isInitialized());
	}

	@Test
	public void testReset() {
		ReviewPhaseServiceImpl test = new ReviewPhaseServiceImpl(mockActiveObjects);
		test.initializeTable();

		assertTrue(ReviewPhaseServiceImpl.isInitialized());
		ReviewPhaseServiceImpl.reset();
		assertFalse(ReviewPhaseServiceImpl.isInitialized());
	}
}
