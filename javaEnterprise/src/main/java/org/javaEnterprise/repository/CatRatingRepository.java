package org.javaEnterprise.repository;

import org.javaEnterprise.domain.Cat;
import org.javaEnterprise.domain.CatRating;
import org.javaEnterprise.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CatRatingRepository extends JpaRepository<CatRating, Long> {

    Optional<CatRating> findByCatAndUser(Cat cat, User user);

    @Query("SELECT COUNT(cr) FROM CatRating cr WHERE cr.cat = :cat AND cr.like = :likeStatus")
    long countByCatAndLikeStatus(@Param("cat") Cat cat, @Param("likeStatus") boolean likeStatus);
} 