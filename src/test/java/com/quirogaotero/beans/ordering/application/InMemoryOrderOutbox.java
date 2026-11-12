package com.quirogaotero.beans.ordering.application;

import com.quirogaotero.beans.ordering.application.port.out.OrderOutbox;
import com.quirogaotero.beans.ordering.domain.OrderPlaced;

import java.util.ArrayList;
import java.util.List;

class InMemoryOrderOutbox implements OrderOutbox {

    final List<OrderPlaced> events = new ArrayList<>();

    public void append(OrderPlaced event) { events.add(event); }

}
