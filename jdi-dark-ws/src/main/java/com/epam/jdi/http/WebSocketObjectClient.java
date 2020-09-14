package com.epam.jdi.http;

import com.epam.http.logger.ILogger;
import org.glassfish.tyrus.client.ClientManager;

import javax.websocket.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static com.epam.http.logger.HTTPLogger.instance;

@ClientEndpoint
public class WebSocketObjectClient {
    private static final ILogger logger = instance("JDI_WS_Client");
    private Session session;
    private CountDownLatch latch;
    private final ClientManager clientManager = ClientManager.createClient();
    private Object lastMessage;
    private final List<Object> messages = new ArrayList<>();

    public void connect(String path)
            throws URISyntaxException, IOException, DeploymentException
    {
        logger.info("Connect to: " + path);
        session = clientManager.connectToServer(this, new URI(path));
    }

    public void close() throws IOException {
        logger.info("Close connection");
        session.close(new CloseReason(
                CloseReason.CloseCodes.GOING_AWAY, "Going away."
        ));
    }

    @OnOpen
    public void onOpen(Session session, EndpointConfig config) {
        logger.info("Connection is opened");
        this.session = session;
    }

    @OnMessage
    public void onMessage(Object message, Session session) {
        logger.info("Received message");
        lastMessage = message;
        messages.add(lastMessage);
        latch.countDown();
    }

    @OnClose
    public void onClose(Session session, CloseReason closeReason) {
        logger.info("Session closed with reason: " + closeReason.getReasonPhrase());
        clientManager.shutdown();
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        logger.error(throwable.getMessage());
    }

    public void sendPlainText(String message) throws IOException {
        session.getBasicRemote().sendText(message);
    }

    public void sendObject(Object object) throws IOException, EncodeException {
        logger.info("Send Object");
        session.getBasicRemote().sendObject(object);
    }

    public void sendBinary(ByteBuffer data) throws IOException {
        logger.info("Send Binary");
        session.getBasicRemote().sendBinary(data);
    }

    public boolean waitNewMessage(int millis) throws InterruptedException {
        latch = new CountDownLatch(1);
        return latch.await(millis, TimeUnit.MILLISECONDS);
    }

    public Object waitAndGetNewMessage(int millis) throws InterruptedException {
        waitNewMessage(millis);
        return lastMessage;
    }

    public List<Object> getMessages() {
        return messages;
    }
}
