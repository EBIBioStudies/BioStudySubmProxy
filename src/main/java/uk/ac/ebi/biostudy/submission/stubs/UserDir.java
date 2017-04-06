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

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryIteratorException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;


class UserDir {

  /*  private final Path root;

    UserDir(Path root) {
        this.root = root;
    }

    JSONArray list(String path, int depth) throws IOException {
        if (root != null) {
            Path p = root.resolve(path.replaceAll("^/", ""));
            if (p.toFile().exists()) {
                return list(p, depth);
            }
        }
        return new JSONArray();
    }

    boolean deleteFile(String filePath) {
        Path path = root.resolve(filePath.replaceAll("^/", ""));
        File f = path.toFile();
        return !f.exists() || f.delete();
    }

    private JSONObject transform(Path path, int depth) throws IOException {
        boolean isDir = path.toFile().isDirectory();

        JSONObject obj = new JSONObject();
        obj.put("name", path.getFileName().toString());
        obj.put("path", "/" + path.subpath(root.getNameCount(), path.getNameCount()).toString());
        obj.put("type", isDir ? "DIR" : "FILE");
        if (isDir && depth > 0) {
            obj.put("files", list(path, depth));
        }
        return obj;
    }

    private JSONArray list(Path path, int depth) throws IOException {
        final JSONArray array = new JSONArray();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(path)) {
            for (Path entry : stream) {
                array.put(transform(entry, depth - 1));
            }
        } catch (DirectoryIteratorException ex) {
            throw ex.getCause();
        }
        return array;
    }*/
}
