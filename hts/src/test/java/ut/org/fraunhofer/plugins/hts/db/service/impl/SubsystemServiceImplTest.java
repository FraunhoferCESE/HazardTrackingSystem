package ut.org.fraunhofer.plugins.hts.db.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import net.java.ao.DBParam;
import net.java.ao.Query;

import org.fraunhofer.plugins.hts.db.Subsystems;
import org.fraunhofer.plugins.hts.db.service.SubsystemService;
import org.fraunhofer.plugins.hts.db.service.impl.SubsystemServiceImpl;
import org.junit.Before;
import org.junit.Test;

import com.atlassian.activeobjects.external.ActiveObjects;

public class SubsystemServiceImplTest {

	Subsystems mockSubsystem;
	Subsystems[] mockSubsystems;
	ActiveObjects mockActiveObjects;

	@Before
	public void setUp() {
		SubsystemServiceImpl.reset();

		mockSubsystem = mock(Subsystems.class);
		mockSubsystems = new Subsystems[] { mockSubsystem };
		mockActiveObjects = mock(ActiveObjects.class);

		when(mockActiveObjects.create(eq(Subsystems.class), (DBParam[]) anyVararg())).thenReturn(mockSubsystem);
		when(mockActiveObjects.find(eq(Subsystems.class))).thenReturn(mockSubsystems);
	}

	@Test
	public void testAdd() {
		SubsystemServiceImpl test = new SubsystemServiceImpl(mockActiveObjects);
		Subsystems result = test.add("blah", "blah");

		assertEquals(mockSubsystem, result);

		verify(mockSubsystem).setLabel("blah");
		verify(mockSubsystem).setDescription("blah");
		verify(mockSubsystem).save();
	}

	@Test
	public void testGetSubSystemByID() {
		Subsystems mockSubsystem2 = mock(Subsystems.class);
		Subsystems[] mockSubsystems = { mockSubsystem, mockSubsystem2 };

		when(mockActiveObjects.find(eq(Subsystems.class), any(Query.class))).thenReturn(mockSubsystems);

		SubsystemServiceImpl test = new SubsystemServiceImpl(mockActiveObjects);
		Subsystems result = test.getSubsystemByID("blah");

		assertTrue(SubsystemServiceImpl.isInitialized());
		verify(mockActiveObjects).find(eq(Subsystems.class), any(Query.class));
		assertEquals(result, mockSubsystem);

	}

	@Test
	public void testGetSubSystemIDNoneFound() {
		Subsystems[] mockSubsystems = {};

		when(mockActiveObjects.find(eq(Subsystems.class), any(Query.class))).thenReturn(mockSubsystems);

		SubsystemServiceImpl test = new SubsystemServiceImpl(mockActiveObjects);
		Subsystems result = test.getSubsystemByID("blah");

		assertTrue(SubsystemServiceImpl.isInitialized());
		verify(mockActiveObjects).find(eq(Subsystems.class), any(Query.class));
		assertNull(result);

	}
	
	@Test
	public void testGetSubsystemsByIDNoneFound() {
		Subsystems[] mockSubsystems = {};
		Integer[] ids = null;
		
		when(mockActiveObjects.find(eq(Subsystems.class), any(Query.class))).thenReturn(mockSubsystems);
		
		SubsystemService test = new SubsystemServiceImpl(mockActiveObjects);
		Subsystems[] result = test.getSubsystemsByID(ids);
		
		assertTrue(SubsystemServiceImpl.isInitialized());
		assertNull(result);
	}

	@Test
	public void testGetSubsystemsByIDMultipleFound() {
		Subsystems mockSubsystem2 = mock(Subsystems.class);
		Subsystems mockSubsystem3 = mock(Subsystems.class);
		Subsystems[] mockSubsystems = { mockSubsystem, mockSubsystem2, mockSubsystem2 };
		Integer[] ids = { mockSubsystem2.getID(), mockSubsystem3.getID() };
		
		when(mockActiveObjects.find(eq(Subsystems.class), any(Query.class))).thenReturn(mockSubsystems);
		
		SubsystemService test = new SubsystemServiceImpl(mockActiveObjects);
		Subsystems[] result = test.getSubsystemsByID(ids);
		
		assertTrue(SubsystemServiceImpl.isInitialized());
		verify(mockActiveObjects, times(2)).get(eq(Subsystems.class), anyInt());
		assertEquals(2, result.length);
	}
	
	@Test
	public void testGetRemainingSubsystems() {
		Subsystems mockSubsystem2 = mock(Subsystems.class);
		Subsystems[] mockSubsystems = { mockSubsystem, mockSubsystem2 };
		Subsystems[] mockList = { mockSubsystem2 };
		
		when(mockActiveObjects.find(eq(Subsystems.class), any(Query.class))).thenReturn(mockSubsystems);
		
		SubsystemService test = new SubsystemServiceImpl(mockActiveObjects);
		List<Subsystems> result = test.getRemainingGroups(mockList);
		
		assertTrue(SubsystemServiceImpl.isInitialized());
		assertEquals(1, result.size());
	}
	
	@Test
	public void testGetRemainingMissionPhasesAllRemoved() {
		Subsystems[] mockSubsystems = { mockSubsystem, mockSubsystem };
		
		when(mockActiveObjects.find(eq(Subsystems.class), any(Query.class))).thenReturn(mockSubsystems);
		
		SubsystemService test = new SubsystemServiceImpl(mockActiveObjects);
		List<Subsystems> result = test.getRemainingGroups(mockSubsystems);
		
		assertTrue(SubsystemServiceImpl.isInitialized());
		assertEquals(0, result.size());
	}
	
	@Test
	public void testGetRemainingMissionPhasesEmptyList() {
		Subsystems[] mockSubsystems = {};
		Subsystems[] mockList = { mockSubsystem };
		
		when(mockActiveObjects.find(eq(Subsystems.class))).thenReturn(mockSubsystems);
		
		SubsystemService test = new SubsystemServiceImpl(mockActiveObjects);
		List<Subsystems> result = test.getRemainingGroups(mockList);
		
		assertTrue(SubsystemServiceImpl.isInitialized());
		assertTrue(result.isEmpty());
	}

	@Test
	public void testAllNoneFound() {
		Subsystems[] mockSubsystems = {};

		when(mockActiveObjects.find(eq(Subsystems.class))).thenReturn(mockSubsystems);

		SubsystemServiceImpl test = new SubsystemServiceImpl(mockActiveObjects);
		List<Subsystems> result = test.all();

		assertTrue(SubsystemServiceImpl.isInitialized());
		assertTrue(result.isEmpty());
	}

	@Test
	public void testAllMultipleFound() {
		Subsystems[] mockSubsystems = { mockSubsystem, mockSubsystem, mockSubsystem };

		when(mockActiveObjects.find(eq(Subsystems.class))).thenReturn(mockSubsystems);

		SubsystemServiceImpl test = new SubsystemServiceImpl(mockActiveObjects);
		List<Subsystems> result = test.all();

		assertTrue(SubsystemServiceImpl.isInitialized());
		assertEquals(result.size(), 3);
	}

	@Test
	public void testInitializeTables() {
		Subsystems[] mockSubsystems = {};
		when(mockActiveObjects.find(eq(Subsystems.class))).thenReturn(mockSubsystems);

		SubsystemServiceImpl test = new SubsystemServiceImpl(mockActiveObjects);
		test.initializeTable();

		verify(mockActiveObjects, times(17)).create(eq(Subsystems.class), (DBParam[]) anyVararg());

		verify(mockSubsystem).setLabel("Structure");
		verify(mockSubsystem).setLabel("Power");
		verify(mockSubsystem).setLabel("Communications");
		verify(mockSubsystem).setLabel("Propulsion");
		verify(mockSubsystem).setLabel("Guidance and Control");
		verify(mockSubsystem).setLabel("Instrument");
		verify(mockSubsystem).setLabel("Ordnance/Pyro");
		verify(mockSubsystem).setLabel("Hazardous Materials");
		verify(mockSubsystem).setLabel("GSE Electrical Materials");
		verify(mockSubsystem).setLabel("GSE Mechanical Materials");
		verify(mockSubsystem).setLabel("System Interfaces");
		verify(mockSubsystem).setLabel("Environmental");
		verify(mockSubsystem).setLabel("Ionized Radiation");
		verify(mockSubsystem).setLabel("Non-Ionized Radiation");
		verify(mockSubsystem).setLabel("Propellants");
		verify(mockSubsystem).setLabel("Electrical/Electronics");
		verify(mockSubsystem).setLabel("Human Factors");
	}

	@Test
	public void testInitializeTablesAlreadyInitialized() {
		Subsystems[] mockSubsystems = {};
		when(mockActiveObjects.find(eq(Subsystems.class))).thenReturn(mockSubsystems);

		SubsystemServiceImpl test = new SubsystemServiceImpl(mockActiveObjects);
		test.initializeTable();
		test.initializeTable();
		
		verify(mockActiveObjects, times(17)).create(eq(Subsystems.class), (DBParam[]) anyVararg());

		verify(mockSubsystem).setLabel("Structure");
		verify(mockSubsystem).setLabel("Power");
		verify(mockSubsystem).setLabel("Communications");
		verify(mockSubsystem).setLabel("Propulsion");
		verify(mockSubsystem).setLabel("Guidance and Control");
		verify(mockSubsystem).setLabel("Instrument");
		verify(mockSubsystem).setLabel("Ordnance/Pyro");
		verify(mockSubsystem).setLabel("Hazardous Materials");
		verify(mockSubsystem).setLabel("GSE Electrical Materials");
		verify(mockSubsystem).setLabel("GSE Mechanical Materials");
		verify(mockSubsystem).setLabel("System Interfaces");
		verify(mockSubsystem).setLabel("Environmental");
		verify(mockSubsystem).setLabel("Ionized Radiation");
		verify(mockSubsystem).setLabel("Non-Ionized Radiation");
		verify(mockSubsystem).setLabel("Propellants");
		verify(mockSubsystem).setLabel("Electrical/Electronics");
		verify(mockSubsystem).setLabel("Human Factors");	}

	@Test
	public void testIsInitialized() {
		assertFalse(SubsystemServiceImpl.isInitialized());
	}

	@Test
	public void testReset() {
		SubsystemServiceImpl test = new SubsystemServiceImpl(mockActiveObjects);
		test.initializeTable();

		assertTrue(SubsystemServiceImpl.isInitialized());
		SubsystemServiceImpl.reset();
		assertFalse(SubsystemServiceImpl.isInitialized());
	}
}
