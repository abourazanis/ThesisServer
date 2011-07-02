package thesis.server.epublib.epub;

import java.util.ArrayList;
import java.util.List;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import thesis.server.epublib.domain.Book;

/**
 * A book processor that combines several other bookprocessors
 * 
 * Fixes coverpage/coverimage.
 * Cleans up the XHTML.
 * 
 * @author paul.siegmann
 *
 */
public class BookProcessorPipeline implements BookProcessor {

	private Logger log = LoggerFactory.getLogger(BookProcessorPipeline.class);
	private List<BookProcessor> bookProcessors;

	public BookProcessorPipeline() {
		this(null);
	}
	
	public BookProcessorPipeline(List<BookProcessor> bookProcessingPipeline) {
		this.bookProcessors = bookProcessingPipeline;
	}

	
	@Override
	public Book processBook(Book book) {
		if (bookProcessors == null) {
			return book;
		}
		for(BookProcessor bookProcessor: bookProcessors) {
			try {
				book = bookProcessor.processBook(book);
			} catch(Exception e) {
				log.error(e.getMessage(), e);
			}
		}
		return book;
	}

	public void addBookProcessor(BookProcessor bookProcessor) {
		if (this.bookProcessors == null) {
			bookProcessors = new ArrayList<BookProcessor>();
		}
		this.bookProcessors.add(bookProcessor);
	}
	
	public List<BookProcessor> getBookProcessors() {
		return bookProcessors;
	}


	public void setBookProcessingPipeline(List<BookProcessor> bookProcessingPipeline) {
		this.bookProcessors = bookProcessingPipeline;
	}

}