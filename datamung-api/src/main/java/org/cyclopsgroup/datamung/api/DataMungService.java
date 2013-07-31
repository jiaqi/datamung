package org.cyclopsgroup.datamung.api;

import javax.ws.rs.GET;
import javax.ws.rs.MatrixParam;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;

import org.cyclopsgroup.datamung.api.types.ExportHandler;
import org.cyclopsgroup.datamung.api.types.ExportInstanceRequest;
import org.cyclopsgroup.datamung.api.types.ExportSnapshotRequest;
import org.cyclopsgroup.datamung.api.types.WorkflowDetail;
import org.cyclopsgroup.datamung.api.types.WorkflowList;

/**
 * RESTful service interface facade
 */
@Path( "datamung" )
public interface DataMungService
{
    /**
     * Start process of exporting an RDS instance to S3
     *
     * @param exportId A unique identifier of export job
     * @param request Details of job
     * @return A handler to reference running job in future
     */
    @PUT
    @Path( "/exportInstance/{exportId}" )
    ExportHandler exportInstance( @PathParam( "exportId" )
    String exportId, ExportInstanceRequest request );

    /**
     * Start process of exporting an RDS snapshot to S3
     *
     * @param exportId A unique identifier of export job
     * @param request Details of job
     * @return A handler to reference running job in future
     */
    @PUT
    @Path( "/exportSnapshot/{exportId}" )
    ExportHandler exportSnapshot( @PathParam( "exportId" )
    String exportId, ExportSnapshotRequest request );

    @GET
    @Path( "/workflow/{workflowId}" )
    WorkflowDetail getWorkflow( @PathParam( "workflowId" )
    String workflowId, @QueryParam( "runId" )
    String runId );

    @GET
    @Path( "/workflow" )
    WorkflowList listWorkflows( @MatrixParam( "closed" )
    boolean closed );
}
