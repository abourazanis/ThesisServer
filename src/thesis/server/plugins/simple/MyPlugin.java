package thesis.server.plugins.simple;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.SecureRandom;
import java.security.Security;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;

import net.xeoh.plugins.base.annotations.Capabilities;
import net.xeoh.plugins.base.annotations.PluginImplementation;
import net.xeoh.plugins.base.annotations.meta.Author;
import net.xeoh.plugins.base.annotations.meta.Version;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import thesis.server.plugins.EncDecPlugin;

@Author(name = "A.Bourazanis")
@Version(version = 1)
@PluginImplementation
public class MyPlugin implements EncDecPlugin {
	
	private static final int iterations = 1;
	private static final int keyLength = 256;
	private static final SecureRandom random = new SecureRandom();
	private byte[] salt;
	private byte[] iv;
	
	static final String HEXES = "0123456789ABCDEF";
	
	public MyPlugin() {
		super();
	}
	
	public void init(){
		Security.insertProviderAt(new BouncyCastleProvider(), 1);
	}
	
	private void setSalt(byte[] data) {
		this.salt = data;
	}
	
	private SecretKey generateKey(String passphrase) throws Exception {
		PBEKeySpec keySpec = new PBEKeySpec(passphrase.toCharArray(), salt,
				iterations, keyLength);
		SecretKeyFactory keyFactory = SecretKeyFactory
				.getInstance("PBEWITHMD5AND256BITAES-CBC-OPENSSL");
		
		return keyFactory.generateSecret(keySpec);

	}

	private IvParameterSpec generateIV(Cipher cipher) throws Exception {
		byte[] ivBytes = new byte[cipher.getBlockSize()];
		random.nextBytes(ivBytes);
		IvParameterSpec spec = new IvParameterSpec(ivBytes);
		iv = spec.getIV();
		return spec;
	}

	public byte[] encrypt(byte[] plaintext, String pass) throws Exception {
		byte[] tSalt = new byte[8];
        random.nextBytes(tSalt);
        setSalt(tSalt);

		SecretKey key = generateKey(pass);

		Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
		cipher.init(Cipher.ENCRYPT_MODE, key, generateIV(cipher));
		int block = cipher.getBlockSize();
		byte[] encD = cipher.doFinal(plaintext);
		byte[] data = new byte[encD.length + block + 8];
		System.arraycopy(iv, 0, data, 0, block);
		//System.arraycopy(MAGIC_SALTED_BYTES, 0, data, 0, 8);
		System.arraycopy(salt, 0, data, block, 8);
		System.arraycopy(encD, 0, data, block+8, encD.length);
		
//		System.out.println("PlainText size:  " + plaintext.length);
//		System.out.println("Encoded size:  " + encD.length);
//		System.out.println("Final size:  " + data.length);
//		System.out.println("IV " + getHex(iv));
//		System.out.println("Key " + getHex(key.getEncoded()));
//		System.out.println("Salt " + getHex(salt));
		
		return data;
	}

	public String getDecryptionCode(String pass) {
		String code = null;
		try {
			code = readTextFile("code.cpp").replace("DECRYPTION_ID", pass);
		} catch (IOException e) {
			e.printStackTrace();
		} 

		return code;
	}

	private String readTextFile(String fileName) throws IOException {
		 
	    InputStream input = getClass().getResourceAsStream(fileName);
	    ByteArrayOutputStream output = new ByteArrayOutputStream(1024);
	 
	    byte[] buffer = new byte[512];
	 
	    int bytes;
	 
	    while ((bytes = input.read (buffer)) > 0) {
	        output.write (buffer, 0, bytes);
	    }
	 
	    input.close ();
	    return new String(output.toByteArray());
	}
	
	
	public static String getHex( byte [] raw ) {
	    if ( raw == null ) {
	      return null;
	    }
	    final StringBuilder hex = new StringBuilder( 2 * raw.length );
	    for ( final byte b : raw ) {
	      hex.append(HEXES.charAt((b & 0xF0) >> 4))
	         .append(HEXES.charAt((b & 0x0F)));
	    }
	    return hex.toString();
	  }
	
	@Capabilities
	public String[] capabilities() { return new String[] {"name:MyPlugin"}; } 

}

