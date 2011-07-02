package thesis.server.epubstore;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.security.SecureRandom;
import java.security.Security;
import java.util.Arrays;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import thesis.server.epublib.domain.Resource;
import thesis.server.epublib.service.EncryptService;
import thesis.server.epublib.util.IOUtil;
import thesis.server.epublib.util.ResourceUtil;

public class EpubEncrypter implements EncryptService {

	private static final int iterations = 500;
	private static final int keyLength = 256;
	private static final SecureRandom random = new SecureRandom();
	private String pass;
	private byte[] salt;
	private byte[] iv;

	private  static long count = 0;

	private final String scriptCommand = "sh /home/tas0s/createLib.sh ";
	private final String compilePath = "/home/tas0s/libCreator/temp/";
	private final String nativeCompileEnv = "/home/tas0s/libCreator/orig";

	public EpubEncrypter() {
		this(null);
	}

	public EpubEncrypter(String key) {
		super();
//		StringBuilder sb = new StringBuilder();
//		for (String x : keys)
//			sb.append(x);
//		this.pass = sb.toString();
		// TODO:REMOVE!!
		this.pass = "E6BF46F4709CEA7A18502D564F70FC81";
		//this.pass = key;
		Security.insertProviderAt(new BouncyCastleProvider(), 1);
	}

	public void setPass(String pass) {
		this.pass = pass;
	}

	private void setSalt(byte[] data) {
		this.salt = data;
	}

	private SecretKey generateKey(String passphrase) throws Exception {
		PBEKeySpec keySpec = new PBEKeySpec(passphrase.toCharArray(), salt,
				iterations, keyLength);
		SecretKeyFactory keyFactory = SecretKeyFactory
				.getInstance("PBEWithSHA256And256BitAES-CBC-BC");// PBEWithMD5And256BitAES-CBC-OpenSSL
		return keyFactory.generateSecret(keySpec);
	}

	private IvParameterSpec generateIV(Cipher cipher) throws Exception {
		byte[] ivBytes = new byte[cipher.getBlockSize()];
		random.nextBytes(ivBytes);
		IvParameterSpec spec = new IvParameterSpec(ivBytes);
		iv = spec.getIV();
		return spec;
	}

	private byte[] encrypt(String passphrase, byte[] plaintext)
			throws Exception {

		byte[] salt = new byte[8];
        System.arraycopy(plaintext, 0, salt, 0, 8); 
		setSalt(salt);

		SecretKey key = generateKey(passphrase);

		Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding","BC");
		cipher.init(Cipher.ENCRYPT_MODE, key, generateIV(cipher), random);
		int block = cipher.getBlockSize();
		byte[] encD = cipher.doFinal(plaintext,8,plaintext.length - 8); //to not touch the salt
		byte[] data = new byte[encD.length + block];
		System.arraycopy(iv, 0, data, 0, block);
		System.arraycopy(encD, 0, data, block, encD.length);
		
		return data;
	}

	@Override
	public byte[] encrypt(InputStream stream) throws Exception {
		byte[] data = null;
		try {
			data = IOUtil.toByteArray(stream);
			data = encrypt(pass, data);
		} catch (IOException e) {
			e.printStackTrace();
		}

		return data;
	}

	@Override
	public byte[] encrypt(Reader reader) throws Exception {

		byte[] data = null;
		try {
			data = IOUtil.toByteArray(reader, "UTF-8"); // TODO: make encoding
														// dynamic
			data = encrypt(pass, data);
		} catch (IOException e) {
			e.printStackTrace();
		}

		return data;
	}

	@Override
	public Resource encrypt(Resource resource) throws Exception {

		byte[] data = resource.getData();
		data = encrypt(pass, data);
		resource.setData(data);
		return resource;
	}

	@Override
	public byte[] encrypt(byte[] data) throws Exception {
		data = encrypt(pass, data);
		return null;
	}

	private Resource generateDecryptLib() {
		Resource lib = null;
		String newPath = compilePath + pass.toString().substring(0, 5) + "_" + count;
		File compileEnv = new File(nativeCompileEnv);
		File tempCompile = new File(newPath);
		
		try {
			
			//create temp folder for compilation 
			copyDirectory(compileEnv, tempCompile);
			
			//get the decoding method source code and store it in dec/dec.cpp
			DBAccess dao = new DBAccess();
			//TODO: dynamically decryption method based on some policy
			String libraryContent = dao.getDecryptionSourceCode(1); 
			libraryContent = libraryContent.replace("[JAVA_PARSING_KEY]", pass).replace("[JAVA_PARSING_IV]", String.valueOf(iv.length));
			IOUtil.writeStringToFile(new File(newPath + "/dec/dec.cpp"), libraryContent, "UTF-8");
			
			//execution
			Runtime rt = Runtime.getRuntime();
			Process proc = rt.exec(scriptCommand + newPath );
			InputStream stderr = proc.getErrorStream();
			InputStreamReader isr = new InputStreamReader(stderr);
			BufferedReader br = new BufferedReader(isr);
			String line = null;
			System.out.println("<Compile native lib>");
			while ((line = br.readLine()) != null)
				System.out.println(line);
			System.out.println("</Compile native lib>");
			int exitVal = proc.waitFor();
			System.out.println("Process exitValue: " + exitVal);

			//resource creation
			lib = ResourceUtil.createResource(new File(newPath + "/libs/armeabi/libdec.so"));
		} catch (Throwable t) {
			t.printStackTrace();
		}
		
		//tempCompile.delete();
		count++;
		if(count == Long.MAX_VALUE) count = 0; //reset counter;
		return lib;
	}

	@Override
	public void writeDecryptLib(ZipOutputStream resultStream)
			throws IOException {
		Resource lib = generateDecryptLib();
		try {
			resultStream.putNextEntry(new ZipEntry("META-INF/libdec.so"));
			InputStream inputStream = lib.getInputStream();
			IOUtil.copy(inputStream, resultStream);
			inputStream.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	 private void copyDirectory(File sourceLocation , File targetLocation)
	    throws IOException {
	        
	        if (sourceLocation.isDirectory()) {
	            if (!targetLocation.exists()) {
	                targetLocation.mkdir();
	            }
	            
	            String[] children = sourceLocation.list();
	            for (int i=0; i<children.length; i++) {
	                copyDirectory(new File(sourceLocation, children[i]),
	                        new File(targetLocation, children[i]));
	            }
	        } else {
	            
	            InputStream in = new FileInputStream(sourceLocation);
	            OutputStream out = new FileOutputStream(targetLocation);
	            
	            // Copy the bits from instream to outstream
	            byte[] buf = new byte[1024];
	            int len;
	            while ((len = in.read(buf)) > 0) {
	                out.write(buf, 0, len);
	            }
	            in.close();
	            out.close();
	        }
	    }

	/*
	 * private static String decrypt(String passphrase, byte [] ciphertext)
	 * throws Exception { SecretKey key = generateKey(passphrase);
	 * 
	 * Cipher cipher = Cipher.getInstance("AES/CTR/NOPADDING");
	 * cipher.init(Cipher.DECRYPT_MODE, key, generateIV(cipher), random); return
	 * new String(cipher.doFinal(ciphertext)); }
	 */

}
