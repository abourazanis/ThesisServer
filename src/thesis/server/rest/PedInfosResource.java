package thesis.server.rest;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.StreamingOutput;
import javax.ws.rs.core.UriInfo;

import thesis.server.pedstore.PedInfo;
import thesis.server.pedstore.PedPackager;
import thesis.server.pedstore.PedProvider;

@Path("/peds")
public class PedInfosResource {

	// Allows to insert contextual objects into the class,
	// e.g. ServletContext, Request, Response, UriInfo
	@Context
	UriInfo uriInfo;
	@Context
	Request request;

	// Return the list of todos to the user in the browser
	@GET
	@Produces(MediaType.TEXT_XML)
	public List<PedInfo> getPedInfosBrowser() {
		List<PedInfo> peds = new ArrayList<PedInfo>();
		peds.addAll(PedProvider.instance.getPedList().values());
		return peds;
	}

	// Return the list of todos for applications
	@GET
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public List<PedInfo> getPedInfos() {
		List<PedInfo> peds = new ArrayList<PedInfo>();
		peds.addAll(PedProvider.instance.getPedList().values());
		return peds;
	}

	// retuns the number of todos
	// Use http://localhost:8080/de.vogella.jersey.todo/rest/todos/count
	// to get the total number of records
	@GET
	@Path("count")
	@Produces(MediaType.TEXT_PLAIN)
	public String getCount() {
		int count = PedProvider.instance.getPedList().size();
		return String.valueOf(count);
	}

	// Defines that the next path parameter after todos is
	// treated as a parameter and passed to the TodoResources
	// Allows to type http://localhost:8080/de.vogella.jersey.todo/rest/todos/1
	// 1 will be treaded as parameter todo and passed to TodoResource
	@Path("{pedInfo}")
	public PedInfoResource getPedInfo(@PathParam("pedInfo") String id) {
		return new PedInfoResource(uriInfo, request, id);
	}

	// Defines that the next path parameter after todos is
	// treated as a parameter and passed to the TodoResources
	// Allows to type http://localhost:8080/de.vogella.jersey.todo/rest/todos/1
	// 1 will be treaded as parameter todo and passed to TodoResource
	@GET
	@Path("{pedId}/")
	@Produces("application/zip")
	public StreamingOutput getPed(@PathParam("pedId") String id) {
		final String pedId = id;
		return new StreamingOutput() {
			public void write(OutputStream output) throws IOException,
					WebApplicationException {
				try {
					PedPackager.instance.create(output, pedId);
				} catch (Exception e) {
					throw new WebApplicationException(e);
				}
			}
		};

	}

}
