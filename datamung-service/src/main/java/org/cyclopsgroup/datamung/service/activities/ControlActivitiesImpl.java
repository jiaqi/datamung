package org.cyclopsgroup.datamung.service.activities;

import com.amazonaws.services.identitymanagement.AmazonIdentityManagement;
import com.amazonaws.services.identitymanagement.AmazonIdentityManagementClient;
import com.amazonaws.services.identitymanagement.model.Role;
import com.amazonaws.services.simpleworkflow.flow.ActivityExecutionContextProvider;
import com.amazonaws.services.simpleworkflow.flow.ActivityExecutionContextProviderImpl;
import com.amazonaws.services.simpleworkflow.flow.JsonDataConverter;
import com.amazonaws.services.simpleworkflow.model.WorkflowExecution;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cyclopsgroup.datamung.api.JobEventListener;
import org.cyclopsgroup.datamung.api.types.AgentConfig;
import org.cyclopsgroup.datamung.api.types.Identity;
import org.cyclopsgroup.datamung.service.ServiceConfig;
import org.cyclopsgroup.datamung.swf.interfaces.Constants;
import org.cyclopsgroup.datamung.swf.interfaces.ControlActivities;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("workflow.ControlActivities")
public class ControlActivitiesImpl implements ControlActivities {
  private static final Log LOG = LogFactory.getLog(ControlActivitiesImpl.class);

  private final String accountId;

  private final ActivityExecutionContextProvider contextProvider =
      new ActivityExecutionContextProviderImpl();

  private final JsonDataConverter converter = new JsonDataConverter();

  private final JobEventListener jobEventListener;

  private final AmazonIdentityManagement serverIam;

  @Autowired
  public ControlActivitiesImpl(
      ServiceConfig config, JobEventListener jobEventListener, AmazonIdentityManagement iam) {
    this.accountId = config.getAwsAccountId();
    this.jobEventListener = jobEventListener;
    this.serverIam = iam;
    LOG.info("AWS account id is " + accountId);
  }

  /** @inheritDoc */
  @Override
  public String createAgentControllerRole(
      String roleName, String workflowTaskList, Identity identity) {
    Map<String, String> policyVariables = new HashMap<String, String>();
    policyVariables.put("CONTROLLER_ACCOUNT_ID", accountId);
    policyVariables.put("SWF_DOMAIN", contextProvider.getActivityExecutionContext().getDomain());
    policyVariables.put("TASK_LIST", workflowTaskList);

    AmazonIdentityManagement clientIam =
        ActivityUtils.createClient(AmazonIdentityManagementClient.class, identity);

    Map<String, String> trustVariables = new HashMap<String, String>();
    trustVariables.put("CLIENT_EXTERNAL_ID", AgentConfig.ROLE_EXTERNAL_ID);
    trustVariables.put("CLIENT_ACCOUNT_ID", ActivityUtils.getAccountId(clientIam));

    Role role =
        ActivityUtils.createRole(
            roleName,
            serverIam,
            "datamung/agent-controller-policy.json",
            policyVariables,
            "datamung/agent-controller-trust.json",
            trustVariables);
    return role.getArn();
  }

  /** @inheritDoc */
  @Override
  public String createAgentUserData(String roleArn, String taskList) {
    AgentConfig config = new AgentConfig();
    config.setControllerRoleArn(roleArn);
    config.setWorkflowTaskList(taskList);
    config.setWorkflowDomain(contextProvider.getActivityExecutionContext().getDomain());
    return converter.toData(config);
  }

  /** @inheritDoc */
  @Override
  public String createDatabaseName(String snapshotName) {
    return snapshotName + "-" + RandomStringUtils.randomAlphanumeric(6);
  }

  /** @inheritDoc */
  @Override
  public String createSnapshotName(String instanceName) {
    return instanceName + "-" + new DateTime().toString("ddHHmm");
  }

  /** @inheritDoc */
  @Override
  public void deleteRole(String roleName) {
    ActivityUtils.deleteRole(roleName, serverIam);
  }

  @Override
  public void notifyActionCompleted(String actionId, String result, long elapsedMillis) {
    jobEventListener.onActionCompleted(actionId, result, elapsedMillis);
  }

  @Override
  public void notifyActionFailed(
      String actionId, String result, String failureDetails, long elaspsedMillis) {
    jobEventListener.onActionFailed(actionId, result, failureDetails, elaspsedMillis);
  }

  @Override
  public String notifyActionStarted(String actionName, String description) {
    String workflowId =
        contextProvider.getActivityExecutionContext().getWorkflowExecution().getWorkflowId();
    if (workflowId.endsWith(Constants.JOB_WORKFLOW_ID_SUFFIX)) {
      workflowId = StringUtils.removeEnd(workflowId, Constants.JOB_WORKFLOW_ID_SUFFIX);
    }
    return jobEventListener.onActionStarted(actionName, description);
  }

  /** @inheritDoc */
  @Override
  public void notifyJobCompleted() {
    jobEventListener.onJobCompleted(
        contextProvider.getActivityExecutionContext().getWorkflowExecution().getWorkflowId());
  }

  /** @inheritDoc */
  @Override
  public void notifyJobFailed(Throwable e) {
    jobEventListener.onJobFailed(
        contextProvider.getActivityExecutionContext().getWorkflowExecution().getWorkflowId(), e);
  }

  /** @inheritDoc */
  @Override
  public void notifyJobStarted() {
    WorkflowExecution exec = contextProvider.getActivityExecutionContext().getWorkflowExecution();
    String ref =
        String.format(
            "swf:%s:%s:%s",
            contextProvider.getActivityExecutionContext().getDomain(),
            exec.getWorkflowId(),
            exec.getRunId());
    jobEventListener.onJobStarted(exec.getWorkflowId(), ref);
  }
}
