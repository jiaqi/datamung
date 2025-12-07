# Why Datamung

[Instroduction](..) | **Why** | [How](how.md) | [Run](run.md)

What Datamung does eventually is a mysqldump command. Shouldn't it be really simple? Before this project I was looking for something that allows me to backup my RDS MySQL instances with a click, and here are many challenges I ran into.

## Snapshot and limitation

RDS supports convenient snapshot functionality. User can take snapshot of an instance and restore from it with a few clicks via web UI. However the AWS feature comes with following limitations:

* RDS snapshot can't be exported into anything useful outside of AWS
* In AWS, RDS snapshot can't be shared across VPC or account .

To replicate or backup RDS instance outside of AWS, a region, VPC or account, user's option is to run mysqldump command.

Having said that, I noticed AWS announced a few features that allows RDS data to go across regions recently

* Snapshot copy across region on Oct 30th
* RDS cross region replica on Nov 14th in re:invent day 2 keynote

## Reliable mysqldump isn't simple

Running a mysqldump command itself is pretty straightforward, but running it with following requirements is not.

* The function should be as easy to use as the way AWS allows user to create snapshot of an RDS instance. Ideally, fill up a form on web and click a button. It shouldn't require user to run command in console or setup host even just for once.
* Since the database to backup is in AWS, the mysqldump result should be uploaded to S3, preferably encrypted.
* It's natural user doesn't want to run long mysqldump command against a hot database. If user chooses to, system should replicate the database first, run mysqldump command against static replication and terminate replication.
* Every step in process such as replication, mysqldump command, S3 upload, etc, should be retried upon failure with exponentially backoff. The backup process fails only if a step continuously fails for a number of times.
* A clean up process should be guaranteed no matter process succeeds or fails. For example, if system creates a replication of database and fails later on, it must have a technical guarantee in system to remove replication in the end.

## Datamung goals

Datamung attempts to provide a web-based, self-serviced platform that allows user to backup MySQL RDS instance with requirements above. Once user provides an AWS credential, with Datamung he can pick a database, fill in some options, press a button and wait for dump result to show up in his S3 bucket.

If you are interested, continue to read How it's done page and visit our demo website , which provide functions above with some limitations.

## The future

As you may see, at this point Datamung is only a prototype system with intention of concept proving. A lot of important features required to seriously and commercially use Datamung are not implemented yet. Here's a short list of things

* Security - The demo website is quite naive, where everyone's jobs are listed in a single page
* Choice of worker instance - The worker instance that runs mysqldump command is not very customizable. For large RDS instance, it can easily run out of disk space of a T1 micro instance.
* Visibility - Other than the detail job page in web UI, user doesn't have much visibility about what happens. It's difficult for user to troubleshoot his workflow and see logs.
* Restore a database - With what has been done in Datamung, it'd be reasonable to build another workflow that restore a mysqldump result in S3 back to an RDS instance. For limited time and resource, this feature is not implemented.