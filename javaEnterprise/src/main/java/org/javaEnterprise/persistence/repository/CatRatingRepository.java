package org.javaEnterprise.persistence.repository;

import org.javaEnterprise.domain.Cat;
import org.javaEnterprise.domain.CatRating;
import org.javaEnterprise.domain.User;
import org.javaEnterprise.persistence.entity.CatRatingEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CatRatingRepository extends JpaRepository<CatRatingEntity, Long> {

    Optional<CatRatingEntity> findByCatAndUser(Cat cat, User user);

    @Query("SELECT COUNT(cr) FROM CatRatingEntity cr WHERE cr.cat = :cat AND cr.like = :likeStatus")
    long countByCatAndLikeStatus(@Param("cat") Cat cat, @Param("likeStatus") boolean likeStatus);
}