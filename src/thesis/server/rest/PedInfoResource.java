package thesis.server.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import thesis.server.pedstore.PedInfo;
import thesis.server.pedstore.PedProvider;

public class PedInfoResource {
	@Context
	UriInfo uriInfo;
	@Context
	Request request;
	String id;

	public PedInfoResource(UriInfo uriInfo, Request request, String id) {
		this.uriInfo = uriInfo;
		this.request = request;
		this.id = id;
	}

	// Application integration
	@GET
	@Produces(MediaType.APPLICATION_XML)
	public PedInfo getPedInfo() {
		PedInfo ped = PedProvider.instance.getPedList().get(id);
		if (ped == null)
			throw new RuntimeException("Get: Ped with " + id + " not found");
		return ped;
	}

	// For the browser
	@GET
	@Produces(MediaType.TEXT_XML)
	public PedInfo getPedInfoHTML() {
		PedInfo ped = PedProvider.instance.getPedList().get(id);
		if (ped == null)
			throw new RuntimeException("Get: Ped with " + id + " not found");
		return ped;
	}
	
//	private Response putAndGetResponse(PedInfo pedInfo) {
//		Response res;
//		if(PedProvider.instance.getPedList().containsKey(pedInfo.getId())) {
//			res = Response.noContent().build();
//		} else {
//			res = Response.created(uriInfo.getAbsolutePath()).build();
//		}
//		PedProvider.instance.getPedList().put(pedInfo.getId(),pedInfo);
//		return res;
//	}

}
