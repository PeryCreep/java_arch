package org.catService.domain.repository;

import org.catService.domain.Cat;
import org.catService.domain.CatRating;
import org.catService.domain.User;

import java.util.List;
import java.util.Optional;

public interface CatRatingRepository {

    Optional<CatRating> findByCatAndUser(Cat cat, User user);
    List<CatRating> listByCat(Cat cat);

    long countByCatAndLikeStatus(Cat cat, boolean likeStatus);

    CatRating save(CatRating rating);

    void deleteAll(Long catId);
}
