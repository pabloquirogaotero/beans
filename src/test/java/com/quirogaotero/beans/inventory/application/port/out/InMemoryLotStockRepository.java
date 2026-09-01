package com.quirogaotero.beans.inventory.application.port.out;

import com.quirogaotero.beans.inventory.domain.LotStock;
import com.quirogaotero.beans.inventory.domain.LotStockId;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class InMemoryLotStockRepository implements LotStockRepository {

  private final Map<LotStockId, LotStock> store = new HashMap<>();

  public void seed(LotStock s) { store.put(s.getId(), s); }

  public Optional<LotStock> find(LotStockId id) { return Optional.ofNullable(store.get(id)); }

  public void save(LotStock s) { store.put(s.getId(), s); }

}
