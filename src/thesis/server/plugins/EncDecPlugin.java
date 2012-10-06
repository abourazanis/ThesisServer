package thesis.server.plugins;

import net.xeoh.plugins.base.Plugin;

public interface EncDecPlugin extends  Plugin {

	public byte[] encrypt(byte[] data, String pass) throws Exception;
	
	public String getDecryptionCode(String pass);
	
	public void init();
	
	public String[] capabilities();
}
