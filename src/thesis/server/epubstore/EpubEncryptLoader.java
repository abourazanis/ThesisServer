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
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.SQLException;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import thesis.server.epublib.domain.Resource;
import thesis.server.epublib.util.IOUtil;
import thesis.server.epublib.util.ResourceUtil;
import thesis.server.epublib.util.StringUtil;
import thesis.server.plugins.EncDecPlugin;

public class EpubEncryptLoader {

	private static long count = 0;
	private final String scriptCommand = "sh /home/tas0s/thesis.server.WORKING/createLib.sh ";
	private final String compilePath = "/home/tas0s/thesis.server.WORKING/libCreator/temp/";
	private final String nativeCompileEnv = "/home/tas0s/thesis.server.WORKING/libCreator/orig";
	private final static String pluginsDir = "/home/tas0s/thesis.server.WORKING/libCreator/plugins/";

	private String epubId;
	private String key;

	private EncDecPlugin encdecrypter;
	private PluginFinder finder;

	public EpubEncryptLoader(String id, String key) {
		this.epubId = id;
		this.key = key;
		setPlugin();
	}

	private void setPlugin() {
		try {
			DBAccess dao = new DBAccess();
			String pluginPath = dao.getPluginName(Integer.parseInt(epubId));
			if (StringUtil.isNotBlank(pluginPath)) {
				encdecrypter = new PluginFinder().getPlugin(pluginPath);
				encdecrypter.init();
			}
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	private Resource generateDecryptLib() {
		Resource lib = null;
		String newPath = compilePath + key.toString().substring(0, 5) + "_"
				+ count;
		File compileEnv = new File(nativeCompileEnv);
		File tempCompile = new File(newPath);

		try {

			// create temp folder for compilation
			copyDirectory(compileEnv, tempCompile);

			// get the decoding method source code and store it in dec/dec.cpp
			String libraryContent = encdecrypter.getDecryptionCode(key);
			IOUtil.writeStringToFile(new File(newPath + "/dec/dec.cpp"),
					libraryContent, "UTF-8");

			// execution
			Runtime rt = Runtime.getRuntime();
			Process proc = rt.exec(scriptCommand + newPath);
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

			// resource creation
			lib = ResourceUtil.createResource(new File(newPath
					+ "/libs/armeabi/libdec.so"));
		} catch (Throwable t) {
			t.printStackTrace();
		}

		// tempCompile.delete();
		count++;
		if (count == Long.MAX_VALUE)
			count = 0; // reset counter;
		return lib;
	}

	public void writeDecryptLib(ZipOutputStream resultStream)
			throws IOException {
		
		if(encdecrypter == null) return;
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

	private byte[] encrypt(String pass, byte[] plaintext) throws Exception {
		if (encdecrypter == null)
			return plaintext;
		return encdecrypter.encrypt(plaintext, pass);
	}

	public byte[] encrypt(InputStream stream) throws Exception {
		byte[] data = null;
		try {
			data = IOUtil.toByteArray(stream);
			data = encrypt(key, data);
		} catch (IOException e) {
			e.printStackTrace();
		}

		return data;
	}

	public byte[] encrypt(Reader reader) throws Exception {

		byte[] data = null;
		try {
			data = IOUtil.toByteArray(reader, "UTF-8"); // TODO: make encoding
														// dynamic
			data = encrypt(key, data);
		} catch (IOException e) {
			e.printStackTrace();
		}

		return data;
	}

	public Resource encrypt(Resource resource) throws Exception {

		byte[] data = resource.getData();
		System.out.println("Resource " + resource.getHref());
		data = encrypt(key, data);
		resource.setData(data);
		return resource;
	}

	public byte[] encrypt(byte[] data) throws Exception {
		data = encrypt(key, data);
		return data;
	}

	private void copyDirectory(File sourceLocation, File targetLocation)
			throws IOException {

		if (sourceLocation.isDirectory()) {
			if (!targetLocation.exists()) {
				targetLocation.mkdir();
			}

			String[] children = sourceLocation.list();
			for (int i = 0; i < children.length; i++) {
				copyDirectory(new File(sourceLocation, children[i]), new File(
						targetLocation, children[i]));
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
}
