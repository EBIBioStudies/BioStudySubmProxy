package uk.ac.ebi.biostudy.submission;

import org.mapdb.DB;

public class ProxyDataServletForTest extends ProxyDataServlet {
	private DB db;

	public ProxyDataServletForTest(DB db) {
		super();
		this.db = db;
	}

	@Override
	public DB getDb() {
		return db;
	}
}
