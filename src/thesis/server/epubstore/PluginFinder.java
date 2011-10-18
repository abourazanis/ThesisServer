package thesis.server.epubstore;

import java.io.File;

import net.xeoh.plugins.base.PluginManager;
import net.xeoh.plugins.base.impl.PluginManagerFactory;
import net.xeoh.plugins.base.options.addpluginsfrom.OptionReportAfter;
import net.xeoh.plugins.base.options.getplugin.OptionCapabilities;
import net.xeoh.plugins.base.util.uri.ClassURI;
import thesis.server.plugins.EncDecPlugin;

/*
 * Interface that plugins must implement
 * public interface EncDecPlugin extends  Plugin {

	public byte[] encrypt(byte[] data, String pass) throws Exception;
	
	public String getDecryptionCode(String pass);
	
	public void init();	
}

its plugin must provide a capabilities method so we it can be distinct among others
ex.@Capabilities
	public String[] capabilities() { return new String[] {"name:MyPlugin"}; }
 */


public class PluginFinder {

	private final static String pluginsDir = "/home/tas0s/thesis.server.WORKING/libCreator/plugins/";
	private PluginManager pm;

	// Singleton initialiser

	public PluginFinder() {

		// JSPFProperties prop = new JSPFProperties();
		// prop.setProperty(PluginManager.class, "logging.level", "FINEST");

		pm = PluginManagerFactory.createPluginManager();
		pm.addPluginsFrom(ClassURI.CLASSPATH, new OptionReportAfter());
		//pm.addPluginsFrom(new File("/opt/apache-tomcat-7.0.11/lib/encdec_plugins.jar").toURI(),new OptionReportAfter());
	}

	public EncDecPlugin getPlugin(String name) {
		EncDecPlugin pl = pm.getPlugin(EncDecPlugin.class, new OptionCapabilities("name:" + name));
		return pl;
	}

}
