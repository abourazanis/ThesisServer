package thesis.server.epublib.service;

import java.util.HashMap;
import java.util.Map;

import thesis.server.epublib.domain.MediaType;
import thesis.server.epublib.util.StringUtil;



/**
 * Manages mediatypes that are used by epubs
 * 
 * @author paul
 *
 */
public class MediatypeService {

	public static final MediaType XHTML = new MediaType("application/xhtml+xml", ".xhtml", new String[] {".htm", ".html", ".xhtml"});
	public static final MediaType EPUB = new MediaType("application/epub+zip", ".epub");
	public static final MediaType JPG = new MediaType("image/jpeg", ".jpg", new String[] {".jpg", ".jpeg"});
	public static final MediaType PNG = new MediaType("image/png", ".png");
	public static final MediaType GIF = new MediaType("image/gif", ".gif");
	public static final MediaType CSS = new MediaType("text/css", ".css");
	public static final MediaType SVG = new MediaType("image/svg+xml", ".svg");
	public static final MediaType TTF = new MediaType("application/x-truetype-font", ".ttf");
	public static final MediaType NCX = new MediaType("application/x-dtbncx+xml", ".ncx");
	public static final MediaType XPGT = new MediaType("application/adobe-page-template+xml", ".xpgt");
	public static final MediaType OPENTYPE = new MediaType("font/opentype", ".otf");
	
	public static MediaType[] mediatypes = new MediaType[] {
		XHTML, EPUB, JPG, PNG, GIF, CSS, SVG, TTF, NCX, XPGT, OPENTYPE
	};
	
	public static Map<String, MediaType> mediaTypesByName = new HashMap<String, MediaType>();
	static {
		for(int i = 0; i < mediatypes.length; i++) {
			mediaTypesByName.put(mediatypes[i].getName(), mediatypes[i]);
		}
	}
	
	public static boolean isBitmapImage(MediaType mediaType) {
		return mediaType == JPG || mediaType == PNG || mediaType == GIF;
	}
	
	/**
	 * Gets the MediaType based on the file extension.
	 * Null of no matching extension found.
	 * 
	 * @param filename
	 * @return
	 */
	public static MediaType determineMediaType(String filename) {
		for(int i = 0; i < mediatypes.length; i++) {
			MediaType mediatype = mediatypes[i];
			for(String extension: mediatype.getExtensions()) {
				if(StringUtil.endsWithIgnoreCase(filename, extension)) {
					return mediatype;
				}
			}
		}
		return null;
	}

	public static MediaType getMediaTypeByName(String mediaTypeName) {
		return mediaTypesByName.get(mediaTypeName);
	}
}
