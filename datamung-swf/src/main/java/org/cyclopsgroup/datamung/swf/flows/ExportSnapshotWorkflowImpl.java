package org.cyclopsgroup.datamung.swf.flows;

import com.amazonaws.services.simpleworkflow.flow.DecisionContextProvider;
import com.amazonaws.services.simpleworkflow.flow.DecisionContextProviderImpl;
import com.amazonaws.services.simpleworkflow.flow.annotations.Asynchronous;
import com.amazonaws.services.simpleworkflow.flow.core.Promise;
import com.amazonaws.services.simpleworkflow.flow.core.TryCatchFinally;
import org.cyclopsgroup.datamung.api.types.ExportSnapshotRequest;
import org.cyclopsgroup.datamung.swf.interfaces.CheckWaitWorkflowClientFactory;
import org.cyclopsgroup.datamung.swf.interfaces.CheckWaitWorkflowClientFactoryImpl;
import org.cyclopsgroup.datamung.swf.interfaces.ControlActivitiesClient;
import org.cyclopsgroup.datamung.swf.interfaces.ControlActivitiesClientImpl;
import org.cyclopsgroup.datamung.swf.interfaces.ExportSnapshotWorkflow;
import org.cyclopsgroup.datamung.swf.interfaces.JobWorkflowClientFactory;
import org.cyclopsgroup.datamung.swf.interfaces.JobWorkflowClientFactoryImpl;
import org.cyclopsgroup.datamung.swf.interfaces.RdsActivitiesClient;
import org.cyclopsgroup.datamung.swf.interfaces.RdsActivitiesClientImpl;
import org.cyclopsgroup.datamung.swf.types.CheckAndWait;
import org.cyclopsgroup.datamung.swf.types.DatabaseInstance;
import org.cyclopsgroup.datamung.swf.types.MySQLDumpJob;
import org.cyclopsgroup.datamung.swf.types.RunJobRequest;

public class ExportSnapshotWorkflowImpl implements ExportSnapshotWorkflow {
  private final DecisionContextProvider contextProvider = new DecisionContextProviderImpl();

  private final ControlActivitiesClient controlActivities = new ControlActivitiesClientImpl();

  private final JobWorkflowClientFactory jobFlowFactory = new JobWorkflowClientFactoryImpl();

  private final RdsActivitiesClient rdsActivities = new RdsActivitiesClientImpl();

  private ExportSnapshotRequest request;

  private final CheckWaitWorkflowClientFactory waitFlowFactory =
      new CheckWaitWorkflowClientFactoryImpl();

  private String workflowId;

  @Asynchronous
  private Promise<Void> dumpDatabase(Promise<DatabaseInstance> database) {
    MySQLDumpJob job = new MySQLDumpJob();
    job.setDataArchive(request.getDestinationArchive());
    job.setDatabaseInstance(database.get());
    job.setIdentity(request.getIdentity());
    job.setMasterPassword(request.getDatabaseMasterPassword());

    RunJobRequest runJob = new RunJobRequest();
    runJob.setJob(job);
    runJob.setIdentity(request.getIdentity());
    runJob.setWorkerOptions(request.getWorkerOptions());
    return jobFlowFactory.getClient(workflowId + "-job").executeCommand(runJob);
  }

  /** @inheritDoc */
  @Override
  public void export(final ExportSnapshotRequest request) {
    this.request = request;
    this.workflowId =
        contextProvider
            .getDecisionContext()
            .getWorkflowContext()
            .getWorkflowExecution()
            .getWorkflowId();
    Promise<Void> started = controlActivities.notifyJobStarted();

    final Promise<String> databaseName =
        controlActivities.createDatabaseName(request.getSnapshotName(), started);
    new TryCatchFinally(databaseName) {
      @Override
      protected void doCatch(Throwable cause) throws Throwable {
        controlActivities.notifyJobFailed(cause);
      }

      @Override
      protected void doFinally() {
        rdsActivities.terminateInstance(databaseName, Promise.asPromise(request.getIdentity()));
      }

      @Override
      protected void doTry() {
        Promise<DatabaseInstance> instance =
            rdsActivities.restoreSnapshot(
                Promise.asPromise(request.getSnapshotName()),
                databaseName,
                Promise.asPromise(request.getSubnetGroupName()),
                Promise.asPromise(request.getIdentity()));

        Promise<Void> sourceAvailable = waitUntilDatabaseAvailable(databaseName, instance);
        Promise<DatabaseInstance> source =
            rdsActivities.describeInstance(
                databaseName, Promise.asPromise(request.getIdentity()), sourceAvailable);
        Promise<Void> done = dumpDatabase(source);
        controlActivities.notifyJobCompleted(done);
      }
    };
  }

  @Asynchronous
  private Promise<Void> waitUntilDatabaseAvailable(
      Promise<String> databaseId, Promise<?>... waitFor) {
    CheckAndWait check = new CheckAndWait();
    check.setCheckType(CheckAndWait.Type.DATABASE_CREATION);
    // Hardcoded 1 hour wait for now
    check.setExpireOn(
        contextProvider.getDecisionContext().getWorkflowClock().currentTimeMillis()
            + request.getSnapshotRestoreTimeoutSeconds() * 1000L);
    check.setIdentity(request.getIdentity());
    check.setObjectName(databaseId.get());
    return waitFlowFactory.getClient(workflowId + "-restore-db").checkAndWait(check);
  }
}
