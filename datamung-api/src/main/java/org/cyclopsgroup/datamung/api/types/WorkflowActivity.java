package org.cyclopsgroup.datamung.api.types;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.cyclopsgroup.kaufman.interfaces.JodaDateTimeAdapter;
import org.joda.time.DateTime;

@XmlType
public class WorkflowActivity {
  @XmlEnum
  public static enum Status {
    @XmlEnumValue("CANCELED")
    CANCELED,
    @XmlEnumValue("COMPLETED")
    COMPLETED,
    @XmlEnumValue("FAILED")
    FAILED,
    @XmlEnumValue("OPEN")
    OPEN,
    @XmlEnumValue("RUNNING")
    RUNNING,
    @XmlEnumValue("TIMEOUT")
    TIMEOUT;
  }

  private String activityId;

  private String activityName;

  private Status activityStatus;

  private DateTime completeDate;

  private String errorDetail;

  private String errorReason;

  private String result;

  private DateTime startDate;

  private String title;

  @XmlElement
  public String getActivityId() {
    return activityId;
  }

  @XmlElement
  public String getActivityName() {
    return activityName;
  }

  @XmlElement
  public Status getActivityStatus() {
    return activityStatus;
  }

  @XmlElement
  @XmlJavaTypeAdapter(JodaDateTimeAdapter.class)
  public DateTime getCompleteDate() {
    return completeDate;
  }

  @XmlElement
  public String getErrorDetail() {
    return errorDetail;
  }

  @XmlElement
  public String getErrorReason() {
    return errorReason;
  }

  @XmlElement
  public String getResult() {
    return result;
  }

  @XmlElement
  @XmlJavaTypeAdapter(JodaDateTimeAdapter.class)
  public DateTime getStartDate() {
    return startDate;
  }

  @XmlElement
  public String getTitle() {
    return title;
  }

  public void setActivityId(String activityId) {
    this.activityId = activityId;
  }

  public void setActivityName(String activityName) {
    this.activityName = activityName;
  }

  public void setActivityStatus(Status activityStatus) {
    this.activityStatus = activityStatus;
  }

  public void setCompleteDate(DateTime completeDate) {
    this.completeDate = completeDate;
  }

  public void setErrorDetail(String errorDetail) {
    this.errorDetail = errorDetail;
  }

  public void setErrorReason(String errorReason) {
    this.errorReason = errorReason;
  }

  public void setResult(String result) {
    this.result = result;
  }

  public void setStartDate(DateTime startDate) {
    this.startDate = startDate;
  }

  public void setTitle(String title) {
    this.title = title;
  }
}
