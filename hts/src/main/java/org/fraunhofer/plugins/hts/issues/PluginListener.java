package org.fraunhofer.plugins.hts.issues;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

public class PluginListener implements InitializingBean, DisposableBean {

    @Override
    public void destroy() throws Exception {
        //Handle plugin disabling or un-installation here    
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        //Handle plugin enabling or installation here
    }
}

