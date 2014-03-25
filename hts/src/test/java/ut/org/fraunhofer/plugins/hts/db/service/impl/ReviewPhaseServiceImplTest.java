package ut.org.fraunhofer.plugins.hts.db.service.impl;

import static org.mockito.Mockito.*;  

import org.fraunhofer.plugins.hts.db.service.impl.ReviewPhaseServiceImpl;
import org.junit.Test;
import org.junit.After;
import org.junit.Before;

import com.atlassian.activeobjects.external.ActiveObjects;

public class ReviewPhaseServiceImplTest {
	
	private ActiveObjects mockActiveObjects;
	
	@Before
	public void setup() {
		mockActiveObjects = mock(ActiveObjects.class);
	}
	
	@Test
	public void testInitializeTables() {
		//ReviewPhaseServiceImpl test = new ReviewPhaseServiceImpl(mockActiveObjects);
	}

}
