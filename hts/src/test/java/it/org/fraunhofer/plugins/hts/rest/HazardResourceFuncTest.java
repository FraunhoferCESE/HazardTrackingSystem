package it.org.fraunhofer.plugins.hts.rest;

import org.junit.Test;
import org.junit.After;
import org.junit.Before;
import org.mockito.Mockito;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import org.fraunhofer.plugins.hts.rest.HazardResource;
import org.fraunhofer.plugins.hts.rest.HazardResourceModel;
import org.apache.wink.client.Resource;
import org.apache.wink.client.RestClient;

public class HazardResourceFuncTest {

    @Before
    public void setup() {

    }

    @After
    public void tearDown() {

    }

    @Test
    public void messageIsValid() {

        String baseUrl = System.getProperty("baseurl");
        String resourceUrl = baseUrl + "/rest/htsrest/1.0/message";

        RestClient client = new RestClient();
        Resource resource = client.resource(resourceUrl);

        HazardResourceModel message = resource.get(HazardResourceModel.class);

        assertEquals("wrong message","Hello World",message.getMessage());
    }
}
