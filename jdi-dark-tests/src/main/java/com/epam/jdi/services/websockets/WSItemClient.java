package com.epam.jdi.services.websockets;

import com.epam.jdi.dto.Item;
import com.epam.jdi.http.WebSocketGenericClient;

import javax.websocket.ClientEndpoint;

@ClientEndpoint(
        decoders = ItemDecoder.class,
        encoders = ItemEncoder.class
)
public class WSItemClient extends WebSocketGenericClient<Item> {
}