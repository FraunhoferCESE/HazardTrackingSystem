package ut.org.fraunhofer.plugins.hts.rest;

import org.junit.Test;
import org.junit.After;
import org.junit.Before;
import org.mockito.Mockito;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import org.fraunhofer.plugins.hts.rest.HazardResource;
import org.fraunhofer.plugins.hts.rest.HazardResourceModel;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.GenericEntity;

public class HazardResourceTest {

    @Before
    public void setup() {

    }

    @After
    public void tearDown() {

    }

    @Test
    public void messageIsValid() {
        HazardResource resource = new HazardResource();

        Response response = resource.getMessage();
        final HazardResourceModel message = (HazardResourceModel) response.getEntity();

        assertEquals("wrong message","Hello World",message.getMessage());
    }
}
