package ut.org.fraunhofer.plugins.hts.rest;

import org.junit.Test;
import org.junit.After;
import org.junit.Before;
import org.mockito.Mockito;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import org.fraunhofer.plugins.hts.rest.htsRest;
import org.fraunhofer.plugins.hts.rest.htsRestModel;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.GenericEntity;

public class htsRestTest {

    @Before
    public void setup() {

    }

    @After
    public void tearDown() {

    }

    @Test
    public void messageIsValid() {
        htsRest resource = new htsRest();

        Response response = resource.getMessage();
        final htsRestModel message = (htsRestModel) response.getEntity();

        assertEquals("wrong message","Hello World",message.getMessage());
    }
}
