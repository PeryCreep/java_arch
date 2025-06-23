package org.catService.domain.repository;

import org.catService.domain.Cat;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface CatRepository {
    Cat save(Cat cat);
    List<Cat> findAll();
    Optional<Cat> findById(Long id);
    Optional<Cat> findRandomCat();
    Page<Cat> findByAuthorIdOrderByCreatedAtDesc(Long authorId, Pageable pageable);
    int deleteByIdAndAuthorId(Long catId, Long authorId);
    void deleteById(Long id);
} 