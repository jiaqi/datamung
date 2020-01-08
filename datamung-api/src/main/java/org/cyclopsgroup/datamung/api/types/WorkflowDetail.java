package org.cyclopsgroup.datamung.api.types;

import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "WorkflowDetail")
public class WorkflowDetail {
  private List<WorkflowActivity> history;

  private Workflow workflow;

  @XmlElementWrapper
  @XmlElement(name = "activity")
  public List<WorkflowActivity> getHistory() {
    return history;
  }

  @XmlElement
  public Workflow getWorkflow() {
    return workflow;
  }

  public void setHistory(List<WorkflowActivity> history) {
    this.history = history;
  }

  public void setWorkflow(Workflow workflow) {
    this.workflow = workflow;
  }
}
