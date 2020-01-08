package org.cyclopsgroup.datamung.service.core;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

@Path("system")
public interface SystemService {
  @GET
  @Path("/ping")
  String ping();
}
