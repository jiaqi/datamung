package org.cyclopsgroup.datamung.service.activities;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cyclopsgroup.datamung.api.types.AgentConfig;
import org.cyclopsgroup.datamung.swf.interfaces.ControlActivities;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.amazonaws.services.identitymanagement.AmazonIdentityManagement;
import com.amazonaws.services.identitymanagement.model.Role;
import com.amazonaws.services.simpleworkflow.flow.JsonDataConverter;

@Component( "workflow.ControlActivities" )
public class ControlActivitiesImpl
    implements ControlActivities
{
    private static final Log LOG =
        LogFactory.getLog( ControlActivitiesImpl.class );

    @Autowired
    private AmazonIdentityManagement iam;

    private final JsonDataConverter converter = new JsonDataConverter();

    /**
     * @inheritDoc
     */
    @Override
    public String createAgentControllerRole( String roleName,
                                             String workflowTaskList )
    {
        Map<String, String> policyVariables = new HashMap<String, String>();
        policyVariables.put( "CONTROLLER_ACCOUNT_ID",
                             ActivityUtils.getAccountId( iam, null ) );
        policyVariables.put( "SWF_DOMAIN", "datamung-test" );
        policyVariables.put( "TASK_LIST", workflowTaskList );

        Role role =
            ActivityUtils.createRole( roleName, iam,
                                      "datamung/agent-controller-policy.json",
                                      policyVariables,
                                      "datamung/agent-controller-trust.json",
                                      null, null );
        return role.getArn();
    }

    /**
     * @inheritDoc
     */
    @Override
    public String createAgentUserData( String taskList )
    {
        AgentConfig config = new AgentConfig();
        config.setWorkflowTaskList( taskList );
        return converter.toData( config );
    }

    /**
     * @inheritDoc
     */
    @Override
    public String createDatabaseName( String snapshotName )
    {
        return snapshotName + "-" + RandomStringUtils.randomAlphanumeric( 6 );
    }

    /**
     * @inheritDoc
     */
    @Override
    public String createSnapshotName( String instanceName )
    {
        return instanceName + "-" + new DateTime().toString( "ddHHmm" );
    }

    /**
     * @inheritDoc
     */
    @Override
    public void deleteRole( String roleName )
    {
        ActivityUtils.deleteRole( roleName, iam, null );
    }
}
