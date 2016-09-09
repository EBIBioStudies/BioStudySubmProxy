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

package uk.ac.ebi.biostudy.submission.stubs;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.file.DirectoryIteratorException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;


class UserDir {

    private final Path dir;

    UserDir(Path dir) {
        this.dir = dir;
    }

    JSONArray list() throws IOException {
        JSONArray array = new JSONArray();
        if (dir != null) {
            array.put(transform(dir));
        }
        return array;
    }

    private JSONObject transform(Path path) throws IOException {
        boolean isDir = path.toFile().isDirectory();

        JSONObject obj = new JSONObject();
        obj.put("name", path.getFileName().toString());
        obj.put("path", path.toAbsolutePath().toString());
        obj.put("type", isDir ? "DIR" : "FILE");
        if (isDir) {
            obj.put("files", list(path));
        }
        return obj;
    }

    private JSONArray list(Path path) throws IOException {
        final JSONArray array = new JSONArray();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(path)) {
            for (Path entry : stream) {
                array.put(transform(entry));
            }
        } catch (DirectoryIteratorException ex) {
            throw ex.getCause();
        }
        return array;
    }
}
