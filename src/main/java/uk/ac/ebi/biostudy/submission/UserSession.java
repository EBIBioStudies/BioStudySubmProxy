package uk.ac.ebi.biostudy.submission;

import java.io.File;

/**
 * 
 * @author mdylag
 *
 */
public final class UserSession {
	private String username;
	private String sessid;
	private File submissionFile;

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getSessid() {
		return sessid;
	}

	public void setSessid(String sessid) {
		this.sessid = sessid;
	}

	public void setSubmissionFile(File f) {
		this.submissionFile = f;
	}

	public File getSubmissionFile() {
		return submissionFile;
	}

}
