package thesis.server.epubstore;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLStreamException;

import thesis.server.epublib.domain.Book;
import thesis.server.epublib.epub.EpubReader;
import thesis.server.epublib.epub.EpubWriter;
import thesis.server.epublib.util.IOUtil;

public enum EpubPackager {
	instance;

	private final static String tempFolder = "/home/tas0s/thesis.server.WORKING/tempEpubs/";
	public void create(OutputStream out, String epubId, String key) {
		DBAccess dao = new DBAccess();
		final String epubPath = dao.getEpubLocation(Integer.parseInt(epubId));
		try {
			FileInputStream epubStream = new FileInputStream(epubPath);

			Book epub = (new EpubReader()).readEpub(epubStream);
			OutputStream str = new FileOutputStream(tempFolder + epub.getTitle()+ ".epub");
			EpubEncrypter encr = new EpubEncrypter(key);
			new EpubWriter(encr).write(epub, str);
			IOUtil.closeQuietly(str);
			InputStream inp = new FileInputStream(tempFolder + epub.getTitle()+ ".epub");
			IOUtil.copy(inp, out);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (XMLStreamException e) {
			e.printStackTrace();
		} catch (FactoryConfigurationError e) {
			e.printStackTrace();
		}catch(Exception e){
			e.printStackTrace();
		}

	}

}
