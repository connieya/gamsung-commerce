package com.loopers.domain.activity;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class ActivityService {

    private final ViewProductRepository viewProductRepository;

    @Transactional
    public Long view(ActivityCommand.View command) {
        ViewProduct viewProduct = viewProductRepository.findByProductId(command.getProductId())
                .orElseGet(() -> ViewProduct.create(command.getProductId(), 0L));

        viewProduct.view();

        viewProductRepository.save(viewProduct);

        return viewProduct.getViewCount();
    }

}


