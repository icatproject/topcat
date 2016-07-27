# Topcat

A web based GUI able to search across multiple ICAT instances and download data via the IDS.

## Status

[![Build Status](https://travis-ci.org/icatproject/topcat.svg?branch=master)](https://travis-ci.org/icatproject/topcat)

[![Stories in Ready](https://badge.waffle.io/icatproject/topcat.png?label=ready&title=Ready)](https://waffle.io/icatproject/topcat)

## Installation

Information on how to install Topcat can be found here:

* https://repo.icatproject.org/site/topcat/

## Development

You can create a Topcat development environment via Vagrant (https://www.vagrantup.com/):

```bash
git clone https://github.com/icatproject/topcat.git
cd topcat
vagrant up
```

Once everything is up and running you'll need to make a security exception, which can be done by going to:

* [https://localhost:8181/topcat/ping](https://localhost:8181/topcat/ping)

 You'll then be able to run Topcat by going to:

* [http://localhost:10080/](http://localhost:10080/)

You will be able to log into the "TEST" facility with the following credentials:

* Authentication Type: Simple
* Username: root
* Password: root

You can edit Topcat's main configuration by editing:

* [yo/app/config/topcat_dev.json](https://github.com/icatproject/topcat/tree/master/yo/app/config/topcat_dev.json)

You can rebuild the server side (i.e. anything Java related) by:

```bash
vagrant ssh
topcat_build_install
```

You can build a distro by:

```bash
vagrant ssh
cd /vagrant
mvn clean install
```

your new build will be in:

* target/

## Licence

Copyright 2012-2015 The ICAT Collaboration

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

* http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
