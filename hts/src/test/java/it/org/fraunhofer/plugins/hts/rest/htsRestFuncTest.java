package it.org.fraunhofer.plugins.hts.rest;

import org.junit.Test;
import org.junit.After;
import org.junit.Before;
import org.mockito.Mockito;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import org.fraunhofer.plugins.hts.rest.htsRest;
import org.fraunhofer.plugins.hts.rest.htsRestModel;
import org.apache.wink.client.Resource;
import org.apache.wink.client.RestClient;

public class htsRestFuncTest {

    @Before
    public void setup() {

    }

    @After
    public void tearDown() {

    }

    @Test
    public void messageIsValid() {

        String baseUrl = System.getProperty("baseurl");
        String resourceUrl = baseUrl + "/rest/hts/1.0/message";

        RestClient client = new RestClient();
        Resource resource = client.resource(resourceUrl);

        htsRestModel message = resource.get(htsRestModel.class);

        assertEquals("wrong message","Hello World",message.getMessage());
    }
}
