package thesis.server.epublib.epub;

import java.io.OutputStream;

import thesis.server.epublib.domain.Resource;


public interface HtmlProcessor {
	
	void processHtmlResource(Resource resource, OutputStream out);
}
