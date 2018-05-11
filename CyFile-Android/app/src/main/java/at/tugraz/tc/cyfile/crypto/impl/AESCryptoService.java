package at.tugraz.tc.cyfile.crypto.impl;


import android.util.Base64;

import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;

import at.tugraz.tc.cyfile.crypto.CryptoService;
import at.tugraz.tc.cyfile.crypto.exceptions.InvalidCryptoOperationException;
import at.tugraz.tc.cyfile.crypto.KeyVaultService;

public class AESCryptoService implements CryptoService {
    private KeyVaultService keyVaultService;

    private static final int BLOCK_SIZE = 16;
    private static final String ALGORITHM = "AES/CBC/PKCS7Padding";
    private static final String PROVIDER = "AndroidKeyStore";


    public AESCryptoService(KeyVaultService keyVaultService) {
        this.keyVaultService = keyVaultService;
    }

    public void init(String passphrase) throws InvalidKeyException {
        keyVaultService.unlockVault(passphrase);
    }

    @Override
    public String encrypt(String data) throws InvalidCryptoOperationException {
        byte[] encryptedBytes = encrypt(data.getBytes(StandardCharsets.UTF_8));
        return Base64.encodeToString(encryptedBytes, Base64.DEFAULT);
    }

    @Override
    public byte[] encrypt(byte[] data) throws InvalidCryptoOperationException {
        Key key = keyVaultService.getEncryptionKey();
        byte[] currentIV;
        Cipher encCipher;

        try {
            encCipher = Cipher.getInstance(ALGORITHM);
            encCipher.init(Cipher.ENCRYPT_MODE, key);

            currentIV = encCipher.getIV();
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException e) {
            throw new InvalidCryptoOperationException(e);
        }

        try {
            byte[] encBytes = encCipher.doFinal(data);
            byte[] ret = new byte[encBytes.length + currentIV.length];
            System.arraycopy(currentIV, 0, ret, 0, currentIV.length);
            System.arraycopy(encBytes, 0, ret, currentIV.length, encBytes.length);
            return ret;
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            throw new InvalidCryptoOperationException(e);
        }
    }

    @Override
    public byte[] decrypt(byte[] cipherData) throws InvalidCryptoOperationException {
        Key key = keyVaultService.getEncryptionKey();
        try {
            byte[] iv = new byte[BLOCK_SIZE];
            byte[] encryptedData = new byte[cipherData.length - BLOCK_SIZE];
            System.arraycopy(cipherData, 0, iv, 0, BLOCK_SIZE);
            System.arraycopy(cipherData, BLOCK_SIZE, encryptedData, 0,
                    cipherData.length - BLOCK_SIZE);

            IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);
            Cipher decCipher = Cipher.getInstance(ALGORITHM);
            decCipher.init(Cipher.DECRYPT_MODE, key, ivParameterSpec);

            return decCipher.doFinal(encryptedData);
        } catch (IllegalBlockSizeException |
                BadPaddingException |
                NoSuchPaddingException |
                InvalidAlgorithmParameterException |
                InvalidKeyException |
                NoSuchAlgorithmException e) {
            throw new InvalidCryptoOperationException(e);
        }
    }

    public String decrypt(String encryptedData) throws InvalidCryptoOperationException {
        byte[] data = Base64.decode(encryptedData, Base64.DEFAULT);
        byte[] decValue = decrypt(data);
        return new String(decValue, StandardCharsets.UTF_8);
    }
}