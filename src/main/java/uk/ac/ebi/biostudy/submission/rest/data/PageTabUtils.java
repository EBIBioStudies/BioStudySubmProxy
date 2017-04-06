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

package uk.ac.ebi.biostudy.submission.rest.data;

import com.fasterxml.jackson.databind.JsonNode;
import jersey.repackaged.com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * @author olkin
 */
public class PageTabUtils {
    private static final Logger logger = LoggerFactory.getLogger(PageTabUtils.class);

    private static final DateFormat format = new SimpleDateFormat("yyyy-MM-dd");

    public static String accno(JsonNode subm) {
        return subm.get("accno").asText();
    }

    public static String title(JsonNode subm) {
        return attributeValue(attributes(subm), "title");
    }

    public static Long rtimeInSeconds(JsonNode subm) {
        return seconds(attributeValue(attributes(subm), "releaseDate"));
    }

    private static List<JsonNode> attributes(JsonNode node) {
        final String attributesName = "attributes";
        if (!node.has(attributesName)) {
            return Collections.emptyList();
        }

        JsonNode attributesNode = node.get(attributesName);
        if (!attributesNode.isArray()) {
            return Collections.emptyList();
        }
        List<JsonNode> list = new ArrayList<>();
        attributesNode.iterator().forEachRemaining(list::add);
        return list;
    }

    private static String attributeValue(List<JsonNode> attributes, String attrName) {
        Optional<String> opt = attributes.stream()
                .filter(node -> getAttrName(node).equalsIgnoreCase(attrName))
                .map(node -> getAttrValue(node))
                .findFirst();
        return opt.orElse("");
    }

    private static String getAttrName(JsonNode node) {
        return getNodeField("name", node);
    }

    private static String getAttrValue(JsonNode node) {
        return getNodeField("value", node);
    }

    private static String getNodeField(String fieldName, JsonNode node) {
        Optional<String> name = Lists
                .newArrayList(node.fieldNames())
                .stream()
                .filter(k -> (k).equalsIgnoreCase(fieldName))
                .findFirst();
        return name.map(s -> node.get(s).asText()).orElse("");
    }

    private static Long seconds(String value) {
        try {
            if (value != null && !value.isEmpty()) {
                return format.parse(value).getTime() / 1000;
            }
        } catch (ParseException e) {
            logger.error("Data format error: {}", value);
        }
        return null;
    }
}
