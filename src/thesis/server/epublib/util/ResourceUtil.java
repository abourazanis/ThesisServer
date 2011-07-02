package thesis.server.epublib.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import thesis.server.epublib.Constants;
import thesis.server.epublib.domain.MediaType;
import thesis.server.epublib.domain.Resource;
import thesis.server.epublib.epub.EpubProcessorSupport;
import thesis.server.epublib.service.MediatypeService;

/**
 * Various resource utility methods
 * 
 * @author paul
 * 
 */
public class ResourceUtil {

	private static Logger log = LoggerFactory.getLogger(ResourceUtil.class);

	public static Resource createResource(File file) throws IOException {
		if (file == null) {
			return null;
		}
		MediaType mediaType = MediatypeService.determineMediaType(file
				.getName());
		byte[] data = IOUtil.toByteArray(new FileInputStream(file));
		Resource result = new Resource(data, mediaType);
		return result;
	}

	/**
	 * Creates a resource with as contents a html page with the given title.
	 * 
	 * @param title
	 * @param href
	 * @return
	 */
	public static Resource createResource(String title, String href) {
		String content = "<html><head><title>" + title
				+ "</title></head><body><h1>" + title + "</h1></body></html>";
		return new Resource(null, content.getBytes(), href,
				MediatypeService.XHTML, Constants.ENCODING);
	}

	/**
	 * Creates a resource out of the given zipEntry and zipInputStream.
	 * 
	 * @param zipEntry
	 * @param zipInputStream
	 * @return
	 * @throws IOException
	 */
	public static Resource createResource(ZipEntry zipEntry,
			ZipInputStream zipInputStream) throws IOException {
		return new Resource(zipInputStream, zipEntry.getName());

	}

	/**
	 * Creates a resource out of the given zipEntry and InputStream.
	 * 
	 * @param zipEntry
	 * @param InputStream
	 * @return
	 * @throws IOException
	 */
	public static Resource createResource(ZipEntry zipEntry,
			InputStream inputStream) throws IOException {
		return new Resource(inputStream, zipEntry.getName());

	}

	/**
	 * Gets the contents of the Resource as an InputSource in a null-safe
	 * manner.
	 * 
	 */
	public static InputSource getInputSource(Resource resource)
			throws IOException {
		if (resource == null) {
			return null;
		}
		Reader reader = resource.getReader();
		if (reader == null) {
			return null;
		}
		InputSource inputSource = new InputSource(reader);
		return inputSource;
	}

	/**
	 * Reads parses the xml therein and returns the result as a Document
	 */
	public static Document getAsDocument(Resource resource)
			throws UnsupportedEncodingException, SAXException, IOException,
			ParserConfigurationException {
		return getAsDocument(resource,
				EpubProcessorSupport.createDocumentBuilder());
	}

	/**
	 * Reads the given resources inputstream, parses the xml therein and returns
	 * the result as a Document
	 * 
	 * @param resource
	 * @param documentBuilderFactory
	 * @return
	 * @throws UnsupportedEncodingException
	 * @throws SAXException
	 * @throws IOException
	 * @throws ParserConfigurationException
	 */
	public static Document getAsDocument(Resource resource,
			DocumentBuilder documentBuilder)
			throws UnsupportedEncodingException, SAXException, IOException,
			ParserConfigurationException {
		InputSource inputSource = getInputSource(resource);
		if (inputSource == null) {
			return null;
		}
		Document result = documentBuilder.parse(inputSource);
		result.setXmlStandalone(true);
		return result;
	}

	public static Resource getResourceFromEpub(String epubFilePath,
			String resourceHref) throws IOException, ZipException {

		ZipFile file = new ZipFile(epubFilePath);
		ZipEntry zipEntry = file.getEntry(resourceHref);
		if (zipEntry != null) {
			InputStream inp = file.getInputStream(zipEntry);
			return createResource(zipEntry, inp);
		} else {
			return null;
		}

	}
}
