package org.cyclopsgroup.datamung.service.core;

import org.cyclopsgroup.datamung.api.DataMungService;
import org.cyclopsgroup.datamung.api.types.ExportHandler;
import org.cyclopsgroup.datamung.api.types.ExportInstanceRequest;
import org.cyclopsgroup.datamung.api.types.ExportSnapshotRequest;
import org.cyclopsgroup.datamung.service.ServiceConfig;
import org.cyclopsgroup.datamung.swf.interfaces.ExportInstanceWorkflowClientExternalFactory;
import org.cyclopsgroup.datamung.swf.interfaces.ExportInstanceWorkflowClientExternalFactoryImpl;
import org.cyclopsgroup.datamung.swf.interfaces.ExportSnapshotWorkflowClientExternalFactory;
import org.cyclopsgroup.datamung.swf.interfaces.ExportSnapshotWorkflowClientExternalFactoryImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.amazonaws.services.simpleworkflow.AmazonSimpleWorkflow;

@Component( "dataMungService" )
public class DataMungServiceImpl
    implements DataMungService
{
    private final ExportInstanceWorkflowClientExternalFactory instanceWorkflowFactory;

    private final ExportSnapshotWorkflowClientExternalFactory snapshotWorkflowFactory;

    @Autowired
    public DataMungServiceImpl( AmazonSimpleWorkflow swfService,
                                ServiceConfig config )
    {
        instanceWorkflowFactory =
            new ExportInstanceWorkflowClientExternalFactoryImpl(
                                                                 swfService,
                                                                 config.getSwfDomainName() );
        snapshotWorkflowFactory =
            new ExportSnapshotWorkflowClientExternalFactoryImpl(
                                                                 swfService,
                                                                 config.getSwfDomainName() );
    }

    /**
     * @inheritDoc
     */
    @Override
    public ExportHandler exportInstance( String exportId,
                                         ExportInstanceRequest request )
    {
        instanceWorkflowFactory.getClient( exportId ).export( request );
        return ExportHandler.of( exportId, null );
    }

    /**
     * @inheritDoc
     */
    @Override
    public ExportHandler exportSnapshot( String exportId,
                                         ExportSnapshotRequest request )
    {
        snapshotWorkflowFactory.getClient( exportId ).export( request );
        return ExportHandler.of( exportId, null );
    }
}
