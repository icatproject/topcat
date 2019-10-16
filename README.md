# Topcat Server

The server-side component of Topcat. It provides an API to manage user carts and downloads for multiple ICAT instances.

## Status

[![Build Status](https://travis-ci.org/icatproject/topcat.svg?branch=master)](https://travis-ci.org/icatproject/topcat)

## Installation

Information on how to install Topcat can be found here:

* https://repo.icatproject.org/site/topcat/2.4.5/installation.html

but see the installation notes within this project for installation of the server-only component.

## Development within a VM

You can create a Topcat development environment via Vagrant (https://www.vagrantup.com/):

```bash
git clone -b origin/issue#445-server-side-only https://github.com/icatproject/topcat.git
cd topcat
vagrant up
```

Once everything is up and running you'll need to make a security exception, which can be done by going to:

* [https://localhost:8181/topcat/ping](https://localhost:8181/topcat/ping)

You can rebuild the server side (i.e. anything Java related) by:

```bash
vagrant ssh
topcat build_install
```

You can build a distro by:

```bash
vagrant ssh
cd /vagrant
mvn clean install
```

your new build will be in:

* target/

## Native development

Go to where you have checked out topcat and do the usual
```
mvn clean install
```


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
