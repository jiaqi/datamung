package org.cyclopsgroup.datamung.api.types;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import org.cyclopsgroup.kaufman.interfaces.BaseComparableBean;

@XmlRootElement(name = "ExportHandler")
public class ExportHandler extends BaseComparableBean {
  public static ExportHandler of(String workflowId, String runId) {
    ExportHandler h = new ExportHandler();
    h.workflowId = workflowId;
    h.workflowRunId = runId;
    return h;
  }

  private String workflowId;

  private String workflowRunId;

  @XmlElement
  public String getWorkflowId() {
    return workflowId;
  }

  @XmlElement
  public String getWorkflowRunId() {
    return workflowRunId;
  }

  public void setWorkflowId(String workflowId) {
    this.workflowId = workflowId;
  }

  public void setWorkflowRunId(String workflowRunId) {
    this.workflowRunId = workflowRunId;
  }
}
