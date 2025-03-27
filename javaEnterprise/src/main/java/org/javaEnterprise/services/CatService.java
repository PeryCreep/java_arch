package org.javaEnterprise.services;

import org.javaEnterprise.domain.Cat;
import org.javaEnterprise.domain.User;
import org.javaEnterprise.repository.CatRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class CatService {
    private final CatRepository catRepository;
    private final UserService userService;

    public CatService(CatRepository catRepository, UserService userService) {
        this.catRepository = catRepository;
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
        int deletedCount = catRepository.deleteByIdAndAuthorId(catId, authorId);
        if (deletedCount == 0) {
            throw new SecurityException("Cat not found or access denied");
        }
    }

    @Transactional
    public void incrementLikes(Long catId) {
        Cat cat = catRepository.findById(catId)
                .orElseThrow(() -> new IllegalStateException("Кот не найден"));
        cat.setLikesCount(cat.getLikesCount() + 1);
        catRepository.save(cat);
    }

    @Transactional
    public void incrementDislikes(Long catId) {
        Cat cat = catRepository.findById(catId)
                .orElseThrow(() -> new IllegalStateException("Кот не найден"));
        cat.setDislikesCount(cat.getDislikesCount() + 1);
        catRepository.save(cat);
    }
} 