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

import org.fraunhofer.plugins.hts.db.Hazard_Group;
import org.fraunhofer.plugins.hts.db.service.HazardGroupService;
import org.fraunhofer.plugins.hts.db.service.impl.HazardGroupServiceImpl;
import org.junit.Before;
import org.junit.Test;

import com.atlassian.activeobjects.external.ActiveObjects;

public class HazardGroupServiceImplTest {

	Hazard_Group mockGroup;
	Hazard_Group[] mockHazardGroups;
	ActiveObjects mockActiveObjects;

	@Before
	public void setUp() {
		HazardGroupServiceImpl.reset();

		mockGroup = mock(Hazard_Group.class);
		mockHazardGroups = new Hazard_Group[] { mockGroup };
		mockActiveObjects = mock(ActiveObjects.class);

		when(mockActiveObjects.create(eq(Hazard_Group.class), (DBParam[]) anyVararg())).thenReturn(mockGroup);
		when(mockActiveObjects.find(eq(Hazard_Group.class))).thenReturn(mockHazardGroups);
	}

	@Test
	public void testAdd() {
		HazardGroupService test = new HazardGroupServiceImpl(mockActiveObjects);
		Hazard_Group result = test.add("blah");

		assertEquals(mockGroup, result);

		verify(mockGroup).setLabel("blah");
		verify(mockGroup).save();
	}

	@Test
	public void testGetHazardGroupByID() {
		Hazard_Group mockGroup2 = mock(Hazard_Group.class);
		Hazard_Group[] mockHazardGroups = { mockGroup, mockGroup2 };

		when(mockActiveObjects.find(eq(Hazard_Group.class), any(Query.class))).thenReturn(mockHazardGroups);

		HazardGroupService test = new HazardGroupServiceImpl(mockActiveObjects);
		Hazard_Group result = test.getHazardGroupByID("blah");

		assertTrue(HazardGroupServiceImpl.isInitialized());
		verify(mockActiveObjects).find(eq(Hazard_Group.class), any(Query.class));
		assertEquals(result, mockGroup);

	}

	@Test
	public void testGetHazardGroupByIDNoneFound() {
		Hazard_Group[] mockHazardGroups = {};

		when(mockActiveObjects.find(eq(Hazard_Group.class), any(Query.class))).thenReturn(mockHazardGroups);

		HazardGroupService test = new HazardGroupServiceImpl(mockActiveObjects);
		Hazard_Group result = test.getHazardGroupByID("blah");

		assertTrue(HazardGroupServiceImpl.isInitialized());
		verify(mockActiveObjects).find(eq(Hazard_Group.class), any(Query.class));
		assertNull(result);

	}

	@Test
	public void testAllNoneFound() {
		Hazard_Group[] mockHazardGroups = {};

		when(mockActiveObjects.find(eq(Hazard_Group.class))).thenReturn(mockHazardGroups);

		HazardGroupService test = new HazardGroupServiceImpl(mockActiveObjects);
		List<Hazard_Group> result = test.all();

		assertTrue(HazardGroupServiceImpl.isInitialized());
		assertTrue(result.isEmpty());
	}

	@Test
	public void testAllMultipleFound() {
		Hazard_Group[] mockHazardGroups = { mockGroup, mockGroup, mockGroup };

		when(mockActiveObjects.find(eq(Hazard_Group.class))).thenReturn(mockHazardGroups);

		HazardGroupService test = new HazardGroupServiceImpl(mockActiveObjects);
		List<Hazard_Group> result = test.all();

		assertTrue(HazardGroupServiceImpl.isInitialized());
		assertEquals(result.size(), 3);
	}

	@Test
	public void testInitializeTables() {
		Hazard_Group[] mockGroups = {};
		when(mockActiveObjects.find(eq(Hazard_Group.class))).thenReturn(mockGroups);
		
		HazardGroupServiceImpl test = new HazardGroupServiceImpl(mockActiveObjects);
		test.initializeTable();

		verify(mockActiveObjects, times(14)).create(eq(Hazard_Group.class), (DBParam[]) anyVararg());

		verify(mockGroup).setLabel("Acceleration");
		verify(mockGroup).setLabel("Asphyxiation");
		verify(mockGroup).setLabel("Contamination");
		verify(mockGroup).setLabel("Corrosion");
		verify(mockGroup).setLabel("Electrical");
		verify(mockGroup).setLabel("Fire/Explosion");
		verify(mockGroup).setLabel("Impact");
		verify(mockGroup).setLabel("Injury or Illness");
		verify(mockGroup).setLabel("Noise");
		verify(mockGroup).setLabel("Pressure");
		verify(mockGroup).setLabel("Ionizing Radiation");
		verify(mockGroup).setLabel("Non-Ionizing Radiation");
		verify(mockGroup).setLabel("Temperature");
		verify(mockGroup).setLabel("Toxic");
	}

	@Test
	public void testInitializeTablesAlreadyInitialized() {
		Hazard_Group[] mockGroups = {};
		when(mockActiveObjects.find(eq(Hazard_Group.class))).thenReturn(mockGroups);
		
		HazardGroupServiceImpl test = new HazardGroupServiceImpl(mockActiveObjects);
		test.initializeTable();
		test.initializeTable();

		verify(mockActiveObjects, times(14)).create(eq(Hazard_Group.class), (DBParam[]) anyVararg());

		verify(mockGroup).setLabel("Acceleration");
		verify(mockGroup).setLabel("Asphyxiation");
		verify(mockGroup).setLabel("Contamination");
		verify(mockGroup).setLabel("Corrosion");
		verify(mockGroup).setLabel("Electrical");
		verify(mockGroup).setLabel("Fire/Explosion");
		verify(mockGroup).setLabel("Impact");
		verify(mockGroup).setLabel("Injury or Illness");
		verify(mockGroup).setLabel("Noise");
		verify(mockGroup).setLabel("Pressure");
		verify(mockGroup).setLabel("Ionizing Radiation");
		verify(mockGroup).setLabel("Non-Ionizing Radiation");
		verify(mockGroup).setLabel("Temperature");
		verify(mockGroup).setLabel("Toxic");
	}

	@Test
	public void testIsInitialized() {
		assertFalse(HazardGroupServiceImpl.isInitialized());
	}

	@Test
	public void testReset() {
		HazardGroupServiceImpl test = new HazardGroupServiceImpl(mockActiveObjects);
		test.initializeTable();

		assertTrue(HazardGroupServiceImpl.isInitialized());
		HazardGroupServiceImpl.reset();
		assertFalse(HazardGroupServiceImpl.isInitialized());
	}
}
