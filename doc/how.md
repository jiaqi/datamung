# How Datamung works

[Instroduction](..) | [Why](why.md) | **How** | [Run](run.md)

Datamung is built on AWS Simple Workflow , which provides strong guarantee that steps in predefined workflow is executed during time. When Datamung executes a workflow that backs up database, the workflow runs in Datamung's own AWS account, while the tasks that touch users resource such as replicating database, launching EC2 instance and uploading result to S3 run in user's AWS account with AWS credentials user provides.

Following drawing shows an overview of components in Datamung.

![Design overview](https://docs.google.com/drawings/d/e/2PACX-1vQuQPUl8lc7dfzJcsF7kbTlxkVDMG-_jdmDxsiJdHydEymvKET3HEdlbCxaI00z0CuR1m4UPrUDRA73/pub?w=960&h=720)

With layout above, Datamung runs one of predefined workflows to backup database. Following sections elaborate each of these workflows.

## Workflows

These are several important workflow definitions defined with Simple Workflow.

### Direct export data workflow

The workflow defines cross-account IAM role, launches EC2 instance in user's AWS account, which executes mysqldump command against given MySQL instance. After the work is done or fails, workflow terminates EC2 instance and delete cross-account IAM role. The source code of workflow definition is [here](https://github.com/jiaqi/datamung/blob/master/datamung-swf/src/main/java/org/cyclopsgroup/datamung/swf/flows/JobWorkflowImpl.java).

Since the workflow is driven by AWS Simple Workflow, every step is bound with timeout and retried up failure. The overall workflow execution is bound with timeout as well.

![Direct export workflow](https://docs.google.com/drawings/d/e/2PACX-1vSqurORZmXuxUpD6VUovLuqpH1aExmOzZdV5WvRHyMqRKgISrBS34amBgwxr1X5Ox1gAvcjDja9Hfab/pub?w=889&h=875)

## Convert snapshot workflow

This workflow converts an RDS snapshot into MySQL dump in S3, by restoring snapshot into an RDS instance, run direct export data workflow against it and terminate RDS instance. Source code is [here](https://github.com/jiaqi/datamung/blob/master/datamung-swf/src/main/java/org/cyclopsgroup/datamung/swf/flows/ExportSnapshotWorkflowImpl.java).

![Convert snapshot workflow](https://docs.google.com/drawings/d/e/2PACX-1vS6ipxIMk9K9zzvCbKCvEsiTonHDYCB40qYSkuUBJsK4u3jPt3RDlaOx-WbfQ3A73kW64zXkwy8LX1i/pub?w=613&h=554)

## Export instance workflow

The export instance workflow is the top level workflow, that either invokes direct export data workflow, or take snapshot of database instance to backup, run convert snapshot workflow and delete snapshot. The source code is [here](https://github.com/jiaqi/datamung/blob/master/datamung-swf/src/main/java/org/cyclopsgroup/datamung/swf/flows/ExportInstanceWorkflowImpl.java).

![Export instance workflow](https://docs.google.com/drawings/d/e/2PACX-1vSZ6AO-S3uxTiquVgPICH4RkgGxhfJkBequUdUd-1S1M4lSpJzHLsmYoA8XUxTbngUedWZIGF2cIfLj/pub?w=874&h=678)

