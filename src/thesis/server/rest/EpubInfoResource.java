package thesis.server.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import thesis.server.epubstore.EpubInfo;
import thesis.server.epubstore.EpubProvider;

public class EpubInfoResource {
	@Context
	UriInfo uriInfo;
	@Context
	Request request;
	String id;

	public EpubInfoResource(UriInfo uriInfo, Request request, String id) {
		this.uriInfo = uriInfo;
		this.request = request;
		this.id = id;
	}

	// Application integration
	@GET
	@Produces(MediaType.APPLICATION_XML)
	public EpubInfo getPedInfo() {
		EpubInfo epub = EpubProvider.instance.getEpubList().get(id);
		if (epub == null)
			throw new RuntimeException("Get: Epub with " + id + " not found");
		return epub;
	}

	// For the browser
	@GET
	@Produces(MediaType.TEXT_XML)
	public EpubInfo getPedInfoHTML() {
		EpubInfo epub = EpubProvider.instance.getEpubList().get(id);
		if (epub == null)
			throw new RuntimeException("Get: Epub with " + id + " not found");
		return epub;
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
