package com.quirogaotero.beans.messaging.application.port.out;

public interface MessagePublisher {

    /** Returns only once the broker has acknowledged the message; throws if it has not. */
    void publish(OutboxMessage message);

}
