package org.common.domain.repository;

import org.common.domain.Cat;
import org.common.domain.CatRating;
import org.common.domain.User;
import java.util.List;
import java.util.Optional;

public interface CatRatingRepository {
    Optional<CatRating> findByCatAndUser(Cat cat, User user);
    List<CatRating> listByCat(Cat cat);
    long countByCatAndLikeStatus(Cat cat, boolean likeStatus);
    CatRating save(CatRating rating);
    void deleteAll(Long catId);
} 