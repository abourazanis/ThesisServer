package thesis.server.epublib.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.security.InvalidKeyException;
import java.util.zip.ZipOutputStream;

import thesis.server.epublib.domain.Resource;




public interface EncryptService {
		
	public byte[] encrypt(byte[] data) throws Exception;
	
	public byte[] encrypt(InputStream stream) throws Exception;
	
	public byte[] encrypt(Reader reader) throws Exception;
	
	public Resource encrypt(Resource resource) throws Exception;
	
	public void writeDecryptLib(ZipOutputStream resultStream) throws IOException;
	
}
