package com.quirogaotero.beans.inventory.application.port.out;

import com.quirogaotero.beans.inventory.domain.LotStock;
import com.quirogaotero.beans.inventory.domain.LotStockId;

import java.util.Optional;

public interface LotStockRepository {

  Optional<LotStock> find(LotStockId id);

  void save(LotStock lotStock);

}
