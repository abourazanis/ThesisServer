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

	private static final int iterations = 1;
	private static final int keyLength = 256;
	private static final SecureRandom random = new SecureRandom();
	private String pass;
	private byte[] salt;
	private byte[] iv;
	
	private  static long count = 0;

	private final String scriptCommand = "sh /home/tas0s/thesis.server.WORKING/createLib.sh ";
	private final String compilePath = "/home/tas0s/thesis.server.WORKING/libCreator/temp/";
	private final String nativeCompileEnv = "/home/tas0s/thesis.server.WORKING/libCreator/orig";
	
	static final String HEXES = "0123456789ABCDEF";

	public EpubEncrypter() {
		this(null);
	}

	public EpubEncrypter(String key) {
		super();
		this.pass = key;
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

	private byte[] encrypt(String passphrase, byte[] plaintext)
			throws Exception {

        byte[] tSalt = new byte[8];
        random.nextBytes(tSalt);
        setSalt(tSalt);

		SecretKey key = generateKey(passphrase);

		Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
		cipher.init(Cipher.ENCRYPT_MODE, key, generateIV(cipher));
		int block = cipher.getBlockSize();
		byte[] encD = cipher.doFinal(plaintext);
		byte[] data = new byte[encD.length + block + 8];
		System.arraycopy(iv, 0, data, 0, block);
		//System.arraycopy(MAGIC_SALTED_BYTES, 0, data, 0, 8);
		System.arraycopy(salt, 0, data, block, 8);
		System.arraycopy(encD, 0, data, block+8, encD.length);
		
		System.out.println("PlainText size:  " + plaintext.length);
		System.out.println("Encoded size:  " + encD.length);
		System.out.println("Final size:  " + data.length);
		System.out.println("IV " + getHex(iv));
		System.out.println("Key " + getHex(key.getEncoded()));
		System.out.println("Salt " + getHex(salt));
		
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
		System.out.println("Resource " + resource.getHref());
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
			libraryContent = libraryContent.replace("[DECRYPT_KEY]", pass);
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

}
