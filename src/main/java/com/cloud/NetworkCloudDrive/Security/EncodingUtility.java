package com.cloud.NetworkCloudDrive.Security;

import com.cloud.NetworkCloudDrive.Models.Enum.System.DEFAULTS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Random;

@Component
public class EncodingUtility {
    private final Logger logger = LoggerFactory.getLogger(EncodingUtility.class);
    private final Environment env;

    public EncodingUtility(Environment env) {
        this.env = env;
    }

    /**
     * Encode User folder using BASE32
     *
     * @param userId   currently logged-in user's ID
     * @param username currently logged-in user's name
     * @param mail     currently logged-in user's mail
     * @return BASE32 encoded user folder name
     */
    public String encodeBase32UserFolderName(long userId, String username, String mail) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString((userId + ":" + username + ":" + mail).getBytes());
    }

    /**
     * Decode BASE32 encoded file/folder names
     *
     * @param base32String BASE32 encoded file/folder name
     * @return decoded BASE32 string
     */
    public String decodeBase32StringNoPadding(String base32String) {
        return new String(Base64.getUrlDecoder().decode(base32String.getBytes()));
    }

    /**
     * Decode BASE32 encoded file/folder names.
     *
     * @param base32String BASE32 encoded file/folder name
     * @return decoded BASE32 string split by ":". Split index 0 is file/folder ID, 1 is file/folder name and 2 is for userID
     */
    public String[] decodedBase32SplitArray(String base32String) {
        return decodeBase32StringNoPadding(base32String).split(":");
    }

    public long getMetadataIDFromEncodedBase64(String base64String) {
        return Long.parseLong(decodedBase32SplitArray(base64String)[0]);
    }

    public boolean isEncodedStringUserDirectory(String encodedString) {
        try {
            long checkLastIndexType /* should be long */ = Long.parseLong(decodedBase32SplitArray(encodedString)[2]);
            //if successful parse MOST likely its not user directory
            logger.warn("Most likely NOT user directory");
            return false;
        } catch (/* throws */ NumberFormatException e) {
            logger.warn("Most likely user directory");
            return true;
        }
    }

    /**
     * Encode Folder names in BASE32
     *
     * @param folderId   folder's ID
     * @param folderName folder's name
     * @param userId     currently logged-in user's ID
     * @return BASE32 encoded Folder name
     */
    public String encodeBase32FolderName(long folderId, String folderName, long userId) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString((folderId + ":" + folderName + ":" + userId).getBytes());
    }

    private String randomizeString(int size) {
        Random random = new Random();
        String alphabet = env.getProperty("encode-randomize-string-alphabet", "ABCDEFGHIJKLMNOPQRSTUVWXYZ");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < size; i++) {
            sb.append(alphabet.charAt(random.nextInt(alphabet.length())));
        }
        return sb.toString();
    }

    /**
     * Encodes string in Base64 with URL safe alphabet. Depending on application configuration.
     * It will also add a 'n' sized array of letters between id's which will greatly increase the combinations encoding can have even if id's are same for all items.
     * <p>This can be configured in 'application.properties' with keys:</p>
     * <ul>
     * <li>
     * "encode-randomize-strings" to turn this feature on, default true
     * </li>
     * <li>
     * "encode-randomize-alphabet" set the letters to use, default alphabet
     * </li>
     * <li>
     * "encode-randomize-alphabet-size" set size of the randomized string, default 3 max value is 32
     * </li>
     * </ul>
     *
     * <p>Changing any of these later will not have any negative impact</p>
     *
     * @param fid    File or Folder id
     * @param userId User id
     * @return BASE64 encoded with URL safe alphabet
     */
    public String unifiedEncodeBase64(long fid, long userId) {
        int size = Integer.parseInt(env.getProperty("encode-randomize-string-size", "3"));
        size = Math.min(size, DEFAULTS.MAX_SIZE_OF_KEY.getMax_size());
        String encodeFormat = fid + ":" + randomizeString(size) + ":" + userId;
        return Base64.getUrlEncoder().withoutPadding().encodeToString((encodeFormat).getBytes());
    }

    public String unifiedEncodeBase64(long fid, String key, long userId) {
        String encodeFormat = fid + ":" + key + ":" + userId;
        return Base64.getUrlEncoder().withoutPadding().encodeToString((encodeFormat).getBytes());
    }

    public String generateKey(int size) {
        return randomizeString(size);
    }

    public String generateKey() {
        int size = Integer.parseInt(env.getProperty("encode-randomize-string-size", "3"));
        size = Math.min(size, DEFAULTS.MAX_SIZE_OF_KEY.getMax_size());
        return generateKey(size);
    }

    /**
     * Encode File names in BASE32
     *
     * @param fileId   file's ID
     * @param fileName file's name
     * @param userId   currently logged-in user's ID
     * @return BASE32 encoded File name
     */
    public String encodeBase32FileName(long fileId, String fileName, long userId) {
        return encodeBase32FolderName(fileId, fileName, userId);
    }

    public boolean isBase32Decodable(String name) {
        try {
            String tryDecoding = decodeBase32StringNoPadding(name);
            long tryIdParse = Long.parseLong(tryDecoding.split(":")[0]);
            logger.debug("trial of id parsing {} from {}", tryIdParse, tryDecoding);
            return !tryDecoding.isEmpty();
        } catch (Exception e) {
            logger.warn("Failed to parse concluding as not BASE32 for string {} Ex. {}", name, e.getMessage());
            return false;
        }
    }

    /**
     * Hashes string in given algorithms
     *
     * @param originalString string to hash
     * @param algorithm      algorithm of hash (Ex. SHA-256, SHA-512)
     * @return hashed string
     * @throws NoSuchAlgorithmException Invalid algorithm
     */
    public String hashString(String originalString, String algorithm) throws NoSuchAlgorithmException {
        // Source - https://stackoverflow.com/a/5531479
        // Posted by Jon Skeet, modified by community. See post 'Timeline' for change history
        // Retrieved 2025-12-22, License - CC BY-SA 4.0
        if (originalString == null) throw new NullPointerException("String to hash is null");
        if (algorithm == null) throw new NullPointerException("No hash algorithm given");
        return new String(MessageDigest.getInstance(algorithm).digest(originalString.getBytes(StandardCharsets.UTF_8)));
    }
}
