package org.cyclopsgroup.datamung.swf.flows;

import com.amazonaws.services.simpleworkflow.flow.DecisionContextProviderImpl;
import com.amazonaws.services.simpleworkflow.flow.annotations.Asynchronous;
import com.amazonaws.services.simpleworkflow.flow.core.Promise;
import org.cyclopsgroup.datamung.swf.interfaces.ControlActivitiesClient;
import org.cyclopsgroup.datamung.swf.interfaces.ControlActivitiesClientImpl;
import org.cyclopsgroup.datamung.swf.interfaces.NoOpWorkflow;

public class NoOpWorkflowImpl implements NoOpWorkflow {
  private final ControlActivitiesClient control = new ControlActivitiesClientImpl();

  /** @inheritDoc */
  @Override
  public void run() {
    System.out.println("Run called");
    run(0);
  }

  @Asynchronous
  private void run(int count, Promise<?>... waitFor) {
    if (count == 3) {
      return;
    }
    Promise<String> name =
        control.createDatabaseName(
            new DecisionContextProviderImpl()
                .getDecisionContext()
                .getWorkflowContext()
                .getWorkflowExecution()
                .getWorkflowId());
    scheduleNext(count, name);
  }

  @Asynchronous
  private void scheduleNext(int count, Promise<String> name) {
    Promise<Void> aSecond =
        new DecisionContextProviderImpl().getDecisionContext().getWorkflowClock().createTimer(1);
    run(count + 1, aSecond);
  }
}
