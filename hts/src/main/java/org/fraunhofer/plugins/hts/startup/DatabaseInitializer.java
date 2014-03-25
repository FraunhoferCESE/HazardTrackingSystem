package org.fraunhofer.plugins.hts.startup;

import org.fraunhofer.plugins.hts.db.service.RiskLikelihoodsService;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import com.atlassian.event.api.EventListener;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.plugin.event.events.PluginEnabledEvent;

public class DatabaseInitializer implements InitializingBean, DisposableBean {

	private final EventPublisher eventPublisher;
	
	public DatabaseInitializer(EventPublisher eventPublisher) {
		this.eventPublisher = eventPublisher;
	}
	
	@EventListener
	public void onPluginEnabledEvent(PluginEnabledEvent event) {
		// TODO: Figure out better way to get plugin id for our plugin
		if(event.getPlugin().toString().equals("org.fraunhofer.plugins.hts.hts"))
			initializeDatabase();
	}

	private void initializeDatabase() {
//		RiskLikelihoodsService r = new Risk;
		// TODO Auto-generated method stub
		
	}

	@Override
	public void destroy() throws Exception {
		eventPublisher.unregister(this);
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		eventPublisher.register(this);
	}
}
