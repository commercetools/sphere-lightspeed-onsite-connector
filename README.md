SPHERE - Lightspeed OnSite Connector
==================================

This connector allows you to synchronize information between a SPHERE.IO project and a Lightspeed OnSite server.
In particular, the following synchronization is performed:

- Export SPHERE.IO products to Lightspeed OnSite (`sku`, `name`, `price` and the first image from the master variant).
- Export SPHERE.IO customers to Lightspeed OnSite (`firstName`, `lastName`, `email`).
- Import Lightspeed OnSite invoices to SPHERE.IO (`orderNumber`, `completedAt`, along with all line items and pricing information, as well as associated customer, if any).
- Updates `paymentStatus` of orders in SPHERE.IO.

#### Requirements

- [JDK 8](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)
- [JCE 8](http://www.oracle.com/technetwork/java/javase/downloads/jce8-download-2133166.html)
- [SBT](http://www.scala-sbt.org/download.html)
- [Lightspeed OnSite](http://www.lightspeedpos.com/onsite-help/first-time-installation/)
- A SPHERE.IO account with an existing project.
- A Lightspeed OnSite account, with an existing App with Read/Write permission to the installed server.

#### Configuration

The [configuration file](https://github.com/sphereio/sphere-lightspeed-onsite-connector/blob/master/src/main/resources/application.conf) of the project allows to set up information such as:
- Lightspeed client configuration.
- SPHERE.IO client configuration.
- Synchronization intervals.
- A timestamp limit to speed up synchronization, so that older documents are not processed.
- Store ID, to identify the point of sale where the orders took place.

You can also use environment variables to configure the connector. Notice that when environment variables are used, the values from the configuration file are overriden.

#### Run

To run it, just type `sbt run` in the project root folder.


