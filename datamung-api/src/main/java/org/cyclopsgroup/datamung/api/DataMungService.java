package org.cyclopsgroup.datamung.api;

import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import org.cyclopsgroup.datamung.api.types.ExportHandler;
import org.cyclopsgroup.datamung.api.types.ExportInstanceRequest;

@Path("datamung")
public interface DataMungService {

	@PUT
	@Path("/exportInstance/{exportId}")
	ExportHandler exportInstance(@PathParam("exportId") String exportId,
			ExportInstanceRequest request);
}
