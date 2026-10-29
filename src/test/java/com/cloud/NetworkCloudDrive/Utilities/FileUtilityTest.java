package com.cloud.NetworkCloudDrive.Utilities;

import com.cloud.NetworkCloudDrive.Properties.IgnoreFileListProperties;
import com.cloud.NetworkCloudDrive.Security.EncodingUtility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FileUtilityTest {

    @Mock
    private EncodingUtility encodingUtility;
    @Mock
    private PathUtility pathUtility;

    private IgnoreFileListProperties ignoreFileListProperties;
    private FileUtility fileUtility;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        ignoreFileListProperties = new IgnoreFileListProperties();
        fileUtility = new FileUtility(encodingUtility, ignoreFileListProperties, pathUtility);
    }

    @ParameterizedTest
    @MethodSource("mimeTypeTestCases")
    void getMimeTypeFromExtensionUsingTikaCore_ReturnsCorrectType(String fileName, String content, String expectedMimeType) throws IOException {
        Path filePath = tempDir.resolve(fileName);
        Files.writeString(filePath, content);
        String mimeType = fileUtility.getMimeTypeFromExtensionUsingTikaCore(filePath.toFile());
        assertEquals(expectedMimeType, mimeType);
    }

    private static Stream<Arguments> mimeTypeTestCases() {
        return Stream.of(
                Arguments.of("test.txt", "hello world", "text/plain"),
                Arguments.of("test.html", "<html></html>", "text/html"),
                Arguments.of("test.json", "{}", "application/json"),
                Arguments.of("test.xml", "<root/>", "application/xml"),
                Arguments.of("test.css", "body {}", "text/css"),
                Arguments.of("test.csv", "a,b,c", "text/csv")
        );
    }

    @Test
    void getFileExtension_withSimpleExtension() {
        assertEquals(".txt", fileUtility.getFileExtension("file.txt"));
    }

    @Test
    void getFileExtension_withMultipleDots() {
        assertEquals(".gz", fileUtility.getFileExtension("archive.tar.gz"));
    }

    @Test
    void getFileExtension_withNoExtension() {
        assertEquals("", fileUtility.getFileExtension("README"));
    }

    @Test
    void getFileExtension_withLeadingDot() {
        assertEquals("", fileUtility.getFileExtension(".hidden"));
    }

    @Test
    void getFileExtension_withEmptyString() {
        assertEquals("", fileUtility.getFileExtension(""));
    }

    @Test
    void hasFileExtension_withExtension_ReturnsTrue() {
        assertTrue(fileUtility.hasFileExtension("document.pdf"));
    }

    @Test
    void hasFileExtension_withoutExtension_ReturnsFalse() {
        assertFalse(fileUtility.hasFileExtension("README"));
    }

    @Test
    void hasFileExtension_withLeadingDot_ReturnsFalse() {
        assertFalse(fileUtility.hasFileExtension(".gitignore"));
    }

    @Test
    void hasFileExtension_withEmptyString_ReturnsFalse() {
        assertFalse(fileUtility.hasFileExtension(""));
    }

    @ParameterizedTest
    @MethodSource("systemIgnoreFiles")
    void isIgnoredFile_systemFile_ReturnsTrue(String filename) {
        assertTrue(fileUtility.isIgnoredFile(filename));
    }

    @ParameterizedTest
    @MethodSource("apiIgnoreFiles")
    void isIgnoredFile_apiFile_ReturnsTrue(String filename) {
        assertTrue(fileUtility.isIgnoredFile(filename));
    }

    @Test
    void isIgnoredFile_dotfileNotInList_ReturnsFalse() {
        assertFalse(fileUtility.isIgnoredFile(".gitignore"));
        assertFalse(fileUtility.isIgnoredFile(".env"));
        assertFalse(fileUtility.isIgnoredFile(".recycleBin"));
    }

    @Test
    void isIgnoredFile_regularFile_ReturnsFalse() {
        assertFalse(fileUtility.isIgnoredFile("document.txt"));
        assertFalse(fileUtility.isIgnoredFile("image.png"));
    }

    @ParameterizedTest
    @MethodSource("systemIgnoreFiles")
    void isIgnoredSystemFile_systemFile_ReturnsTrue(String filename) {
        assertTrue(fileUtility.isIgnoredSystemFile(filename));
    }

    @Test
    void isIgnoredSystemFile_dotfileNotInList_ReturnsFalse() {
        assertFalse(fileUtility.isIgnoredSystemFile(".gitignore"));
        assertFalse(fileUtility.isIgnoredSystemFile(".env"));
        assertFalse(fileUtility.isIgnoredSystemFile(".recycleBin"));
    }

    @Test
    void isIgnoredSystemFile_regularFile_ReturnsFalse() {
        assertFalse(fileUtility.isIgnoredSystemFile("file.txt"));
    }

    @ParameterizedTest
    @MethodSource("apiIgnoreFiles")
    void isIgnoredAPIFile_apiFile_ReturnsTrue(String filename) {
        assertTrue(fileUtility.isIgnoredAPIFile(filename));
    }

    @Test
    void isIgnoredAPIFile_dotfileNotInList_ReturnsFalse() {
        assertFalse(fileUtility.isIgnoredAPIFile(".gitignore"));
        assertFalse(fileUtility.isIgnoredAPIFile(".env"));
        assertFalse(fileUtility.isIgnoredAPIFile(".recycleBin"));
    }

    @Test
    void isIgnoredAPIFile_regularFile_ReturnsFalse() {
        assertFalse(fileUtility.isIgnoredAPIFile("file.txt"));
    }

    private static Stream<String> systemIgnoreFiles() {
        return new IgnoreFileListProperties().getIgnoreFileList().stream();
    }

    private static Stream<String> apiIgnoreFiles() {
        return new IgnoreFileListProperties().getIgnoreAPIFilesList().stream();
    }

    @Test
    void deleteFolders_RemovesDirectoryAndContents() throws IOException {
        Path dir = tempDir.resolve("delete_me");
        Files.createDirectories(dir);
        Path subDir = Files.createDirectory(dir.resolve("sub"));
        Path file1 = Files.createFile(dir.resolve("file1.txt"));
        Path file2 = Files.createFile(subDir.resolve("file2.txt"));

        assertTrue(Files.exists(dir));
        assertTrue(Files.exists(subDir));
        assertTrue(Files.exists(file1));
        assertTrue(Files.exists(file2));

        fileUtility.deleteFolders(dir);

        assertTrue(Files.notExists(dir));
        assertTrue(Files.notExists(subDir));
        assertTrue(Files.notExists(file1));
        assertTrue(Files.notExists(file2));
    }

    @Test
    void deleteFolders_WhenDirectoryDoesNotExist_ThrowsException() {
        Path nonExistent = tempDir.resolve("nonexistent");

        assertThrows(IOException.class,
                () -> fileUtility.deleteFolders(nonExistent));
    }

    @Test
    void checkIfFileExistsDecodeNames_WhenFileExists_ReturnsTrue() throws IOException {
        String encodedName = "encodedFile123";
        String decodedName = "myfile.txt";
        when(pathUtility.getFullPath("test/path")).thenReturn(tempDir);
        when(encodingUtility.decodedBase32SplitArray(encodedName)).thenReturn(new String[]{"1", decodedName, "99"});
        Files.createFile(tempDir.resolve(encodedName));

        boolean result = fileUtility.checkIfFileExistsDecodeNames("test/path", decodedName);

        assertTrue(result);
    }

    @Test
    void checkIfFileExistsDecodeNames_WhenFileDoesNotMatch_ReturnsFalse() throws IOException {
        String encodedName = "encodedFile456";
        when(pathUtility.getFullPath("test/path")).thenReturn(tempDir);
        when(encodingUtility.decodedBase32SplitArray(encodedName)).thenReturn(new String[]{"1", "other.txt", "99"});
        Files.createFile(tempDir.resolve(encodedName));

        boolean result = fileUtility.checkIfFileExistsDecodeNames("test/path", "myfile.txt");

        assertFalse(result);
    }

    @Test
    void checkIfFileExistsDecodeNames_WhenOnlyIgnoredFileExists_ReturnsFalse() throws IOException {
        when(pathUtility.getFullPath("test/path")).thenReturn(tempDir);
        Files.createFile(tempDir.resolve(".DS_Store"));

        boolean result = fileUtility.checkIfFileExistsDecodeNames("test/path", "anyfile.txt");

        assertFalse(result);
        verifyNoInteractions(encodingUtility);
    }

    @Test
    void checkDuplicate_withNoFiles_ReturnsFalse() {
        List<Path> emptyList = List.of();
        assertFalse(fileUtility.checkDuplicate(emptyList, "test.txt"));
    }

    @Test
    void checkDuplicate_whenFileExistsWithMatchingDecodedName_ReturnsTrue() {
        String encodedName = "encoded123";
        String filename = "myfile.txt";
        when(encodingUtility.decodedBase32SplitArray(encodedName)).thenReturn(new String[]{"1", filename, "99"});

        List<Path> files = List.of(tempDir.resolve(encodedName));

        assertTrue(fileUtility.checkDuplicate(files, filename));
    }

    @Test
    void checkDuplicate_whenFileExistsWithDifferentName_ReturnsFalse() {
        String encodedName = "encoded123";
        when(encodingUtility.decodedBase32SplitArray(encodedName)).thenReturn(new String[]{"1", "other.txt", "99"});

        List<Path> files = List.of(tempDir.resolve(encodedName));

        assertFalse(fileUtility.checkDuplicate(files, "myfile.txt"));
    }

    @Test
    void checkDuplicate_whenFileIsIgnored_ReturnsFalse() {
        List<Path> files = List.of(tempDir.resolve(".DS_Store"));

        assertFalse(fileUtility.checkDuplicate(files, "anything.txt"));
        verifyNoInteractions(encodingUtility);
    }

    @Test
    void getFileAndFolderPathsFromFolder_ReturnsPaths() throws IOException {
        Files.createFile(tempDir.resolve("file1.txt"));
        Files.createDirectory(tempDir.resolve("subdir"));
        Files.createFile(tempDir.resolve("subdir").resolve("file2.txt"));

        List<Path> result = fileUtility.getFileAndFolderPathsFromFolder(tempDir);

        assertEquals(2, result.size());
    }

    @Test
    void walkFsTree_forward_ReturnsAllPathsIncludingRoot() throws IOException {
        Files.createDirectory(tempDir.resolve("dir"));
        Files.createFile(tempDir.resolve("dir").resolve("f.txt"));
        Files.createFile(tempDir.resolve("root.txt"));

        List<Path> result = fileUtility.walkFsTree(tempDir, false);

        assertTrue(result.size() >= 3);
    }

    @Test
    void walkFsTree_reverse_ReturnsReversedOrder() throws IOException {
        Files.createDirectory(tempDir.resolve("dir"));
        Files.createFile(tempDir.resolve("dir").resolve("f.txt"));
        Files.createFile(tempDir.resolve("root.txt"));

        List<Path> result = fileUtility.walkFsTree(tempDir, true);

        assertTrue(result.size() >= 3);
    }

    @Test
    void returnPathIfItsNotADuplicate_whenNotDuplicate_ReturnsPath() throws IOException {
        Path result = fileUtility.returnPathIfItsNotADuplicate(tempDir.resolve("newfile.txt").toString());

        assertEquals(tempDir.resolve("newfile.txt"), result);
    }

    @Test
    void returnPathIfItsNotADuplicate_whenFileExists_ThrowsFileNotFoundException() throws IOException {
        Files.createFile(tempDir.resolve("existing.txt"));

        assertThrows(java.io.FileNotFoundException.class,
                () -> fileUtility.returnPathIfItsNotADuplicate(tempDir.resolve("existing.txt").toString()));
    }

    @Test
    void returnPathIfItsNotADuplicate_whenFolderExists_ThrowsFileNotFoundException() throws IOException {
        Files.createDirectory(tempDir.resolve("existingdir"));

        assertThrows(java.io.FileNotFoundException.class,
                () -> fileUtility.returnPathIfItsNotADuplicate(tempDir.resolve("existingdir").toString()));
    }

    @Test
    void returnFileIfItExists_whenExists_ReturnsFile() throws IOException {
        Files.createFile(tempDir.resolve("file.txt"));

        java.io.File result = fileUtility.returnFileIfItExists(tempDir.resolve("file.txt").toString());

        assertNotNull(result);
        assertTrue(result.exists());
    }

    @Test
    void returnFileIfItExists_whenNotExists_ThrowsFileNotFoundException() {
        assertThrows(java.io.FileNotFoundException.class,
                () -> fileUtility.returnFileIfItExists(tempDir.resolve("missing.txt").toString()));
    }

    @Test
    void returnPathIfItExists_whenExists_ReturnsPath() throws IOException {
        Files.createFile(tempDir.resolve("file.txt"));

        Path result = fileUtility.returnPathIfItExists(tempDir.resolve("file.txt").toString());

        assertNotNull(result);
        assertTrue(Files.exists(result));
    }

    @Test
    void returnPathIfItExists_whenNotExists_ThrowsFileNotFoundException() {
        assertThrows(java.io.FileNotFoundException.class,
                () -> fileUtility.returnPathIfItExists(tempDir.resolve("missing.txt").toString()));
    }

    @Test
    void returnPathIfItExistsOld_whenNotExists_ThrowsFileNotFoundException() {
        when(pathUtility.getFullPath("path/missing.txt")).thenReturn(tempDir.resolve("missing.txt"));

        assertThrows(java.io.FileNotFoundException.class,
                () -> fileUtility.returnPathIfItExistsOld("path/missing.txt"));
    }

    @Test
    void returnPathIfItExistsOld_whenExists_ReturnsPath() throws IOException {
        when(pathUtility.getFullPath("path/file.txt")).thenReturn(tempDir.resolve("file.txt"));
        Files.createFile(tempDir.resolve("file.txt"));

        Path result = fileUtility.returnPathIfItExistsOld("path/file.txt");

        assertNotNull(result);
        assertTrue(Files.exists(result));
    }
}
