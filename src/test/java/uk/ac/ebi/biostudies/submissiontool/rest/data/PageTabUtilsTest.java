/*
 * Copyright (c) 2017 European Molecular Biology Laboratory
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

package uk.ac.ebi.biostudies.submissiontool.rest.data;

import com.fasterxml.jackson.databind.JsonNode;
import jersey.repackaged.com.google.common.collect.Lists;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static org.junit.Assert.assertEquals;
import static uk.ac.ebi.biostudies.submissiontool.rest.data.Json.objectMapper;
import static uk.ac.ebi.biostudies.submissiontool.rest.data.PageTabUtils.*;

/**
 * @author olkin
 */
public class PageTabUtilsTest {

    private static JsonNode pageTab;

    @BeforeClass
    public static void loadPageTab() throws URISyntaxException, IOException {
        URL url = PageTabUtilsTest.class.getResource("submission.json");
        String json = new String(Files.readAllBytes(Paths.get(url.toURI())));
        pageTab = objectMapper().readTree(json);
    }

    @Test
    public void titleAttrTest() {
        assertEquals(titleAttr(pageTab), "Submission Title");
    }

    @Test
    public void accnoFieldTest() {
        assertEquals(accnoField(pageTab), "S-BSST43");
    }

    @Test
    public void rtimeInSecondsAttrTest() throws ParseException {
        assertEquals(rtimeInSecondsAttr(pageTab).longValue(), toSeconds("2017-04-19"));
    }

    @Test
    public void attachToAttrTest() {
        assertEquals(attachToAttr(pageTab), Lists.newArrayList("A1", "A2", "A3"));
    }

    @Test
    public void accnoTemplateAttrTest() {
        assertEquals(accnoTemplateAttr(pageTab), "!{S-BSST}");
    }

    private static long toSeconds(String date) throws ParseException {
        Date d = new SimpleDateFormat("yyyy-MM-dd").parse(date);
        return d.getTime() / 1000;
    }
}
