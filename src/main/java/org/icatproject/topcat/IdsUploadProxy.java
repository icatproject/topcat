package org.icatproject.topcat;

import java.util.Map;
import java.util.HashMap;

import java.io.*;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import javax.enterprise.context.ApplicationScoped;

import java.net.URL;
import java.net.URLEncoder;
import java.net.HttpURLConnection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@ApplicationScoped
@ServerEndpoint("/topcat/user/upload")
public class IdsUploadProxy {

    private static final Logger logger = LoggerFactory.getLogger(IdsUploadProxy.class);

    private Map<Session, IdsUploadProxy.Upload> uploads = new HashMap<Session, IdsUploadProxy.Upload>();

    @OnOpen
    public void open(Session session) {
        try {
            uploads.put(session, new IdsUploadProxy.Upload(session));
            logger.info("open()");
        } catch(Exception e){
            logger.error("open(): " + e.getMessage());
        }
    }

    @OnClose
    public void close(Session session) {
        try {
            uploads.remove(session);
            logger.info("close()");
        } catch(Exception e){
            logger.error("close(): " + e.getMessage());
        }
    }

    @OnError
    public void onError(Throwable error) {
        logger.error("onError(): " + error.getMessage());
    }

    @OnMessage
    public void handleMessage(String message, Session session) {
        try {
            uploads.get(session).write(message);
        } catch(Exception e){
            logger.error("handleMessage(): " + e.getMessage());
        }
    }

    public class Upload {

        private Session session;
        private HttpURLConnection connection;
        private DataOutputStream outputStream;
        private Long contentLength;
        private Long bytesWritten = 0L;

        public Upload(Session session) throws Exception {
            this.session = session;

            Map<String, String> queryStringParams = Utils.parseQueryString(session.getQueryString());

            StringBuilder url = new StringBuilder();
            url.append(queryStringParams.get("idsUrl") + "/ids/put");
            url.append("?sessionId=" + URLEncoder.encode(queryStringParams.get("sessionId"), "UTF-8"));
            url.append("&name=" + URLEncoder.encode(queryStringParams.get("name"), "UTF-8"));
            url.append("&datafileFormatId=" + URLEncoder.encode(queryStringParams.get("datafileFormatId"), "UTF-8"));
            url.append("&datasetId=" + URLEncoder.encode(queryStringParams.get("datasetId"), "UTF-8"));

            connection = (HttpURLConnection) (new URL(url.toString())).openConnection();
            connection.setRequestMethod("PUT");
            connection.setUseCaches(false);
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setChunkedStreamingMode(1000);
            contentLength = Long.parseLong(queryStringParams.get("contentLength"));
            connection.setRequestProperty("Content-Length", contentLength.toString());
            connection.setRequestProperty("Content-Type", "application/octet-stream");

            outputStream = new DataOutputStream(connection.getOutputStream());
        }

        public void write(String data) throws Exception {
            outputStream.writeBytes(data);
            outputStream.flush();
            bytesWritten += new Long(data.length());
            logger.info("bytesWritten: " + bytesWritten + " contentLength: " + contentLength);
            if(bytesWritten >= contentLength){
                outputStream.close();
                String responseBody = Utils.inputStreamToString(connection.getInputStream());
                session.getBasicRemote().sendText(responseBody);
            }
        }

    }

}
