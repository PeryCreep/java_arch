package org.javaEnterprise.services;

import org.javaEnterprise.domain.Cat;
import org.javaEnterprise.domain.CatRating;
import org.javaEnterprise.domain.User;
import org.javaEnterprise.repository.CatRatingRepository;
import org.javaEnterprise.repository.CatRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class CatService {
    private final CatRepository catRepository;
    private final CatRatingRepository catRatingRepository;
    private final UserService userService;

    public CatService(CatRepository catRepository, CatRatingRepository catRatingRepository, UserService userService) {
        this.catRepository = catRepository;
        this.catRatingRepository = catRatingRepository;
        this.userService = userService;
    }

    @Transactional(readOnly = true)
    public Optional<Cat> getCatById(Long catId) {
        return catRepository.findById(catId);
    }

    @Transactional
    public Cat addCat(Long authorId, String name, byte[] photoData) {
        User author = userService.findByChatId(authorId)
                .orElseGet(() -> userService.createUser(authorId, "User" + authorId));

        Cat cat = new Cat();
        cat.setName(name);
        cat.setPhotoData(photoData);
        cat.setAuthor(author);
        return catRepository.save(cat);
    }

    @Transactional(readOnly = true)
    public Optional<Cat> getRandomCat() {
        return catRepository.findRandomCat();
    }

    @Transactional(readOnly = true)
    public Page<Cat> getCatsByAuthor(Long authorId, int page, int size) {
        return catRepository.findByAuthorIdOrderByCreatedAtDesc(authorId,
                PageRequest.of(page, size));
    }

    @Transactional
    public void deleteCat(Long catId, Long authorId) {
        Cat cat = catRepository.findById(catId)
                .orElseThrow(() -> new IllegalStateException("Кот не найден"));

        if (!cat.getAuthor().getId().equals(authorId)) {
            throw new SecurityException("Нет прав на удаление этого кота");
        }

        catRatingRepository.deleteAll(cat.getRatings());

        catRepository.delete(cat);
    }

    @Transactional(readOnly = true)
    public long getActualLikesCount(Cat cat) {
        return catRatingRepository.countByCatAndLikeStatus(cat, true);
    }

    @Transactional(readOnly = true)
    public long getActualDislikesCount(Cat cat) {
        return catRatingRepository.countByCatAndLikeStatus(cat, false);
    }

    @Transactional
    public boolean rateCat(Long catId, Long userId, boolean isLike) {
        Cat cat = catRepository.findById(catId)
                .orElseThrow(() -> new IllegalStateException("Кот не найден"));
        User user = userService.findByChatId(userId)
                .orElseGet(() -> userService.createUser(userId, "User" + userId));
        Optional<CatRating> existingRating = catRatingRepository.findByCatAndUser(cat, user);
        
        if (existingRating.isPresent()) {
            CatRating rating = existingRating.get();

            if (rating.isLike() == isLike) {
                return false;
            }

            if (isLike) {
                cat.decrementDislikes();
                cat.incrementLikes();
            } else {
                cat.decrementLikes();
                cat.incrementDislikes();
            }

            rating.setLike(isLike);
            catRatingRepository.save(rating);
        } else {
            CatRating rating = new CatRating(user, cat, isLike);
            catRatingRepository.save(rating);
            if (isLike) {
                cat.incrementLikes();
            } else {
                cat.incrementDislikes();
            }
        }
        
        catRepository.save(cat);
        return true;
    }
} 