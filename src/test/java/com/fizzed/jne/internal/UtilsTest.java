package com.fizzed.jne.internal;

/*-
 * #%L
 * jne
 * %%
 * Copyright (C) 2016 - 2025 Fizzed, Inc
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.fizzed.crux.util.Resources;
import com.fizzed.crux.util.TemporaryPath;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.List;

import static com.fizzed.jne.internal.Utils.readFileToString;
import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;

class UtilsTest {

    @Test
    void filterLinesIfPresentInFile() throws IOException {
        final Path linesExampleShellFile = Resources.file("/com/fizzed/jne/internal/LinesExampleShell.txt");

        List<String> filteredLines;

        filteredLines = Utils.filterLinesIfPresentInFile(linesExampleShellFile, null);

        assertThat(filteredLines, hasSize(0));

        filteredLines = Utils.filterLinesIfPresentInFile(linesExampleShellFile, Collections.emptyList());

        assertThat(filteredLines, hasSize(0));

        filteredLines = Utils.filterLinesIfPresentInFile(linesExampleShellFile, asList("Not Present", "This is not either"));

        assertThat(filteredLines, hasSize(2));
        assertThat(filteredLines, hasItems("Not Present", "This is not either"));

        filteredLines = Utils.filterLinesIfPresentInFile(linesExampleShellFile, asList("Not Present", "Hello World"));

        assertThat(filteredLines, hasSize(1));
        assertThat(filteredLines, hasItems("Not Present"));

        filteredLines = Utils.filterLinesIfPresentInFile(linesExampleShellFile, asList("export PATH=\"$PATH:/test/bin\"", "Not Present", "Hello World"));

        assertThat(filteredLines, hasSize(1));
        assertThat(filteredLines, hasItems("Not Present"));
    }

    @Test
    void writeLinesForAppendingToFileWithEmptyArray() throws IOException {
        try (TemporaryPath tp = TemporaryPath.tempFile()) {
            // make the file non-empty
            Files.write(tp.getPath(), "Hello".getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

            // an empty list means we shouldn't even do anything to the file for appending
            Utils.writeLinesToFile(tp.getPath(), Collections.emptyList(), true);

            assertThat(readFileToString(tp.getPath()), is("Hello"));
        }
    }

    @Test
    void writeLinesForAppendingToFileThatHasNoNewline() throws IOException {
        try (TemporaryPath tp = TemporaryPath.tempFile()) {
            // make the file non-empty
            Files.write(tp.getPath(), "Hello".getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

            // an empty list means we shouldn't even do anything to the file for appending
            Utils.writeLinesToFile(tp.getPath(), asList("World"), true);

            assertThat(readFileToString(tp.getPath()), is("Hello\n\nWorld\n"));
        }
    }

    @Test
    void writeLinesForAppendingToFileThatHasNewline() throws IOException {
        try (TemporaryPath tp = TemporaryPath.tempFile()) {
            // make the file non-empty
            Files.write(tp.getPath(), "Hello\n".getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

            // an empty list means we shouldn't even do anything to the file for appending
            Utils.writeLinesToFile(tp.getPath(), asList("World"), true);

            assertThat(readFileToString(tp.getPath()), is("Hello\n\nWorld\n"));
        }
    }

    @Test
    void writeLinesForAppendingToFileThatHasAnEmptyLastLine() throws IOException {
        try (TemporaryPath tp = TemporaryPath.tempFile()) {
            // make the file non-empty
            Files.write(tp.getPath(), "Hello\n ".getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

            // an empty list means we shouldn't even do anything to the file for appending
            Utils.writeLinesToFile(tp.getPath(), asList("World"), true);

            assertThat(readFileToString(tp.getPath()), is("Hello\n \nWorld\n"));
        }
    }

    @Test
    void writeLinesForAppendingToFileThatHasTwoNewlines() throws IOException {
        try (TemporaryPath tp = TemporaryPath.tempFile()) {
            // make the file non-empty
            Files.write(tp.getPath(), "Hello\n\n".getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

            // an empty list means we shouldn't even do anything to the file for appending
            Utils.writeLinesToFile(tp.getPath(), asList("World"), true);

            assertThat(readFileToString(tp.getPath()), is("Hello\n\nWorld\n"));
        }
    }

    @Test
    void writeLinesForAppendingToFileThatHasTwoNewlinesAlt() throws IOException {
        try (TemporaryPath tp = TemporaryPath.tempFile()) {
            // make the file non-empty
            Files.write(tp.getPath(), "Hello\n \n".getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

            // an empty list means we shouldn't even do anything to the file for appending
            Utils.writeLinesToFile(tp.getPath(), asList("World"), true);

            assertThat(readFileToString(tp.getPath()), is("Hello\n \nWorld\n"));
        }
    }

    @Test
    void writeLinesForAppendingToFileExampleBashrcWithTwoNewlines() throws IOException {
        final Path exampleBashrcFile = Resources.file("/com/fizzed/jne/internal/ExampleBashrc.txt");
        try (TemporaryPath tp = TemporaryPath.tempFile()) {
            Files.copy(exampleBashrcFile, tp.getPath(), StandardCopyOption.REPLACE_EXISTING);

            // make this more windows unit test battleproof
            assertThat(readFileToString(tp.getPath()).replace("\r", ""), endsWith("$ \"\n\n"));

            int newlinesNeeded = Utils.newlinesNeededForAppending(tp.getPath());
            assertThat(newlinesNeeded, is(0));

            // an empty list means we shouldn't even do anything to the file for appending
            Utils.writeLinesToFile(tp.getPath(), asList("World"), true);

            assertThat(readFileToString(tp.getPath()).replace("\r", ""), endsWith("$ \"\n\nWorld\n"));
        }
    }

    @Test
    void endsWithNewlineForAppending() throws IOException {
        final Path linesExampleShellFile = Resources.file("/com/fizzed/jne/internal/LinesExampleShell.txt");
        final Path linesEmptyFile = Resources.file("/com/fizzed/jne/internal/LinesEmpty.txt");
        final Path linesExampleShellWithNewlineFile = Resources.file("/com/fizzed/jne/internal/LinesExampleShellWithNewline.txt");

        boolean endsWithNewline;

        endsWithNewline = Utils.endsWithNewlineForAppending(linesExampleShellFile);

        assertThat(endsWithNewline, is(false));

        endsWithNewline = Utils.endsWithNewlineForAppending(linesEmptyFile);

        assertThat(endsWithNewline, is(true));

        endsWithNewline = Utils.endsWithNewlineForAppending(linesExampleShellWithNewlineFile);

        assertThat(endsWithNewline, is(true));
    }

}
