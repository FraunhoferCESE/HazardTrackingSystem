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

import org.fraunhofer.plugins.hts.model.Risk_Likelihoods;
import org.fraunhofer.plugins.hts.service.RiskLikelihoodsService;
import org.junit.Before;
import org.junit.Test;

import com.atlassian.activeobjects.external.ActiveObjects;

public class RiskLikelihoodServiceImplTest {

	Risk_Likelihoods mockLikelihood;
	Risk_Likelihoods[] mockLikelihoods;
	ActiveObjects mockActiveObjects;

	@Before
	public void setUp() {
		RiskLikelihoodsService.reset();

		mockLikelihood = mock(Risk_Likelihoods.class);
		mockLikelihoods = new Risk_Likelihoods[] { mockLikelihood };
		mockActiveObjects = mock(ActiveObjects.class);

		when(mockActiveObjects.create(eq(Risk_Likelihoods.class), (DBParam[]) anyVararg())).thenReturn(mockLikelihood);
		when(mockActiveObjects.find(eq(Risk_Likelihoods.class))).thenReturn(mockLikelihoods);
	}

	@Test
	public void testAdd() {
		RiskLikelihoodsService test = new RiskLikelihoodsService(mockActiveObjects);
		Risk_Likelihoods result = test.add("blah", "desc");

		assertEquals(mockLikelihood, result);

		verify(mockLikelihood).setValue("blah");
		verify(mockLikelihood).setLikelihoodDesc("desc");
		verify(mockLikelihood).save();
	}

	@Test
	public void testGetLikelihoodByID() {
		Risk_Likelihoods mockLikelihood2 = mock(Risk_Likelihoods.class);
		Risk_Likelihoods[] mockLikelihoods = { mockLikelihood, mockLikelihood2 };

		when(mockActiveObjects.find(eq(Risk_Likelihoods.class), any(Query.class))).thenReturn(mockLikelihoods);

		RiskLikelihoodsService test = new RiskLikelihoodsService(mockActiveObjects);
		Risk_Likelihoods result = test.getLikelihoodByID("blah");

		assertTrue(RiskLikelihoodsService.isInitialized());
		verify(mockActiveObjects).find(eq(Risk_Likelihoods.class), any(Query.class));
		assertEquals(result, mockLikelihood);

	}

	@Test
	public void testGetLikelihoodByIDNoneFound() {
		Risk_Likelihoods[] mockLikelihoods = {};

		when(mockActiveObjects.find(eq(Risk_Likelihoods.class), any(Query.class))).thenReturn(mockLikelihoods);

		RiskLikelihoodsService test = new RiskLikelihoodsService(mockActiveObjects);
		Risk_Likelihoods result = test.getLikelihoodByID("blah");

		assertTrue(RiskLikelihoodsService.isInitialized());
		verify(mockActiveObjects).find(eq(Risk_Likelihoods.class), any(Query.class));
		assertNull(result);

	}

	@Test
	public void testAllNoneFound() {
		Risk_Likelihoods[] mockLikelihoods = {};

		when(mockActiveObjects.find(eq(Risk_Likelihoods.class))).thenReturn(mockLikelihoods);

		RiskLikelihoodsService test = new RiskLikelihoodsService(mockActiveObjects);
		List<Risk_Likelihoods> result = test.all();

		assertTrue(RiskLikelihoodsService.isInitialized());
		assertTrue(result.isEmpty());
	}

	@Test
	public void testAllMultipleFound() {
		Risk_Likelihoods[] mockLikelihoods = { mockLikelihood, mockLikelihood };

		when(mockActiveObjects.find(eq(Risk_Likelihoods.class))).thenReturn(mockLikelihoods);

		RiskLikelihoodsService test = new RiskLikelihoodsService(mockActiveObjects);
		List<Risk_Likelihoods> result = test.all();

		assertTrue(RiskLikelihoodsService.isInitialized());
		assertEquals(result.size(), 2);
	}

	@Test
	public void testInitializeTables() {
		Risk_Likelihoods[] mockLikelihoods = {};
		when(mockActiveObjects.find(eq(Risk_Likelihoods.class))).thenReturn(mockLikelihoods);

		RiskLikelihoodsService test = new RiskLikelihoodsService(mockActiveObjects);
		test.initializeTable();

		verify(mockActiveObjects, times(5)).create(eq(Risk_Likelihoods.class), (DBParam[]) anyVararg());

		verify(mockLikelihood).setValue("A - Frequent");
		verify(mockLikelihood).setLikelihoodDesc("Likely to occur repeatedly");
		verify(mockLikelihood).setValue("B - Reasonably probable");
		verify(mockLikelihood).setLikelihoodDesc("Likely to occur several times");
		verify(mockLikelihood).setValue("C - Occasional");
		verify(mockLikelihood).setLikelihoodDesc("Likely to occur sometime");
		verify(mockLikelihood).setValue("D - Remote");
		verify(mockLikelihood).setLikelihoodDesc("Unlikely to occur, but possible");
		verify(mockLikelihood).setValue("E - Extremely improbable");
		verify(mockLikelihood).setLikelihoodDesc("The probability of occurence cannot be distinguished from zero");
	}

	@Test
	public void testInitializeTablesAlreadyInitialized() {
		Risk_Likelihoods[] mockLikelihoods = {};
		when(mockActiveObjects.find(eq(Risk_Likelihoods.class))).thenReturn(mockLikelihoods);

		RiskLikelihoodsService test = new RiskLikelihoodsService(mockActiveObjects);
		test.initializeTable();
		test.initializeTable();

		verify(mockActiveObjects, times(5)).create(eq(Risk_Likelihoods.class), (DBParam[]) anyVararg());

		verify(mockLikelihood).setValue("A - Frequent");
		verify(mockLikelihood).setLikelihoodDesc("Likely to occur repeatedly");
		verify(mockLikelihood).setValue("B - Reasonably probable");
		verify(mockLikelihood).setLikelihoodDesc("Likely to occur several times");
		verify(mockLikelihood).setValue("C - Occasional");
		verify(mockLikelihood).setLikelihoodDesc("Likely to occur sometime");
		verify(mockLikelihood).setValue("D - Remote");
		verify(mockLikelihood).setLikelihoodDesc("Unlikely to occur, but possible");
		verify(mockLikelihood).setValue("E - Extremely improbable");
		verify(mockLikelihood).setLikelihoodDesc("The probability of occurence cannot be distinguished from zero");
	}

	@Test
	public void testIsInitialized() {
		assertFalse(RiskLikelihoodsService.isInitialized());
	}

	@Test
	public void testReset() {
		RiskLikelihoodsService test = new RiskLikelihoodsService(mockActiveObjects);
		test.initializeTable();

		assertTrue(RiskLikelihoodsService.isInitialized());
		RiskLikelihoodsService.reset();
		assertFalse(RiskLikelihoodsService.isInitialized());
	}
}
