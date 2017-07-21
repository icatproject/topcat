package org.icatproject.topcat;

import java.util.Map;
import java.util.HashMap;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import javax.enterprise.context.ApplicationScoped;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



@ServerEndpoint("/topcat/user/upload")
public class IdsUploadProxy {

    private Logger logger = LoggerFactory.getLogger(IdsUploadProxy.class);

    private Map<Session, Upload> uploads = new HashMap<Session, Upload>();

    public IdsUploadProxy() {
        logger.info("class loaded " + this.getClass());
    }

    @OnOpen
    public void open(Session session) {
        logger.info("open()");
    }

    @OnClose
    public void close(Session session) {
        logger.info("close()");
    }

    @OnError
    public void onError(Throwable error) {
        logger.info("onError()");
    }

    @OnMessage
    public void handleMessage(String message, Session session) {
        logger.info("onError(): " + message);
    }

    class Upload {

    }

}
