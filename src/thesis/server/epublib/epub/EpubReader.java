package thesis.server.epublib.epub;


import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.xml.parsers.ParserConfigurationException;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import thesis.server.epublib.Constants;
import thesis.server.epublib.domain.Book;
import thesis.server.epublib.domain.Metadata;
import thesis.server.epublib.domain.Resource;
import thesis.server.epublib.service.MediatypeService;
import thesis.server.epublib.util.ResourceUtil;
import thesis.server.epublib.util.StringUtil;

/**
 * Reads an epub file.
 * 
 * @author paul
 * 
 */
public class EpubReader {

	private static final Logger log = LoggerFactory.getLogger(EpubReader.class);
	private BookProcessor bookProcessor = BookProcessor.IDENTITY_BOOKPROCESSOR;
	
	
	public EpubReader(){
	}
	

	public Book readEpub(InputStream in) throws IOException {
		return readEpub(in, Constants.ENCODING);
	}

	public Book readEpub(ZipInputStream in) throws IOException {
		return readEpub(in, Constants.ENCODING);
	}

	/**
	 * Read epub from inputstream
	 * 
	 * @param in
	 *            the inputstream from which to read the epub
	 * @param encoding
	 *            the encoding to use for the html files within the epub
	 * @return
	 * @throws IOException
	 */
	public Book readEpub(InputStream in, String encoding) throws IOException {
		return readEpub(new ZipInputStream(in), encoding);
	}

	public Book readEpub(ZipInputStream in, String encoding) throws IOException {
		Book result = new Book();
		Map<String, Resource> resources = readResources(in, encoding);
		handleMimeType(result, resources);
		String packageResourceHref = getPackageResourceHref(result, resources);
		Resource packageResource = processPackageResource(packageResourceHref,
				result, resources);
		result.setOpfResource(packageResource);
		Resource ncxResource = processNcxResource(packageResource, result);
		result.setNcxResource(ncxResource);
		result = postProcessBook(result);
		return result;
	}

	private Book postProcessBook(Book book) {
		if (bookProcessor != null) {
			book = bookProcessor.processBook(book);
		}
		return book;
	}

	private Resource processNcxResource(Resource packageResource, Book book) {
		return NCXDocument.read(book, this);
	}

	private Resource processPackageResource(String packageResourceHref,
			Book book, Map<String, Resource> resources) {
		Resource packageResource = resources.remove(packageResourceHref);
		try {
			PackageDocumentReader.read(packageResource, this, book, resources);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return packageResource;
	}

	private String getPackageResourceHref(Book book,
			Map<String, Resource> resources) {
		String defaultResult = "OEBPS/content.opf";
		String result = defaultResult;

		Resource containerResource = resources.remove("META-INF/container.xml");
		if (containerResource == null) {
			return result;
		}
		try {
			Document document = ResourceUtil.getAsDocument(containerResource);
			Element rootFileElement = (Element) ((Element) document
					.getDocumentElement().getElementsByTagName("rootfiles")
					.item(0)).getElementsByTagName("rootfile").item(0);
			result = rootFileElement.getAttribute("full-path");
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		if (StringUtil.isBlank(result)) {
			result = defaultResult;
		}
		return result;
	}

	private void handleMimeType(Book result, Map<String, Resource> resources) {
		resources.remove("mimetype");
	}

	private Map<String, Resource> readResources(ZipInputStream in,
			String defaultHtmlEncoding) throws IOException {
		Map<String, Resource> result = new HashMap<String, Resource>();
		for (ZipEntry zipEntry = in.getNextEntry(); zipEntry != null; zipEntry = in
				.getNextEntry()) {
			// System.out.println(zipEntry.getName());
			if (zipEntry.isDirectory()) {
				continue;
			}
			Resource resource = ResourceUtil.createResource(zipEntry, in);
			if (resource.getMediaType() == MediatypeService.XHTML) {
				resource.setInputEncoding(defaultHtmlEncoding);
			}
			result.put(resource.getHref(), resource);
		}
		return result;
	}

	public Metadata readEpubMetadata(InputStream in) throws IOException {
		return readEpubMetadata(in, Constants.ENCODING);
	}

	public Metadata readEpubMetadata(ZipInputStream in) throws IOException {
		return readEpubMetadata(in, Constants.ENCODING);
	}

	
	public Metadata readEpubMetadata(InputStream in, String encoding) throws IOException {
		return readEpubMetadata(new ZipInputStream(in), encoding);
	}

	
	
	public Metadata readEpubMetadata(ZipInputStream in, String encoding) throws IOException  {
		Book result = new Book();
		Map<String, Resource> resources = readResources(in, encoding);
		handleMimeType(result, resources);
		String packageResourceHref = getPackageResourceHref(result, resources);
		Resource packageResource = resources.remove(packageResourceHref);
		try {
			PackageDocumentReader.readMetaData(packageResource, this, result, resources);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result.getMetadata();
	}

}
