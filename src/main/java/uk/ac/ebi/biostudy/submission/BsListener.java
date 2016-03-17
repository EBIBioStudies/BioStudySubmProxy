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

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import org.mapdb.DB;
import org.mapdb.DBMaker;

/**
 * Application Lifecycle Listener implementation class BsListener
 *
 */
@WebListener
public class BsListener implements ServletContextListener {
	private DB db;

	/**
	 * Default constructor.
	 */
	public BsListener() {
	}

	public void contextDestroyed(ServletContextEvent sce) {
		db.close();
	}

	/**
	 * @see ServletContextListener#contextInitialized(ServletContextEvent)
	 */
	public void contextInitialized(ServletContextEvent sce) {

		URI resource = null;
		try {
			resource = getClass().getResource("/").toURI();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		String path = resource.getPath();

		File file = new File(path + "/submissiondb");
		// Change this to logger
		System.out.println(file.getAbsolutePath());
		this.db = DBMaker.fileDB(file).closeOnJvmShutdown().cacheSize(128).transactionDisable().make();
		sce.getServletContext().setAttribute("db", this.db);
		System.out.println("listener" + db.toString());
	}

}
