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

import org.fraunhofer.plugins.hts.db.Mission_Phase;
import org.fraunhofer.plugins.hts.db.service.MissionPhaseService;
import org.fraunhofer.plugins.hts.db.service.impl.MissionPhaseServiceImpl;
import org.junit.Before;
import org.junit.Test;

import com.atlassian.activeobjects.external.ActiveObjects;

public class MissionPhaseServiceImplTest {
	
	Mission_Phase mockPhase;
	Mission_Phase[] mockMissionPhases;
	ActiveObjects mockActiveObjects;
	
	@Before
	public void setUp() {
		MissionPhaseServiceImpl.reset();
		
		mockPhase = mock(Mission_Phase.class);
		mockMissionPhases = new Mission_Phase[] { mockPhase };
		mockActiveObjects = mock(ActiveObjects.class);
		
		when(mockActiveObjects.create(eq(Mission_Phase.class), (DBParam[]) anyVararg())).thenReturn(mockPhase);
		when(mockActiveObjects.find(eq(Mission_Phase.class))).thenReturn(mockMissionPhases);
	}
	
	@Test
	public void testAdd() {
		MissionPhaseService test = new MissionPhaseServiceImpl(mockActiveObjects);
		Mission_Phase result = test.add("blah");
		
		assertEquals(mockPhase, result);
		
		verify(mockPhase).setLabel("blah");
		verify(mockPhase).save();
	}
	
	@Test
	public void testGetMissionPhaseByID() {
		Mission_Phase mockGroup2 = mock(Mission_Phase.class);
		Mission_Phase[] mockMissionPhases = { mockPhase, mockGroup2 };
		
		when(mockActiveObjects.find(eq(Mission_Phase.class), any(Query.class))).thenReturn(mockMissionPhases);
		
		MissionPhaseService test = new MissionPhaseServiceImpl(mockActiveObjects);
		Mission_Phase result = test.getMissionPhaseByID("blah");
		
		assertTrue(MissionPhaseServiceImpl.isInitialized());
		verify(mockActiveObjects).find(eq(Mission_Phase.class), any(Query.class));
		assertEquals(result, mockPhase);
	}
	
	@Test
	public void testGetMissionPhasesByIDNoneFound() {
		Mission_Phase[] mockMissionPhases = {};
		int[] ids = { 85 };
		
		when(mockActiveObjects.find(eq(Mission_Phase.class), any(Query.class))).thenReturn(mockMissionPhases);
		
		MissionPhaseService test = new MissionPhaseServiceImpl(mockActiveObjects);
		Mission_Phase[] result = test.getMissionPhasesByID(ids);
		
		assertTrue(MissionPhaseServiceImpl.isInitialized());
		verify(mockActiveObjects).get(eq(Mission_Phase.class), eq(ids[0]));
		assertNull(result[0]);
	}
	
	@Test
	public void testGetMissionPhaseByIDNoneFound() {
		Mission_Phase[] mockMissionPhases = {};
		
		when(mockActiveObjects.find(eq(Mission_Phase.class), any(Query.class))).thenReturn(mockMissionPhases);
		
		MissionPhaseService test = new MissionPhaseServiceImpl(mockActiveObjects);
		Mission_Phase result = test.getMissionPhaseByID("blah");
		
		assertTrue(MissionPhaseServiceImpl.isInitialized());
		verify(mockActiveObjects).find(eq(Mission_Phase.class), any(Query.class));
		assertNull(result);
	}
	
	@Test
	public void testAllNoneFound() {
		Mission_Phase[] mockMissionPhases = {};
		
		when(mockActiveObjects.find(eq(Mission_Phase.class))).thenReturn(mockMissionPhases);
		
		MissionPhaseService test = new MissionPhaseServiceImpl(mockActiveObjects);
		List<Mission_Phase> result = test.all();
		
		assertTrue(MissionPhaseServiceImpl.isInitialized());
		assertTrue(result.isEmpty());
	}
	
	@Test
	public void testAllMultipleFound() {
		Mission_Phase[] mockMissionPhases = { mockPhase, mockPhase, mockPhase };
		
		when(mockActiveObjects.find(eq(Mission_Phase.class))).thenReturn(mockMissionPhases);
		
		MissionPhaseService test = new MissionPhaseServiceImpl(mockActiveObjects);
		List<Mission_Phase> result = test.all();
		
		assertTrue(MissionPhaseServiceImpl.isInitialized());
		assertEquals(result.size(), 3);
	}
	
	@Test
	public void testInitializeTables() {
		Mission_Phase[] mockPhases = {};
		when(mockActiveObjects.find(eq(Mission_Phase.class))).thenReturn(mockPhases);
		
		MissionPhaseServiceImpl test = new MissionPhaseServiceImpl(mockActiveObjects);
		test.initializeTable();
		
		verify(mockActiveObjects, times(6)).create(eq(Mission_Phase.class), (DBParam[]) anyVararg());
		
		verify(mockPhase).setLabel("Pre-launch");
		verify(mockPhase).setLabel("Launch");
		verify(mockPhase).setLabel("Cruise");
		verify(mockPhase).setLabel("Encounter");
		verify(mockPhase).setLabel("Extended Operations");
		verify(mockPhase).setLabel("Decommissioning");
	}
	
	@Test
	public void testInitializeTablesAlreadyInitilized() {
		Mission_Phase[] mockPhases = {};
		when(mockActiveObjects.find(eq(Mission_Phase.class))).thenReturn(mockPhases);
		
		MissionPhaseServiceImpl test = new MissionPhaseServiceImpl(mockActiveObjects);
		test.initializeTable();
		test.initializeTable();
		
		verify(mockActiveObjects, times(6)).create(eq(Mission_Phase.class), (DBParam[]) anyVararg());
		
		verify(mockPhase).setLabel("Pre-launch");
		verify(mockPhase).setLabel("Launch");
		verify(mockPhase).setLabel("Cruise");
		verify(mockPhase).setLabel("Encounter");
		verify(mockPhase).setLabel("Extended Operations");
		verify(mockPhase).setLabel("Decommissioning");
	}
	
	@Test
	public void testIsInitialized() {
		assertFalse(MissionPhaseServiceImpl.isInitialized());
	}
	
	@Test
	public void testReset() {
		MissionPhaseServiceImpl test = new MissionPhaseServiceImpl(mockActiveObjects);
		test.initializeTable();
		
		assertTrue(MissionPhaseServiceImpl.isInitialized());
		MissionPhaseServiceImpl.reset();
		assertFalse(MissionPhaseServiceImpl.isInitialized());
	}
}
