package org.javaEnterprise.services;

import org.javaEnterprise.domain.Cat;
import org.javaEnterprise.domain.CatRating;
import org.javaEnterprise.domain.User;
import org.javaEnterprise.domain.repository.CatRatingRepository;
import org.javaEnterprise.domain.repository.CatRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
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
        
        if (author.getId() == null) {
            author = userService.save(author);
        }

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

        catRatingRepository.deleteAll(cat.getId());

        catRepository.deleteById(cat.getId());
    }

    @Transactional
    public boolean rateCat(Long catId, Long userId, boolean isLike) {
        Cat cat = catRepository.findById(catId)
                .orElseThrow(() -> new IllegalStateException("Кот не найден"));
        User user = userService.findByChatId(userId)
                .orElseThrow(() -> new IllegalStateException("Неизвестный пользователь"));
        Optional<CatRating> existingRating = catRatingRepository.findByCatAndUser(cat, user);
        
        if (existingRating.isPresent()) {
            CatRating rating = existingRating.get();

            if (rating.isLike() == isLike) {
                return false;
            }

            rating.setLike(isLike);
            catRatingRepository.save(rating);
        } else {
            CatRating rating = new CatRating(user, cat, isLike);
            catRatingRepository.save(rating);
        }
        
        catRepository.save(cat);
        return true;
    }

    public long getLikeCount(Cat cat) {
        List<CatRating> existingRating = catRatingRepository.listByCat(cat);
        return existingRating.stream()
                .filter(CatRating::isLike)
                .count();
    }

    public long getDislikeCount(Cat cat) {
        List<CatRating> existingRating = catRatingRepository.listByCat(cat);
        return existingRating.stream()
                .filter(r -> !r.isLike())
                .count();
    }
} 