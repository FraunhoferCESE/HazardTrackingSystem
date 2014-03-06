package ut.org.fraunhofer.plugins.hts.servlet;

import org.junit.Test;
import org.junit.After;
import org.junit.Before;
import org.mockito.Matchers;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class HazardsTest {

    HttpServletRequest mockRequest;
    HttpServletResponse mockResponse;

    @Before
    public void setup() {
        mockRequest = mock(HttpServletRequest.class);
        mockResponse = mock(HttpServletResponse.class);
    }

    @After
    public void tearDown() {

    }

    @Test
    public void testSomething() {
        String expected = "test";
        when(mockRequest.getParameter(Matchers.anyString())).thenReturn(expected);
        assertEquals(expected,mockRequest.getParameter("some string"));

    }
}
