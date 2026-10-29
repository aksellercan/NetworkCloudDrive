package com.cloud.NetworkCloudDrive.Security;

import com.cloud.NetworkCloudDrive.Models.FolderMetadata;
import com.cloud.NetworkCloudDrive.Persistence.SQLiteDAO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;
import org.springframework.mock.env.MockEnvironment;

import java.security.NoSuchAlgorithmException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EncodingUtilityTest {

    @Mock
    private SQLiteDAO sqLiteDAO;

    private EncodingUtility encodingUtility;

    @BeforeEach
    void setUp() {
        encodingUtility = new EncodingUtility(new MockEnvironment());
    }

    @Test
    void encodeBase32UserFolderName_And_Decode_Roundtrip() {
        long id = 1L;
        String name = "testuser";
        String mail = "test@example.com";

        String encoded = encodingUtility.encodeBase32UserFolderName(id, name, mail);
        assertNotNull(encoded);
        assertFalse(encoded.isEmpty());

        String decoded = encodingUtility.decodeBase32StringNoPadding(encoded);
        String[] parts = decoded.split(":");
        assertEquals("1", parts[0]);
        assertEquals("testuser", parts[1]);
        assertEquals("test@example.com", parts[2]);
    }

    @Test
    void encodeBase32FolderName_And_Decode_Roundtrip() {
        long folderId = 42L;
        String folderName = "myFolder";
        long userId = 7L;

        String encoded = encodingUtility.encodeBase32FolderName(folderId, folderName, userId);
        assertNotNull(encoded);

        String[] decoded = encodingUtility.decodedBase32SplitArray(encoded);
        assertEquals("42", decoded[0]);
        assertEquals("myFolder", decoded[1]);
        assertEquals("7", decoded[2]);
    }

    @Test
    void encodeBase32FileName_ProducesSameAsFolderName() {
        String fileEncoded = encodingUtility.encodeBase32FileName(10L, "file.txt", 3L);
        String folderEncoded = encodingUtility.encodeBase32FolderName(10L, "file.txt", 3L);
        assertEquals(folderEncoded, fileEncoded);
    }

    @Test
    void decodedBase32SplitArray_ReturnsCorrectParts() {
        String encoded = encodingUtility.encodeBase32FolderName(99L, "document.pdf", 5L);
        String[] parts = encodingUtility.decodedBase32SplitArray(encoded);
        assertEquals(3, parts.length);
        assertEquals("99", parts[0]);
        assertEquals("document.pdf", parts[1]);
        assertEquals("5", parts[2]);
    }

    @Test
    void getMetadataIDFromEncodedBase32_ReturnsCorrectId() {
        String encoded = encodingUtility.encodeBase32FolderName(77L, "photo.jpg", 2L);
        long id = encodingUtility.getMetadataIDFromEncodedBase64(encoded);
        assertEquals(77L, id);
    }

    @Test
    void hashString_SHA256_ReturnsNonNull() throws NoSuchAlgorithmException {
        String hash = encodingUtility.hashString("hello", "SHA-256");
        assertNotNull(hash);
        assertFalse(hash.isEmpty());
    }

    @Test
    void hashString_SHA512_ReturnsNonNull() throws NoSuchAlgorithmException {
        String hash = encodingUtility.hashString("hello", "SHA-512");
        assertNotNull(hash);
        assertFalse(hash.isEmpty());
    }

    @Test
    void hashString_DifferentInputs_ProduceDifferentHashes() throws NoSuchAlgorithmException {
        String hash1 = encodingUtility.hashString("hello", "SHA-256");
        String hash2 = encodingUtility.hashString("world", "SHA-256");
        assertNotEquals(hash1, hash2);
    }

    @Test
    void hashString_NullInput_ThrowsNullPointerException() {
        assertThrows(NullPointerException.class, () -> encodingUtility.hashString(null, "SHA-256"));
    }

    @Test
    void hashString_NullAlgorithm_ThrowsNullPointerException() {
        assertThrows(NullPointerException.class, () -> encodingUtility.hashString("test", null));
    }

    @Test
    void hashString_InvalidAlgorithm_ThrowsNoSuchAlgorithmException() {
        assertThrows(NoSuchAlgorithmException.class, () -> encodingUtility.hashString("test", "INVALID_ALGO"));
    }

    @Test
    void isBase32Decodable_WithValidEncodedString_ReturnsTrue() {
        String encoded = encodingUtility.encodeBase32FolderName(1L, "test", 1L);
        assertTrue(encodingUtility.isBase32Decodable(encoded));
    }

    @Test
    void isBase32Decodable_WithInvalidString_ReturnsFalse() {
        assertFalse(encodingUtility.isBase32Decodable("not-encoded!!!"));
    }

    @Test
    void isBase32Decodable_WithEmptyString_ReturnsFalse() {
        assertFalse(encodingUtility.isBase32Decodable(""));
    }

    @Test
    void isEncodedStringUserDirectory_WithUserFolderFormat_ReturnsTrue() {
        String encoded = encodingUtility.encodeBase32UserFolderName(1L, "user", "mail@test.com");
        assertTrue(encodingUtility.isEncodedStringUserDirectory(encoded));
    }

    @Test
    void isEncodedStringUserDirectory_WithFileFolderFormat_ReturnsFalse() {
        String encoded = encodingUtility.encodeBase32FolderName(5L, "file.txt", 1L);
        assertFalse(encodingUtility.isEncodedStringUserDirectory(encoded));
    }

    @Test
    void getFolderMetadataFromEncoding_CallsDAO() throws Exception {
        String encoded = encodingUtility.encodeBase32FolderName(10L, "folder", 1L);
        FolderMetadata expected = new FolderMetadata();
        expected.setId(10L);
        expected.setName("folder_encoded");
        expected.setUserid(1L);

        when(sqLiteDAO.getFolderMetadataFromEncoding(encoded, 1L)).thenReturn(expected);

        FolderMetadata result = sqLiteDAO.getFolderMetadataFromEncoding(encoded, 1L);

        assertEquals(10L, result.getId());
        assertEquals("folder_encoded", result.getName());
        assertEquals(1L, result.getUserid());
    }

    @Test
    void unifiedEncodeBase64_AddsRandomKeyBetweenIds() throws Exception {
        String encoded = encodingUtility.unifiedEncodeBase64(10L, 1L);
        String[] parts = encodingUtility.decodedBase32SplitArray(encoded);

        assertEquals(3, parts.length);
        assertEquals("10", parts[0]);
        assertEquals(3, parts[1].length());
        assertEquals("1", parts[2]);
    }

    @Test
    void unifiedEncodeBase64_WithExplicitKey_KeepsKey() throws Exception {
        String encoded = encodingUtility.unifiedEncodeBase64(10L, "ABC", 1L);
        String[] parts = encodingUtility.decodedBase32SplitArray(encoded);

        assertEquals(3, parts.length);
        assertEquals("10", parts[0]);
        assertEquals("ABC", parts[1]);
        assertEquals("1", parts[2]);
    }

    @Test
    void unifiedEncodeBase64_WithMetadata_EncodesIdsAndKey() throws Exception {
        com.cloud.NetworkCloudDrive.Models.FileMetadata file =
                new com.cloud.NetworkCloudDrive.Models.FileMetadata("ignored", 1L, 1L, "text/plain", 100L);
        file.setId(20L);
        file.setKey("KEY");

        String encoded = encodingUtility.unifiedEncodeBase64(file);
        String[] parts = encodingUtility.decodedBase32SplitArray(encoded);

        assertEquals("20", parts[0]);
        assertEquals("KEY", parts[1]);
        assertEquals("1", parts[2]);
    }

    @Test
    void unifiedEncodeBase64_CapsKeySizeToDefaultMax() throws Exception {
        MockEnvironment env = new MockEnvironment();
        env.setProperty("encode-randomize-string-size", "100");
        EncodingUtility custom = new EncodingUtility(env);

        String encoded = custom.unifiedEncodeBase64(1L, 1L);
        String[] parts = custom.decodedBase32SplitArray(encoded);

        int actualKeyLength = parts[1].length();
        int maxSize = com.cloud.NetworkCloudDrive.Models.Enum.System.DEFAULTS.MAX_SIZE_OF_KEY.getMax_size();
        assertTrue(actualKeyLength <= maxSize);
        assertTrue(actualKeyLength > 0);
    }

    @Test
    void generateKey_UsesConfiguredAlphabetAndSize() throws Exception {
        MockEnvironment env = new MockEnvironment();
        env.setProperty("encode-randomize-string-alphabet", "XY");
        env.setProperty("encode-randomize-string-size", "5");
        EncodingUtility custom = new EncodingUtility(env);

        String key = custom.generateKey();

        assertEquals(5, key.length());
        for (char c : key.toCharArray()) {
            assertTrue(c == 'X' || c == 'Y');
        }
    }

    @Test
    void generateKey_WithDefaultEnvironment_UsesDefaultAlphabetAndSize() throws Exception {
        String key = encodingUtility.generateKey();

        assertEquals(3, key.length());
        key.chars().forEach(c -> assertTrue(
                c >= 'A' && c <= 'Z',
                "Character " + (char) c + " is outside default alphabetical range"));
    }
}
