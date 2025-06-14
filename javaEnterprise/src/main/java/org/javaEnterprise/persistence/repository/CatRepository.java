package org.javaEnterprise.persistence.repository;

import org.javaEnterprise.persistence.entity.CatEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CatRepository extends JpaRepository<CatEntity, Long> {

    @Query("SELECT c FROM CatEntity c LEFT JOIN FETCH c.author ORDER BY FUNCTION('RAND') LIMIT 1")
    Optional<CatEntity> findRandomCat();

    @Query("SELECT c FROM CatEntity c WHERE c.author.id = :authorId ORDER BY c.createdAt DESC")
    Page<CatEntity> findByAuthorIdOrderByCreatedAtDesc(@Param("authorId") Long authorId, Pageable pageable);

    @Modifying
    @Query("DELETE FROM CatEntity c WHERE c.id = :catId AND c.author.id = :authorId")
    int deleteByIdAndAuthorId(@Param("catId") Long catId, @Param("authorId") Long authorId);
} 