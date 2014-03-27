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

import org.fraunhofer.plugins.hts.db.Risk_Categories;
import org.fraunhofer.plugins.hts.db.service.RiskCategoryService;
import org.fraunhofer.plugins.hts.db.service.impl.RiskCategoryServiceImpl;
import org.junit.Before;
import org.junit.Test;

import com.atlassian.activeobjects.external.ActiveObjects;

public class RiskCategoryServiceImplTest {

	ActiveObjects mockActiveObjects;
	Risk_Categories mockCategory;
	Risk_Categories[] mockCategories;

	@Before
	public void setUp() {
		RiskCategoryServiceImpl.reset();

		mockActiveObjects = mock(ActiveObjects.class);
		mockCategory = mock(Risk_Categories.class);
		mockCategories = new Risk_Categories[] { mockCategory };

		when(mockActiveObjects.create(eq(Risk_Categories.class), (DBParam[]) anyVararg())).thenReturn(mockCategory);
		when(mockActiveObjects.find(eq(Risk_Categories.class))).thenReturn(mockCategories);
	}

	@Test
	public void testAdd() {
		RiskCategoryService test = new RiskCategoryServiceImpl(mockActiveObjects);
		Risk_Categories result = test.add("blah", "desc");

		assertEquals(mockCategory, result);

		verify(mockCategory).setValue("blah");
		verify(mockCategory).setRiskDesc("desc");
		verify(mockCategory).save();
	}

	@Test
	public void testGetRiskByID() {
		Risk_Categories mockCategory2 = mock(Risk_Categories.class);
		Risk_Categories[] mockLikelihoods = { mockCategory, mockCategory2 };

		when(mockActiveObjects.find(eq(Risk_Categories.class), any(Query.class))).thenReturn(mockLikelihoods);

		RiskCategoryService test = new RiskCategoryServiceImpl(mockActiveObjects);

		Risk_Categories result = test.getRiskByID("blah");

		assertTrue(RiskCategoryServiceImpl.isInitialized());
		verify(mockActiveObjects).find(eq(Risk_Categories.class), any(Query.class));
		assertEquals(result, mockCategory);

	}

	@Test
	public void testGetRiskByIDNoneFound() {
		Risk_Categories[] mockLikelihoods = {};

		when(mockActiveObjects.find(eq(Risk_Categories.class), any(Query.class))).thenReturn(mockLikelihoods);

		RiskCategoryService test = new RiskCategoryServiceImpl(mockActiveObjects);

		Risk_Categories result = test.getRiskByID("blah");

		assertTrue(RiskCategoryServiceImpl.isInitialized());
		assertNull(result);

	}

	@Test
	public void testAllNoneFound() {
		Risk_Categories[] mockLikelihoods = {};

		when(mockActiveObjects.find(eq(Risk_Categories.class))).thenReturn(mockLikelihoods);

		RiskCategoryService test = new RiskCategoryServiceImpl(mockActiveObjects);
		List<Risk_Categories> result = test.all();

		assertTrue(RiskCategoryServiceImpl.isInitialized());
		assertTrue(result.isEmpty());

	}

	@Test
	public void testAllMultipleFound() {
		Risk_Categories[] mockLikelihoods = { mockCategory, mockCategory };

		ActiveObjects mockActiveObjects = mock(ActiveObjects.class);

		when(mockActiveObjects.find(eq(Risk_Categories.class))).thenReturn(mockLikelihoods);

		RiskCategoryService test = new RiskCategoryServiceImpl(mockActiveObjects);
		List<Risk_Categories> result = test.all();

		assertTrue(RiskCategoryServiceImpl.isInitialized());
		assertEquals(result.size(), 2);
	}

	@Test
	public void testInitializeTables() {
		Risk_Categories[] mockCategories = {};
		when(mockActiveObjects.find(eq(Risk_Categories.class))).thenReturn(mockCategories);

		RiskCategoryServiceImpl test = new RiskCategoryServiceImpl(mockActiveObjects);
		test.initializeTable();

		verify(mockActiveObjects, times(4)).create(eq(Risk_Categories.class), (DBParam[]) anyVararg());

		verify(mockCategory).setValue("I - Catastrophic");
		verify(mockCategory)
				.setRiskDesc(
						"May cause death, equipment loss > $1M, unit downtime > 4 month, data is never recoverable or primary program objectives are lost");
		verify(mockCategory).setValue("II - Critical");
		verify(mockCategory)
				.setRiskDesc(
						"May  cuase severe injury or severe occupational illness, equipment loss 200K-1M, unit downtime 2 weeks to 4 months, may cause repeat of test program");
		verify(mockCategory).setValue("III - Marginal");
		verify(mockCategory)
				.setRiskDesc(
						"May cause minor injury or minor occupational illness, equipment loss 10K to 200K, unit downtime 1 day to 2 weeks, may cause repeat of test period");
		verify(mockCategory).setValue("IV - Negligible");
		verify(mockCategory)
				.setRiskDesc(
						"Will not result in injury or occupational illness, equipment loss < 10K, unit downtime < 1 day, may cause repeat of data point or data may require minor manipulation or computer rerun");
	}

	@Test
	public void testInitializeTablesAlreadyInitialized() {

		Risk_Categories[] mockCategories = {};
		when(mockActiveObjects.find(eq(Risk_Categories.class))).thenReturn(mockCategories);

		RiskCategoryServiceImpl test = new RiskCategoryServiceImpl(mockActiveObjects);
		test.initializeTable();
		test.initializeTable();

		verify(mockActiveObjects, times(4)).create(eq(Risk_Categories.class), (DBParam[]) anyVararg());

		verify(mockCategory).setValue("I - Catastrophic");
		verify(mockCategory)
				.setRiskDesc(
						"May cause death, equipment loss > $1M, unit downtime > 4 month, data is never recoverable or primary program objectives are lost");
		verify(mockCategory).setValue("II - Critical");
		verify(mockCategory)
				.setRiskDesc(
						"May  cuase severe injury or severe occupational illness, equipment loss 200K-1M, unit downtime 2 weeks to 4 months, may cause repeat of test program");
		verify(mockCategory).setValue("III - Marginal");
		verify(mockCategory)
				.setRiskDesc(
						"May cause minor injury or minor occupational illness, equipment loss 10K to 200K, unit downtime 1 day to 2 weeks, may cause repeat of test period");
		verify(mockCategory).setValue("IV - Negligible");
		verify(mockCategory)
				.setRiskDesc(
						"Will not result in injury or occupational illness, equipment loss < 10K, unit downtime < 1 day, may cause repeat of data point or data may require minor manipulation or computer rerun");
	}

	@Test
	public void testIsInitialized() {
		assertFalse(RiskCategoryServiceImpl.isInitialized());
	}

	@Test
	public void testReset() {
		RiskCategoryServiceImpl test = new RiskCategoryServiceImpl(mockActiveObjects);
		test.initializeTable();

		assertTrue(RiskCategoryServiceImpl.isInitialized());
		RiskCategoryServiceImpl.reset();
		assertFalse(RiskCategoryServiceImpl.isInitialized());
	}
}
