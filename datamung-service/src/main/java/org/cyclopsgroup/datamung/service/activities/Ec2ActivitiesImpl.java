package org.cyclopsgroup.datamung.service.activities;

import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.IamInstanceProfileSpecification;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.InstanceType;
import com.amazonaws.services.ec2.model.Reservation;
import com.amazonaws.services.ec2.model.RunInstancesRequest;
import com.amazonaws.services.ec2.model.RunInstancesResult;
import com.amazonaws.services.ec2.model.TerminateInstancesRequest;
import com.amazonaws.services.identitymanagement.AmazonIdentityManagement;
import com.amazonaws.services.identitymanagement.AmazonIdentityManagementClient;
import com.amazonaws.services.identitymanagement.model.AddRoleToInstanceProfileRequest;
import com.amazonaws.services.identitymanagement.model.CreateInstanceProfileRequest;
import com.amazonaws.services.identitymanagement.model.DeleteInstanceProfileRequest;
import com.amazonaws.services.identitymanagement.model.EntityAlreadyExistsException;
import com.amazonaws.services.identitymanagement.model.GetInstanceProfileRequest;
import com.amazonaws.services.identitymanagement.model.GetInstanceProfileResult;
import com.amazonaws.services.identitymanagement.model.NoSuchEntityException;
import com.amazonaws.services.identitymanagement.model.RemoveRoleFromInstanceProfileRequest;
import com.amazonaws.services.identitymanagement.model.Role;
import com.amazonaws.services.simpleworkflow.flow.ActivityExecutionContextProvider;
import com.amazonaws.services.simpleworkflow.flow.ActivityExecutionContextProviderImpl;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cyclopsgroup.datamung.api.types.Identity;
import org.cyclopsgroup.datamung.service.ServiceConfig;
import org.cyclopsgroup.datamung.swf.interfaces.Ec2Activities;
import org.cyclopsgroup.datamung.swf.types.CreateInstanceOptions;
import org.cyclopsgroup.datamung.swf.types.WorkerInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("workflow.Ec2Activities")
public class Ec2ActivitiesImpl implements Ec2Activities {
  private static final Log LOG = LogFactory.getLog(Ec2ActivitiesImpl.class);

  private final ActivityExecutionContextProvider contextProvider =
      new ActivityExecutionContextProviderImpl();

  @Autowired private ServiceConfig config;

  /** @inheritDoc */
  @Override
  public void createAgentInstanceProfile(
      String profileName, String controlRoleArn, Identity identity) {
    AmazonIdentityManagement iam =
        ActivityUtils.createClient(AmazonIdentityManagementClient.class, identity);

    // Create role if necessary
    String roleName = profileName + "-role";
    Map<String, String> policyVariables = new HashMap<String, String>();
    policyVariables.put("CONTROLLER_ROLE_ARN", controlRoleArn);
    Role role =
        ActivityUtils.createRole(
            roleName,
            iam,
            "datamung/agent-policy.json",
            policyVariables,
            "datamung/agent-trust.json",
            null);

    // Create instance profile and associate role if necessary
    boolean roleAssociationRequired = true;
    try {
      iam.createInstanceProfile(
          new CreateInstanceProfileRequest()
              .withInstanceProfileName(profileName)
              .withPath(role.getPath()));
    } catch (EntityAlreadyExistsException e) {
      LOG.info("Instance profile " + profileName + " already exists!");
      roleAssociationRequired =
          iam.getInstanceProfile(
                  new GetInstanceProfileRequest().withInstanceProfileName(profileName))
              .getInstanceProfile()
              .getRoles()
              .isEmpty();
    }
    if (roleAssociationRequired) {
      LOG.info("Adding role " + roleName + " to instance profile " + profileName);
      iam.addRoleToInstanceProfile(
          new AddRoleToInstanceProfileRequest()
              .withInstanceProfileName(profileName)
              .withRoleName(roleName));
    }
  }

  /** @inheritDoc */
  @Override
  public void deleteInstanceProfile(String profileName, Identity identity) {
    AmazonIdentityManagement iam =
        ActivityUtils.createClient(AmazonIdentityManagementClient.class, identity);

    String roleName = profileName + "-role";
    try {
      GetInstanceProfileResult profileResult =
          iam.getInstanceProfile(
              new GetInstanceProfileRequest().withInstanceProfileName(profileName));

      if (!profileResult.getInstanceProfile().getRoles().isEmpty()) {
        iam.removeRoleFromInstanceProfile(
            new RemoveRoleFromInstanceProfileRequest()
                .withInstanceProfileName(profileName)
                .withRoleName(roleName));
      }

      iam.deleteInstanceProfile(
          new DeleteInstanceProfileRequest().withInstanceProfileName(profileName));
    } catch (NoSuchEntityException e) {
      LOG.info("Instance profile is already gone: " + profileName);
    }
    ActivityUtils.deleteRole(roleName, iam);
  }

  /** @inheritDoc */
  @Override
  public WorkerInstance describeInstance(String instanceId, Identity identity) {
    AmazonEC2 ec2 = ActivityUtils.createClient(AmazonEC2Client.class, identity);
    DescribeInstancesResult result =
        ec2.describeInstances(new DescribeInstancesRequest().withInstanceIds(instanceId));
    if (result.getReservations().isEmpty()) {
      return null;
    }
    Reservation resv = result.getReservations().get(0);
    if (resv.getInstances().isEmpty()) {
      return null;
    }
    Instance ins = resv.getInstances().get(0);
    WorkerInstance wi = new WorkerInstance();
    wi.setInstanceId(ins.getInstanceId());
    wi.setInstanceStatus(ins.getState().getName());
    wi.setPublicIpAddress(ins.getPublicIpAddress());
    return wi;
  }

  /** @inheritDoc */
  @Override
  public String launchInstance(CreateInstanceOptions options, Identity identity) {
    RunInstancesRequest request = new RunInstancesRequest();
    if (options.getInstanceProfileName() != null) {
      request.setIamInstanceProfile(
          new IamInstanceProfileSpecification().withName(options.getInstanceProfileName()));
    }
    request.setSubnetId(options.getWorkerOptions().getSubnetId());
    request.setSecurityGroupIds(options.getWorkerOptions().getSecurityGroupIds());
    if (options.getUserData() != null) {
      request.setUserData(Base64.encodeBase64String(options.getUserData().getBytes()));
    }
    request
        .withMinCount(1)
        .withMaxCount(1)
        .withImageId(config.getAgentAmiId())
        .withInstanceType(InstanceType.T1Micro);
    request.setKeyName(options.getWorkerOptions().getKeyPairName());
    request.setClientToken(
        "launch-ec2-worker-"
            + contextProvider.getActivityExecutionContext().getWorkflowExecution().getWorkflowId());

    AmazonEC2 ec2 = ActivityUtils.createClient(AmazonEC2Client.class, identity);
    RunInstancesResult result = ec2.runInstances(request);
    return result.getReservation().getInstances().get(0).getInstanceId();
  }

  /** @inheritDoc */
  @Override
  public void terminateInstance(String instanceId, Identity identity) {
    AmazonEC2 ec2 = ActivityUtils.createClient(AmazonEC2Client.class, identity);
    ec2.terminateInstances(new TerminateInstancesRequest().withInstanceIds(instanceId));
  }
}
