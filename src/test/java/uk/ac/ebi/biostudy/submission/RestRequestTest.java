package uk.ac.ebi.biostudy.submission;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Test;

public class RestRequestTest {
	final static Pattern regDelete = Pattern.compile("/submission/([A-Z0-9-]*)");
	final static Pattern regDeleteSubmited = Pattern.compile("/submission/submited/([A-Z0-9-]*)");

	@Test
	public void test() {
		String path = "/submission/submited/A-SMB-2";
		Matcher matcherDelete = regDelete.matcher(path);
		Matcher matcherDeleteSubm = regDeleteSubmited.matcher(path);
		Pattern splitReg = Pattern.compile("/");
		if (matcherDelete.matches()) {
			// System.out.println(matcher.groupCount());
			String acc = matcherDelete.group(1);

			System.out.println(acc);
			String[] array = splitReg.split(path);
			for (String string : array) {
				System.out.println(string);

			}
		} else if (matcherDeleteSubm.matches()) {
			String acc = matcherDeleteSubm.group(1);
			System.out.println(acc);
			String[] array = splitReg.split(path);
			for (String string : array) {
				System.out.println(string);

			}
		} else {
			System.out.println("not found");

		}
	}

}
