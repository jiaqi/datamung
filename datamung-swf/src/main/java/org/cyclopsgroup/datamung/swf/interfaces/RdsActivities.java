package org.cyclopsgroup.datamung.swf.interfaces;

import com.amazonaws.services.simpleworkflow.flow.annotations.Activities;
import com.amazonaws.services.simpleworkflow.flow.annotations.ActivityRegistrationOptions;
import com.amazonaws.services.simpleworkflow.flow.annotations.ExponentialRetry;
import org.cyclopsgroup.datamung.api.types.Identity;
import org.cyclopsgroup.datamung.swf.types.DatabaseInstance;

@Activities(version = "1.0")
@ActivityRegistrationOptions(
    defaultTaskStartToCloseTimeoutSeconds = 600,
    defaultTaskScheduleToStartTimeoutSeconds = 600,
    defaultTaskList = Constants.ACTIVITY_TASK_LIST)
public interface RdsActivities {
  @Description("Create database snapshot $params.get(0) from RDS instance $params.get(1)")
  @ExponentialRetry(initialRetryIntervalSeconds = 30, maximumAttempts = 5)
  void createSnapshot(String snapshotName, String instanceName, Identity identity);

  @Description("Delete database snapshot $params.get(0)")
  @ExponentialRetry(initialRetryIntervalSeconds = 30, maximumAttempts = 5)
  void deleteSnapshot(String snapshotName, Identity identity);

  @Description(
      value = "Check status of RDS instance $params.get(0)",
      result = "$output.instanceStatus")
  @ExponentialRetry(initialRetryIntervalSeconds = 30, maximumAttempts = 5)
  DatabaseInstance describeInstance(String instanceName, Identity identity);

  @Description(value = "Check status of database snapshot $params.get(0)", result = "$output")
  @ExponentialRetry(initialRetryIntervalSeconds = 30, maximumAttempts = 5)
  String getSnapshotStatus(String snapshotName, Identity identity);

  @Description(
      "Restore RDS instance $params.get(1) from database snapshot $params.get(0) in subnet $params.get(2)")
  @ExponentialRetry(initialRetryIntervalSeconds = 30, maximumAttempts = 5)
  DatabaseInstance restoreSnapshot(
      String snapshotName, String instanceName, String subnetId, Identity identity);

  @Description("Terminate RDS instance $params.get(0)")
  @ExponentialRetry(initialRetryIntervalSeconds = 30, maximumAttempts = 5)
  void terminateInstance(String instanceName, Identity identity);
}
