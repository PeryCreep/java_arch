package org.javaEnterprise.domain.repository;

import org.javaEnterprise.domain.Cat;
import org.javaEnterprise.domain.CatRating;
import org.javaEnterprise.domain.User;

import java.util.List;
import java.util.Optional;

public interface CatRatingRepository {

    Optional<CatRating> findByCatAndUser(Cat cat, User user);
    List<CatRating> listByCat(Cat cat);

    long countByCatAndLikeStatus(Cat cat, boolean likeStatus);

    CatRating save(CatRating rating);

    void deleteAll(Long catId);
}
