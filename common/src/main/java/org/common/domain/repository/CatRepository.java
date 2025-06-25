package org.common.domain.repository;

import org.common.domain.Cat;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;

public interface CatRepository {
    Cat save(Cat cat);
    Optional<Cat> findById(Long id);
    Optional<Cat> findRandomCat();
    void deleteById(Long id);
    Page<Cat> findByAuthorIdOrderByCreatedAtDesc(Long authorId, Pageable pageable);
} 