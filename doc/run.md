# Checkout and run Datamung

[Instroduction](..) | [Why](why.md) | [How](how.md) | **Run**

This page talks about Datamung project assuming the reader is not only interested in how to use Datamung, but also how to work with its source code, build bundle or run it in his own AWS account.

## Check out code

Datamung is a Java open source project hosted in Github and built with Maven . Once you check it out to your disk and have JDK and Maven available, run

```bash
cd ~/some_workspace
git clone https://github.com/jiaqi/datamung.git
cd datamung
mvn clean package install
```

A few bundles will be generated under each module.

If command fails because missing `aws-java-sdk-flow-build-tools` dependency, this is because the aws `build-tools` dependencies are not available in public Maven repository. Check [this article](https://repost.aws/forums?newRedirect=1&origin=/thread.jspa&threadID=109870) to find out how to install it locally.

## Run locally

Datamung RESTful service, agent and demo website are all web applications. Each of these modules are configured with jetty plugin. Under `datamung-service`, `datamung-agent` or `datamung-web`, a jetty command will start up the service.

In order to simplify how Datamung runs, a special module, datamung-bundle, is created to wrap both RESTful service and demo website as a single WAR bundle file.

```bash
cd datamung-bundle
mvn jetty:run
```

## Run in AWS

After being built, datamung is nothing but a war file that runs on any J2EE web container. The easiest way to run it in AWS is via AWS Beanstalk .

### Create Simple Workflow domain

Datamung heavily relies on AWS SimpleWorkflow. To create workflow domain for datamung, goto AWS web console, under Amazon Simple Workflow, create a domain named datamung-test . Make sure the default execution timeout is long enough for normal backup process. Use 8 hours execution timeout if you are not sure.

### Create IAM role

Datamung service will run on EC2 instance and access SWF and IAM service. In order to allow EC2 instance to access SWF and IAM, an instance profile is required. Under IAM function, create a new role with EC2 role type with following permission.

```json
{
"Statement": [
  {
    "Effect": "Allow",
    "Action": "swf:*",
    "Resource":"arn:aws:swf:*:<12 digits account Id>:/domain/datamung-test"
  },
  {
    "Effect": "Allow",
    "Action": "iam:*",
    "Resource":"*"
  }
]
}
```

Although the name of role can be arbitrary, let's assume it's `datamung-role`.

### Build war files from source code

Checkout datamung source code and build with maven.

```bash
$> mvn clean package install
...
[INFO] Reactor Summary:
[INFO]
[INFO] DataMung .......................................... SUCCESS [0.443s]
[INFO] DataMung API ...................................... SUCCESS [2.016s]
[INFO] DataMung Simple Workflow .......................... SUCCESS [9.704s]
[INFO] DataMung Service .................................. SUCCESS [0.776s]
[INFO] Datamung web UI ................................... SUCCESS [1.068s]
[INFO] DataMung command line agent ....................... SUCCESS [2.153s]
[INFO] DataMung War Bundle ............................... SUCCESS [1.553s]
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time: 52.534s
[INFO] Finished at: Mon Aug 12 21:09:17 CDT 2013
[INFO] Final Memory: 66M/402M
[INFO] ------------------------------------------------------------------------
$> find . -type f | grep "war$"
./datamung-agent/target/datamung-agent-1.0.0-SNAPSHOT.war
./datamung-bundle/target/datamung-bundle.war
```

### Create Beanstalk application and run datamung-service.war

Follow the beanstalk tutorial to create application and environment for datamung-bundle.war web application. A few things need attention:

* Make sure environment runs with instance profile `datamung-role` created earlier.
* Add JVM arguments: `-Dstage=beta`
* Health check path is `/ping`

