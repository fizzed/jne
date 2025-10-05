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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Utils {
    static private final Logger log = LoggerFactory.getLogger(Utils.class);

    static public String readFileToString(Path file) throws IOException {
        if (file == null) {
            throw new IllegalArgumentException("Path cannot be null.");
        }

        if (!Files.exists(file)) {
            throw new FileNotFoundException("File not found: " + file);
        }

        byte[] bytes = Files.readAllBytes(file);

        return new String(bytes, StandardCharsets.UTF_8);
    }

    static public String trimToNull(String value) {
        if (value != null) {
            value = value.trim();
            if (value.equals("")) {
                return null;
            }
        }
        return value;
    }

    static public Path which(String nameOfExeOrBin) {
        // search PATH for existence of an executable
        final String path = System.getenv("PATH");
        if (path != null) {
            final String[] paths = path.split(File.pathSeparator);
            for (String p : paths) {
                Path pathDir = Paths.get(p);
                Path exeFile = pathDir.resolve(nameOfExeOrBin);
                if (Files.exists(exeFile)) {
                    return exeFile;
                }
            }
        }
        return null;
    }

    static public String execAndGetOutput(List<String> commands) throws IOException, InterruptedException {
        // Create a ProcessBuilder instance
        final ProcessBuilder processBuilder = new ProcessBuilder(commands.toArray(new String[0]));

        // Start the process
        final Process process = processBuilder.start();

        // Read the output from the process's input stream (standard output)
        final StringBuilder output = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append(System.lineSeparator());
            }
        }

        // Read the error from the process's input stream (standard error)
        //final StringBuilder error = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                //output.append(line).append(System.lineSeparator());
                System.err.println(line);
            }
        }

        // Wait for the process to complete and get its exit code
        int exitCode = process.waitFor();

        if (exitCode != 0) {
            throw new IOException("Process exited with a non-zero code (actual " + exitCode + ")");
        }

        return output.toString();
    }

    static public boolean searchEnvVar(String name, String value) {
        return searchEnvVar(System.getenv(), name, value);
    }

    static public boolean searchEnvVar(Map<String,String> env, String name, String value) {
        final String currentEnvVar = trimToNull(env.get(name));

        return currentEnvVar != null && currentEnvVar.equals(value);
    }

    static public boolean searchEnvPath(Path path) {
        return searchEnvPath(System.getenv(), path);
    }

    static public boolean searchEnvPath(Map<String,String> env, Path path) {
        return searchEnvPath(trimToNull(env.get("PATH")), path);
    }

    static public boolean searchEnvPath(String pathValueInEnv, Path path) {
        final String[] currentPathVarParts = pathValueInEnv != null ? pathValueInEnv.split(File.pathSeparator) : new String[0];

        for (String currentPathVarPart : currentPathVarParts) {
            if (currentPathVarPart != null && currentPathVarPart.equalsIgnoreCase(path.toString())) {
                return true;
            }
        }

        return false;
    }

    static public String joinIfDelimiterMissing(String v1, String v2, String delimiter) {
        if (v1 == null) {
            return v2;
        } else if (v2 == null) {
            return v1;
        }
        if (v1.endsWith(delimiter)) {
            return v1 + v2;
        }
        return v1 + delimiter + v2;
    }

    /**
     * Filters the input list and returns a new list containing only lines that
     * do not exist within the specified file, while preserving the original order.
     *
     * @param lines The list of strings to be filtered. (Not modified)
     * @param file  The Path to the file containing existing lines (potentially massive).
     * @return A new List<String> with existing lines filtered out.
     * @throws IOException If an I/O error occurs reading the file.
     */
    public static List<String> filterLinesIfPresentInFile(Path file, List<String> lines) throws IOException {
        if (lines == null) {
            return new ArrayList<>();
        }

        if (lines.isEmpty() || file == null || !Files.exists(file)) {
            return new ArrayList<>(lines);
        }

        // 1. Create a Set of all unique lines present in the input list (O(N_L) time/memory).
        // This is used for fast O(1) lookups as we stream the file.
        Set<String> linesInList = new HashSet<>(lines);

        // 2. Create a Set to collect all lines that are found in the file and must be removed.
        Set<String> linesToRemove = new HashSet<>();

        // 3. Stream the file line by line (O(N_F) time).
        // Files.lines() ensures only one line is loaded into memory at a time, keeping memory low.
        try (Stream<String> fileStream = Files.lines(file, StandardCharsets.UTF_8)) {
            fileStream.forEach(fileLine -> {
                // Check if the line from the massive file is one we are looking for. (O(1) lookup)
                if (linesInList.contains(fileLine)) {
                    // If found, mark it for removal from the original list.
                    linesToRemove.add(fileLine);
                }
            });
        }

        // 4. Create a new list retaining the order and excluding lines found in the file. (O(N_L) time).
        // The original 'lines' list is NOT modified.
        return lines.stream()
            .filter(line -> !linesToRemove.contains(line))
            .collect(Collectors.toList());
    }

    static public void writeLinesToFileWithSectionBeginAndEndLines(Path file, List<String> lines, boolean appendOrReplace) throws IOException {
        final StringBuilder sb = new StringBuilder();

        // if we're not appending or replacing, we can simply write the lines and be done
        if (!appendOrReplace) {
            for (String line : lines) {
                sb.append(line).append("\n");
            }

            Files.write(file, sb.toString().getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } else {
            if (lines == null || lines.size() <= 0) {
                throw new IllegalArgumentException("Lines cannot be null or empty.");
            }

            // the first and last lines should be section begin and end lines, let's confirm that
            final String firstLine = lines.get(0);
            final String lastLine = lines.get(lines.size() - 1);

            if (!firstLine.contains(" begin ")) {
                throw new IllegalArgumentException("First line must be a section begin line (was " + firstLine + ")");
            }

            if (!lastLine.contains(" end ")) {
                throw new IllegalArgumentException("Last line must be a section end line (was " + lastLine + ")");
            }

            // search the target file for the section begin line starting byte position
            final ByteRange firstLineByteRange = findLineByteRange(file, firstLine, 0);
            final long nextLineSearchIndex = firstLineByteRange != null ? firstLineByteRange.getIndex() + firstLineByteRange.getLength() : 0;
            final ByteRange lastLineByteRange = findLineByteRange(file, lastLine, nextLineSearchIndex);

            log.debug("firstLine: {}", firstLineByteRange);
            log.debug("nextLineSearchIndex: {}", nextLineSearchIndex);
            log.debug("lastLine: {}", lastLineByteRange);

            if (firstLineByteRange != null && lastLineByteRange != null) {
                // we are going to REPLACE the text between the section begin and end lines
                // no need to calc newlines needed, we just build the content and go
                for (String line : lines) {
                    sb.append(line).append("\n");
                }
                replaceByteRange(file, sb.toString(), firstLineByteRange.getIndex(), lastLineByteRange.getIndex()+ lastLineByteRange.getLength());
            } else {
                // we are going to APPEND the section begin and end lines to the file
                // we need to calculate the number of newlines needed before the first line
                int newlinesNeededCount = newlinesNeededForAppending(file);
                for (int i = 0; i < newlinesNeededCount; i++) {
                    sb.append("\n");
                }
                for (String line : lines) {
                    sb.append(line).append("\n");
                }
                Files.write(file, sb.toString().getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            }
        }
    }


    /**
     * Atomically replaces a byte range in a file with a new message, or appends
     * the message if the positions are invalid (-1).
     *
     * @param file The file to modify.
     * @param content The message to insert or append.
     * @param replaceStartPos The starting byte offset (inclusive) for replacement, or -1 for append.
     * @param replaceEndPos The ending byte offset (exclusive) for replacement.
     * @throws IOException If an I/O error occurs during processing.
     * @throws IllegalArgumentException If replacement positions are invalid (e.g., start > end).
     */
    static public void replaceByteRange(Path file, String content, long replaceStartPos, long replaceEndPos) throws IOException {
        final byte[] messageBytes = content.getBytes(StandardCharsets.UTF_8);

        // 2. Validate replacement case positions
        long fileSize = Files.size(file);
        if (replaceStartPos < 0 || replaceEndPos < 0 || replaceStartPos > replaceEndPos || replaceStartPos > fileSize || replaceEndPos > fileSize) {
            throw new IllegalArgumentException(
                String.format("Invalid startPos (%d) or endPos (%d) for file size (%d).", replaceStartPos, replaceEndPos, fileSize)
            );
        }

        // Use a temporary file for atomic update
        final Path tempFile = Files.createTempFile(file.getFileName().toString(), ".tmp");

        // We need to retain the permissions of the original file when we move this back
        replicateFilePermissions(file, tempFile);

        // We use RandomAccessFile for precise control over reading the original file
        // and standard streams for writing to the temporary file.
        try (RandomAccessFile raf = new RandomAccessFile(file.toFile(), "r"); OutputStream os = Files.newOutputStream(tempFile, StandardOpenOption.WRITE)) {
            byte[] buffer = new byte[8192];
            int bytesRead;

            // --- Part 1: Copy bytes from start (0) up to startPos ---
            long bytesRemaining = replaceStartPos;
            while (bytesRemaining > 0) {
                int bytesToRead = (int) Math.min(buffer.length, bytesRemaining);
                bytesRead = raf.read(buffer, 0, bytesToRead);

                if (bytesRead < 0) { break; } // Should not happen

                os.write(buffer, 0, bytesRead);
                bytesRemaining -= bytesRead;
            }

            // --- Part 2: Write the replacement message ---
            os.write(messageBytes);

            // --- Part 3: Skip bytes in the original file up to endPos ---
            raf.seek(replaceEndPos);

            // --- Part 4: Copy remaining bytes from endPos to EOF ---
            while ((bytesRead = raf.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
        } catch (Exception e) {
            // If anything fails during processing, ensure the temporary file is deleted
            Files.deleteIfExists(tempFile);
            throw e;
        }

        // --- Final Step: Atomic Replacement ---
        // ATOMIC_MOVE ensures the file is either fully replaced or the original remains untouched.
        try {
            Files.move(tempFile, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException e) {
            // fallback is atomic move is not supported
            Files.move(tempFile, file, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    /**
     * Reads a file byte-by-byte, treating the newline character ('\n', byte 10)
     * as the line separator, and returns the byte offset where the target line starts.
     * * Note: This method assumes a standard Unix-style line ending ('\n') for separation.
     * The line content is decoded using UTF-8 for comparison.
     * * @param filePath The path to the file to search.
     * @param targetLine The exact string content of the line to find.
     * @return The starting byte position (0-based offset) of the line, or -1 if not found.
     * @throws IOException If an I/O error occurs reading the file.
     */
    static public ByteRange findLineByteRange(Path filePath, String targetLine, long startPosition) throws IOException {

        // Use try-with-resources to ensure the streams are closed automatically
        try (InputStream is = Files.newInputStream(filePath, StandardOpenOption.READ); BufferedInputStream bis = new BufferedInputStream(is)) {

            final long totalBytesSkipped = bis.skip(startPosition);
            final ByteArrayOutputStream lineBuffer = new ByteArrayOutputStream();
            long lineStartOffset = totalBytesSkipped;
            long totalBytesRead = totalBytesSkipped;
            int byteRead;

            // Read the file byte by byte until EOF (-1) is reached
            while ((byteRead = bis.read()) != -1) {
                // Increment the absolute byte position counter after processing the byte
                totalBytesRead++;

                // If the byte is a newline character (ASCII LF = 10)
                if (byteRead == '\n') {
                    // Convert the accumulated bytes to a String using UTF-8
                    String currentLine = lineBuffer.toString(StandardCharsets.UTF_8.name());

                    // Compare the extracted line with the target line
                    if (currentLine.equals(targetLine)) {
                        // Match found! Return the offset where the line started.
                        return new ByteRange(lineStartOffset, lineBuffer.size() + 1);
                    }

                    // Prepare for the next line
                    lineBuffer.reset();

                    // The start of the next line is the current total bytes read position.
                    // Since we just processed the '\n' byte, the next read will be the start.
                    // We must calculate the offset *before* incrementing totalBytesRead,
                    // or after reading the byte, depending on the loop structure.
                    // In this logic: totalBytesRead is incremented AFTER the read, so
                    // totalBytesRead is the count of bytes read *up to and including* the newline.
                    lineStartOffset = totalBytesRead;

                } else {
                    // Accumulate bytes for the current line
                    lineBuffer.write(byteRead);
                }
            }

            // --- Handle the final line (if file doesn't end with a newline) ---
            if (lineBuffer.size() > 0) {
                String finalLine = lineBuffer.toString(StandardCharsets.UTF_8.name());
                if (finalLine.equals(targetLine)) {
                    return new ByteRange(lineStartOffset, lineBuffer.size());
                }
            }

            return null;    // Line not found
        }
    }

    static public void writeLinesToFile(Path file, List<String> lines, boolean append) throws IOException {
        // build the lines into a full string
        final StringBuilder sb = new StringBuilder();

        // if we are appending to a file, we need to be careful about adding newlines before we write our content
        if (append && !lines.isEmpty() && Files.exists(file)) {
            int newlinesNeededCount = newlinesNeededForAppending(file);
            for (int i = 0; i < newlinesNeededCount; i++) {
                sb.append("\n");
            }
        }

        for (String line : lines) {
            sb.append(line).append("\n");
        }

        if (append) {
            Files.write(file, sb.toString().getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } else {
            Files.write(file, sb.toString().getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        }
    }

    static public int newlinesNeededForAppending(Path file) throws IOException {
        final StringBuilder currentLine = new StringBuilder();
        int lineCount = 0;
        boolean wasLastLine2Empty = false;

        // Use BufferedReader for efficient, low-memory, character-by-character reading
        try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            int charCode;

            // Read until the end of the file is reached (-1)
            while ((charCode = reader.read()) != -1) {
                char c = (char)charCode;
                currentLine.append(c);

                // Only use '\n' as the line termination character.
                if (c == '\n') {
                    lineCount++;

                    wasLastLine2Empty = currentLine.toString().trim().length() == 0;

                    // line is complete. It may contain a preceding '\r' (for \r\n)
                    currentLine.setLength(0); // Reset buffer
                }
            }
        }

        final String lastLine = currentLine.toString();

        if (lastLine.length() == 0) {
            if (lineCount == 0) {
                return 0;
            } else {
                if (wasLastLine2Empty) {
                    return 0;
                } else {
                    return 1;
                }
            }
        } else {
            if (lastLine.trim().length() == 0) {
                // if the last line is all whitespace, visually we only need one newline
                return 1;
            } else {
                // otherwise, the line consists of real chars and we need to append 2
                return 2;
            }
        }
    }

    /**
     * Checks if the file specified by the Path ends with a Line Feed ('\n') character.
     * * Note: This method specifically checks the *last byte* of the file. If the file
     * uses Windows-style newlines (CRLF: '\r\n'), the last byte will still be '\n' (LF),
     * and this method will return true.
     *
     * @param path The path to the file to check.
     * @return true if the file ends with '\n', false otherwise.
     * @throws IOException if an I/O error occurs during file access or if the path is invalid.
     */
    static public boolean endsWithNewlineForAppending(Path path) throws IOException {
        if (path == null) {
            throw new IllegalArgumentException("Path cannot be null.");
        }

        // Ensure the path refers to an existing, readable regular file
        if (!Files.exists(path)) {
            return true;
        }

        long fileSize = Files.size(path);

        // An empty file (size 0) cannot end with a newline
        if (fileSize == 0) {
            return true;
        }

        // Use RandomAccessFile for efficient access to the last byte.
        // Java 8 and earlier are compatible with the toFile() conversion.
        try (RandomAccessFile raf = new RandomAccessFile(path.toFile(), "r")) {
            // Move file pointer to the position of the last byte
            raf.seek(fileSize - 1);

            // Read the last byte (returns -1 at EOF, but we checked the size)
            int lastByte = raf.read();

            // Check if the last byte is a Line Feed (LF, '\n')
            return lastByte == 10;
        } catch (IOException e) {
            // Re-throw the exception, providing context about the file that failed
            throw new IOException("Error accessing file to check newline status: " + path, e);
        }
    }

    /**
     * Replicates the permissions from a source file to a target file,
     * attempting the best possible method for the current operating system.
     *
     * @param sourceFile the file to read permissions from.
     * @param targetFile the file to write permissions to.
     * @throws IOException if an I/O error occurs.
     */
    static public void replicateFilePermissions(Path sourceFile, Path targetFile) throws IOException {
        // Get the FileSystem's supported attribute views
        Set<String> supportedViews = sourceFile.getFileSystem().supportedFileAttributeViews();

        if (supportedViews.contains("posix")) {
            // Priority 1: POSIX systems (Linux, macOS)
//            System.out.println("Using POSIX file permissions.");
            try {
                Set<PosixFilePermission> permissions = Files.getPosixFilePermissions(sourceFile);
                Files.setPosixFilePermissions(targetFile, permissions);
                // Optionally copy owner/group as well, if you have the necessary privileges
                // FileOwnerAttributeView ownerView = Files.getFileAttributeView(source, FileOwnerAttributeView.class);
                // Files.setOwner(target, ownerView.getOwner());
            } catch (IOException e) {
                log.error("Failed to apply POSIX permissions: " + e.getMessage());
            }
            return;
        }

        if (supportedViews.contains("acl")) {
            // Priority 2: Windows NTFS systems
//            System.out.println("Using ACL file permissions.");
            try {
                AclFileAttributeView aclView = Files.getFileAttributeView(sourceFile, AclFileAttributeView.class);
                List<AclEntry> aclEntries = aclView.getAcl();

                AclFileAttributeView targetAclView = Files.getFileAttributeView(targetFile, AclFileAttributeView.class);
                targetAclView.setAcl(aclEntries);
            } catch (IOException e) {
                log.error("Failed to apply ACL permissions: " + e.getMessage());
            }
            return;
        }

        if (supportedViews.contains("dos")) {
            // Priority 3: Basic fallback for older/simpler file systems
//            System.out.println("Using basic DOS file attributes.");
            try {
                DosFileAttributeView sourceDos = Files.getFileAttributeView(sourceFile, DosFileAttributeView.class);
                DosFileAttributes sourceAttrs = sourceDos.readAttributes();

                DosFileAttributeView targetDos = Files.getFileAttributeView(targetFile, DosFileAttributeView.class);
                targetDos.setReadOnly(sourceAttrs.isReadOnly());
                targetDos.setHidden(sourceAttrs.isHidden());
                targetDos.setSystem(sourceAttrs.isSystem());
                targetDos.setArchive(sourceAttrs.isArchive());
            } catch (IOException e) {
                log.error("Failed to apply DOS attributes: " + e.getMessage());
            }
            return;
        }
    }

}
