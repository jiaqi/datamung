package org.cyclopsgroup.datamung.api.types;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import org.cyclopsgroup.kaufman.interfaces.BaseComparableBean;

@XmlRootElement(name = "WorkflowList")
public class WorkflowList extends BaseComparableBean implements Iterable<Workflow> {
  public static WorkflowList of(List<Workflow> workflows) {
    WorkflowList list = new WorkflowList();
    list.list = workflows;
    return list;
  }

  private List<Workflow> list;

  @XmlElement(name = "workflow")
  public List<Workflow> getList() {
    return list;
  }

  /** @return */
  @Override
  public Iterator<Workflow> iterator() {
    return list == null ? Collections.<Workflow>emptyList().iterator() : list.iterator();
  }

  public void setList(List<Workflow> list) {
    this.list = list;
  }
}
