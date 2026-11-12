package com.quirogaotero.beans.ordering.application.port.out;

import com.quirogaotero.beans.ordering.domain.OrderPlaced;

/**
 * Where Ordering announces what happened. An event appended here is stored in the same
 * database transaction as the order it describes, and published to the broker afterwards.
 */
public interface OrderOutbox {

    void append(OrderPlaced event);

}
