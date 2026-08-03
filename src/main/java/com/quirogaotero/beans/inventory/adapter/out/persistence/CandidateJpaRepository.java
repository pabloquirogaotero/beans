package com.quirogaotero.beans.inventory.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Set;

public interface CandidateJpaRepository
        extends JpaRepository<LotStockJpaEntity, LotStockKey> {

  @Query(value = """
            SELECT  lo.id AS location_id,
                    lo.type AS location_type,
                    lo.latitude AS latitude,
                    lo.longitude AS longitude,
                    l.id AS lot_id,
                    l.sku_id AS sku_id,
                    l.code AS lot_code,
                    l.best_before AS best_before,
                    (ls.on_hand - ls.reserved) AS available
            FROM lot_stocks ls
            JOIN lots l ON l.id = ls.lot_id
            JOIN locations lo ON lo.id = ls.location_id
            WHERE l.sku_id IN (:skuIds)
              AND (ls.on_hand - ls.reserved) > 0
            """, nativeQuery = true)
  List<CandidateRow> findCandidateRows(@Param("skuIds") Set<String> skuIds);

}
