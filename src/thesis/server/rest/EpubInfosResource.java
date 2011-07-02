package thesis.server.rest;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.StreamingOutput;
import javax.ws.rs.core.UriInfo;

import thesis.server.epubstore.EpubInfo;
import thesis.server.epubstore.EpubPackager;
import thesis.server.epubstore.EpubProvider;

@Path("/epubs")
public class EpubInfosResource {

	// Allows to insert contextual objects into the class,
	// e.g. ServletContext, Request, Response, UriInfo
	@Context
	UriInfo uriInfo;
	@Context
	Request request;

	// Return the list of todos to the user in the browser
	@GET
	@Produces(MediaType.TEXT_XML)
	public List<EpubInfo> getEpubInfosBrowser() {
		List<EpubInfo> epubs = new ArrayList<EpubInfo>();
		epubs.addAll(EpubProvider.instance.getEpubList().values());
		return epubs;
	}

	// Return the list of todos for applications
	@GET
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public List<EpubInfo> getEpubInfos() {
		List<EpubInfo> epubs = new ArrayList<EpubInfo>();
		epubs.addAll(EpubProvider.instance.getEpubList().values());
		return epubs;
	}

	// retuns the number of todos
	// Use http://localhost:8080/de.vogella.jersey.todo/rest/todos/count
	// to get the total number of records
	@GET
	@Path("count")
	@Produces(MediaType.TEXT_PLAIN)
	public String getCount() {
		int count = EpubProvider.instance.getEpubList().size();
		return String.valueOf(count);
	}

	// Defines that the next path parameter after todos is
	// treated as a parameter and passed to the TodoResources
	// Allows to type http://localhost:8080/de.vogella.jersey.todo/rest/todos/1
	// 1 will be treaded as parameter todo and passed to TodoResource
	@Path("{epubInfo}")
	public EpubInfoResource getEpubInfo(@PathParam("epubInfo") String id) {
		return new EpubInfoResource(uriInfo, request, id);
	}

	// Defines that the next path parameter after todos is
	// treated as a parameter and passed to the TodoResources
	// Allows to type http://localhost:8080/de.vogella.jersey.todo/rest/todos/1
	// 1 will be treaded as parameter todo and passed to TodoResource
	@GET
	@Path("{epubId}/")
	@Produces("application/epub+zip")
	public StreamingOutput getEpub(@PathParam("epubId") String id){//, @FormParam("key") String key) {
		final String epubId = id;
		final String uid = "";//key;
		//final List<String> keyList = Arrays.asList(key.split(","));
		return new StreamingOutput() {
			public void write(OutputStream output) throws IOException,
					WebApplicationException {
				try {
					EpubPackager.instance.create(output, epubId, uid);
				} catch (Exception e) {
					throw new WebApplicationException(e);
				}
			}
		};

	}

}
