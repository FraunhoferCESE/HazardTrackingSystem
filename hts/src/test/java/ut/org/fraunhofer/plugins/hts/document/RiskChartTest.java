package ut.org.fraunhofer.plugins.hts.document;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

import java.util.List;

import org.fraunhofer.plugins.hts.document.RiskChart;
import org.junit.Before;
import org.junit.Test;

public class RiskChartTest {

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testConstructor() {
		RiskChart test = new RiskChart(5, 4);
		assertEquals(4, test.getnCols());
		assertEquals(5, test.getnRows());
		assertNotNull(test.getChart());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testConstructorTooSmallRow() {
		new RiskChart(0, 5);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testConstructorTooSmallColumn() {
		new RiskChart(5, 0);
	}

	@Test
	public void testAddToChart() {
		RiskChart test = new RiskChart(5, 5);
		test.addToChart(5, 5, "test");
		List<String> result = test.getChart().get(5, 5);
		assertNotNull(result);
		assertEquals(1, result.size());
		assertEquals("test", result.get(0));

		test.addToChart(5, 5, "test #2");
		assertEquals(2, result.size());
		assertEquals("test #2", result.get(1));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testAddToChartRowsTooLow() {
		RiskChart test = new RiskChart(5, 5);
		test.addToChart(0, 5, "test");
	}

	@Test(expected = IllegalArgumentException.class)
	public void testAddToChartRowsTooHigh() {
		RiskChart test = new RiskChart(5, 5);
		test.addToChart(6, 5, "test");
	}

	@Test(expected = IllegalArgumentException.class)
	public void testAddToChartColsTooLow() {
		RiskChart test = new RiskChart(5, 5);
		test.addToChart(5, 0, "test");
	}

	@Test(expected = IllegalArgumentException.class)
	public void testAddToChartColsTooHigh() {
		RiskChart test = new RiskChart(5, 5);
		test.addToChart(5, 6, "test");
	}

	@Test(expected = NullPointerException.class)
	public void testAddToChartNullValue() {
		RiskChart test = new RiskChart(5, 5);
		test.addToChart(5, 5, null);
	}

	@Test
	public void testRemoveFromChart() {
		RiskChart test = new RiskChart(5, 5);
		test.addToChart(5, 5, "test");
		assertTrue(test.removeFromChart(5, 5, "test"));
		assertFalse(test.removeFromChart(5, 5, "test"));
	}

	@Test
	public void testRemoveFromChartMultiple() {
		RiskChart test = new RiskChart(5, 5);
		test.addToChart(5, 5, "test");
		test.addToChart(5, 5, "test");
		assertTrue(test.removeFromChart(5, 5, "test"));
		assertEquals(1, test.getChart().get(5, 5).size());
		assertTrue(test.removeFromChart(5, 5, "test"));
		assertFalse(test.removeFromChart(5, 5, "test"));
	}

	@Test
	public void testRemoveFromChartEmpty() {
		RiskChart test = new RiskChart(5, 5);
		assertFalse(test.removeFromChart(5, 5, "test"));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testRemoveFromChartTooLowRows() {
		RiskChart test = new RiskChart(5, 5);
		test.removeFromChart(0, 5, "test");
	}

	@Test(expected = IllegalArgumentException.class)
	public void testRemoveFromChartTooHighRows() {
		RiskChart test = new RiskChart(5, 5);
		test.removeFromChart(6, 5, "test");
	}

	@Test(expected = IllegalArgumentException.class)
	public void testRemoveFromChartTooLowColumns() {
		RiskChart test = new RiskChart(5, 5);
		test.removeFromChart(5, 0, "test");
	}

	@Test(expected = IllegalArgumentException.class)
	public void testRemoveFromChartTooHighColumns() {
		RiskChart test = new RiskChart(5, 5);
		test.removeFromChart(5, 6, "test");
	}

	@Test(expected = NullPointerException.class)
	public void testRemoveFromChartNullValue() {
		RiskChart test = new RiskChart(5, 5);
		test.removeFromChart(5, 5, null);
	}

	@Test
	public void testSaveComponentAsJPEG() {
		RiskChart test = new RiskChart(5, 5);

		for (int i = 1; i <= 5; i++)
			for (int j = 1; j <= 5; j++)
				test.addToChart(i, j, "Cause " + Integer.toString(i * j));

		test.saveComponentAsJPEG(System.getProperty("user.dir") + System.getProperty("file.separator") + "test_out"
				+ System.getProperty("file.separator") + "test.jpg");
		
		System.err.println(System.getProperty("user.dir") + System.getProperty("file.separator") + "test_out"
				+ System.getProperty("file.separator") + "test.jpg");

	}

}
