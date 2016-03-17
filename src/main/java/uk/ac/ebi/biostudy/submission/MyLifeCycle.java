/*
 * Copyright (c) 2016 European Molecular Biology Laboratory
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or impl
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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