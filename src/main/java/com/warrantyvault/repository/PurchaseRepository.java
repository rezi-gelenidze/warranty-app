package com.warrantyvault.repository;

import com.warrantyvault.entity.Purchase;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PurchaseRepository extends JpaRepository<Purchase, Long> {

    // EXISTS instead of a join keeps one row per purchase when it has several tags
    @EntityGraph(attributePaths = "store")
    @Query("""
            select p from Purchase p
            where lower(p.name) like lower(concat('%', :query, '%'))
              and (:storeId is null or p.store.id = :storeId)
              and (:tagId is null or exists (
                    select 1 from Purchase p2 join p2.tags t
                    where p2 = p and t.id = :tagId))
            """)
    Page<Purchase> search(@Param("query") String query,
                          @Param("storeId") Long storeId,
                          @Param("tagId") Long tagId,
                          Pageable pageable);

    @Query("""
            select p from Purchase p
            where p.warrantyUntil between :from and :to
            order by p.warrantyUntil asc
            """)
    List<Purchase> findWarrantiesEndingBetween(@Param("from") LocalDate from,
                                               @Param("to") LocalDate to);

    @EntityGraph(attributePaths = {"store", "tags"})
    Optional<Purchase> findWithStoreAndTagsById(Long id);
}
