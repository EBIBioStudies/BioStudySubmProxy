package uk.ac.ebi.biostudy.submission;

import org.apache.camel.component.servletlistener.CamelContextLifecycle;
import org.apache.camel.component.servletlistener.ServletCamelContext;
import org.apache.camel.impl.JndiRegistry;

public class MyLifeCycle implements CamelContextLifecycle<JndiRegistry> {

	public void beforeStart(ServletCamelContext camelContext, JndiRegistry registry) throws Exception {
	}

	public void beforeStop(ServletCamelContext camelContext, JndiRegistry registry) throws Exception {
	}

	public void afterStop(ServletCamelContext camelContext, JndiRegistry registry) throws Exception {
		// noop
	}

	public void beforeAddRoutes(ServletCamelContext camelContext, JndiRegistry registry) throws Exception {
		// noop
	}

	public void afterAddRoutes(ServletCamelContext camelContext, JndiRegistry registry) throws Exception {
		// noop
	}

	public void afterStart(ServletCamelContext camelContext, JndiRegistry registry) throws Exception {
		// noop
	}
}