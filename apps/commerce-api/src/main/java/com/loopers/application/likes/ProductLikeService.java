package com.loopers.application.likes;

import com.loopers.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductLikeService {

    private final UserRepository userRepository;

    public void add(Long userId, Long productId) {
        userRepository.findById(userId);
    }
}
