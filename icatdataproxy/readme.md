## ICAT Data Proxy

This is a CORS enabled web service to return data from the ICAT API in a format suitable for pagination.

## Installation

Prerequisites:

    node.js
    npm

Run:

    npm install

Start Service:

    npm start

or

    nodemon

Once started the service is available at [http://localhost:3000](http://localhost:3000) or [https://localhost:3001](http://localhost:3001). Read the main page for usage.

For development, it is recommended you install nodemon as it watched for file change so you don't have to restart the service every time you change a file. To install nodemon, use:

    npm install -g nodemon

If running icatdataproxy behind a web proxy, make you you have http_proxy set in your environment.

For development, self-signed certificates are used. If valid ssl certicates are required then name them as follow and copy them to the certs directory and then set the environment variable NODE_ENV as 'production':

    ssl private key: key.crt
    ssl root certificate: root.crt
    ssl intermediate cetificate: intermediate.crt

The real certs will be loaded automatically.

If you do want to have more control of the file path and name, you have to edit app.js directly.


