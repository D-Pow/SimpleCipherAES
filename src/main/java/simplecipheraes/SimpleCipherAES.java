package simplecipheraes;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/**
 * This is an implementation of AES 128-bit encryption that
 * encrypts and decrypts files using a JFileChooser.
 * The key is made by a simple hash of a user-entered key.
 * Any size file (even those >2GB) can be processed using this program.
 *
 * @author dPow
 */
public class SimpleCipherAES {
    MessageDigest hashFunction;

    AppProgressText progressText;

    public SimpleCipherAES(AppProgressText progressText) {
        this.progressText = progressText;
    }

    /**
     * Hashes a user-supplied key with SHA-256.
     *
     * @param text
     *          User-supplied key
     * @return
     *          The hash of the supplied key; reduced to 128 bits
     */
    public byte[] hashKey(String text) {
        if (text == null || text.equals("")) {
            return null;
        }

        byte[] key = null;
        try {
            hashFunction = MessageDigest.getInstance("SHA-256");
            hashFunction.update(text.getBytes("UTF-8"));
            key = hashFunction.digest();

            //Key is 256 bits, we want 128 for simplicity
            key = Arrays.copyOf(key, key.length/2);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return key;
    }

    /**
     * Processes the selected file for encryption or decryption.
     * Functions by reading the file 100 MB at a time, and then
     * encrypting or decrypting it according to the selected option.
     *
     * @param file
     *          File to be encrypted or decrypted
     * @param key
     *          User-given key
     * @param type
     *          If the file being processed will be encrypted or decrypted
     */
    public void processFile(File file, byte[] key, EncryptionDirection type){
        //Delete old encrypted/decrypted files
        if (type == EncryptionDirection.ENCRYPT) {
            try {
                String fileName = file.getCanonicalPath();
                String newName = fileName + ".enc";
                File newFile = new File(newName);
                if (newFile.exists()) {
                    newFile.delete();
                    progressText.updateProgress("Old file deleted.");
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        } else if (type == EncryptionDirection.DECRYPT) {
            try {
                //Remove ".enc" extension
                String oldFilePath = file.getCanonicalPath();
                String newFilePath = oldFilePath.substring(0, oldFilePath.length() - 4);
                //Get original extension
                int indexOfExtension = newFilePath.indexOf(".");
                String extension = newFilePath.substring(indexOfExtension);
                //Add " [decrypted]" just before the file's real extension
                newFilePath = newFilePath.substring(0, indexOfExtension) + " [decrypted]"
                        + extension;
                File newFile = new File(newFilePath);
                if (newFile.exists()) {
                    newFile.delete();
                    progressText.updateProgress("Old file deleted.");
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        Thread fileProcess = new Thread(){
            @Override
            public void run() {
                long fileSize = file.length();

                //Reads file as bytes and encrypts/decrypts it
                //Processed one byte array at a time so that large (>2GB) files
                //can be encrypted and decrypted, too
                try (InputStream inputStream = new BufferedInputStream(
                        new FileInputStream(file))) {
                    long totalBytesRead = 0;

                    while (totalBytesRead < fileSize) {
                        long bytesRemaining = fileSize - totalBytesRead;
                        int size; //size of byte array to be processed
                        //Need to ensure the bytesRemaining fits into a byte array
                        //array so we choose an arbitrary number smaller than
                        //Integer.MAX_VALUE - 5 and smaller than the heap space
                        if (bytesRemaining > 100000000) {
                            //If the file is larger than 100 MB, then process it
                            //in 100 MB chunks.
                            //Note: when split the file this way, the size of the
                            //encrypted chunk will be 100000016 instead of 100000000.
                            //Thus, decryption will need to process a larger chunk
                            //than 100 MB
                            if (type == EncryptionDirection.DECRYPT) {
                                //When encrypted, 100 MB becomes 100.000016 MB
                                //Thus, decryption must handle the larger filesize
                                //in order to make a 100 MB sized unencrypted file.
                                size = 100000016;
                            } else {
                                size = 100000000;
                            }
                        } else {
                            size = (int) bytesRemaining;
                        }
                        byte[] bytes = new byte[size];
                        int bytesRead = inputStream.read(bytes);
                        totalBytesRead += bytesRead;

                        //Encrypt or decrypt byte array
                        if (type == EncryptionDirection.ENCRYPT) {
                            encryptAndSave(bytes, key, file);
                        } else if (type == EncryptionDirection.DECRYPT) {
                            decryptAndSave(bytes, key, file);
                        }

                        //Let the user know what's happening
                        progressText.updateProgress(totalBytesRead, fileSize);
                        System.out.println(totalBytesRead + " completed so far.");
                        if (totalBytesRead == fileSize) {
                            System.out.println("Done!");
                            progressText.updateProgress("File Completed!");
                        }
                    }
                    //try-with-resources does not need to be closed
                } catch (IOException ex) {
                    System.err.println(ex.getCause());
                }
            }
        };
        fileProcess.start();
    }

    /**
     * Encrypts a byte array using a given key, then saves the encryption
     * to a new file with a similar name to the original file.
     *
     * @param message
     *          Byte array to be encrypted
     * @param key
     *          User-given key
     * @param file
     *          Old, unencrypted file to be encrypted; used to make a file with
     *          a similar name
     */
    private void encryptAndSave(byte[] message, byte[] key, File file) {
        byte[] encryptedBytes = null;
        try {
            //Make a new key of the correct 128-bit length
            SecretKey aesKey = new SecretKeySpec(key, "AES");
            //Instantiate a new cipher
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, aesKey);
            //Encrypt all the file bytes
            encryptedBytes = cipher.doFinal(message);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException |
                InvalidKeyException | IllegalBlockSizeException |
                BadPaddingException ex) {
            ex.printStackTrace();
        }

        //Save data to a file
        OutputStream out = null;
        try {
            String oldFilePath = file.getCanonicalPath();
            String newFilePath = oldFilePath + ".enc";
            out = new BufferedOutputStream(
                    new FileOutputStream(newFilePath, true)); //Append to file
            if (encryptedBytes != null) {
                out.write(encryptedBytes);
            }
            out.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Encrypts a byte array using a given key, then saves the encryption
     * to a new file with a similar name to the original file.
     *
     * @param message
     *          Byte array to be decrypted
     * @param key
     *          User-given key
     * @param encryptedFile
     *          Old file that was previously encrypted; used to make a file with
     *          a similar name
     */
    private void decryptAndSave(byte[] message, byte[] key, File encryptedFile) {
        byte[] decryptedBytes = null;
        try {
            //Make a new key of the correct 128-bit length
            SecretKey aesKey = new SecretKeySpec(key, "AES");
            //Instantiate a new cipher
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, aesKey);
            //Encrypt all the file bytes
            decryptedBytes = cipher.doFinal(message);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException |
                InvalidKeyException | IllegalBlockSizeException |
                BadPaddingException ex) {
            ex.printStackTrace();
        }

        //Save data to a file
        OutputStream out = null;
        try {
            //Remove ".enc" extension
            String oldFilePath = encryptedFile.getCanonicalPath();
            String newFilePath = oldFilePath.substring(0, oldFilePath.length()-4);
            //Get original extension
            int indexOfExtension = newFilePath.indexOf(".");
            String extension = newFilePath.substring(indexOfExtension);
            //Add " [decrypted]" just before the file's real extension
            newFilePath = newFilePath.substring(0, indexOfExtension) + " [decrypted]"
                    + extension;

            out = new BufferedOutputStream(
                    new FileOutputStream(newFilePath, true));
            if (decryptedBytes != null) {
                out.write(decryptedBytes);
            }
            out.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

}
