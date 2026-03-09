package com.loopers.infrastructure.review;

import com.loopers.application.review.OrderVerifier;
import com.loopers.domain.review.exception.ReviewException;
import com.loopers.infrastructure.feign.order.OrderApiClient;
import com.loopers.infrastructure.feign.order.OrderApiDto;
import com.loopers.interfaces.api.ApiResponse;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderVerifierImpl implements OrderVerifier {

    private static final String COMPLETED_STATUS = "COMPLETED";

    private final OrderApiClient orderApiClient;

    @Override
    public void verifyPurchase(Long orderId, Long userId, Long productId) {
        ApiResponse<OrderApiDto.OrderResponse> response = orderApiClient.getOrder(orderId);
        OrderApiDto.OrderResponse order = response.data();

        if (!COMPLETED_STATUS.equals(order.orderStatus())) {
            throw new ReviewException.ReviewOrderNotCompletedException(ErrorType.REVIEW_ORDER_NOT_COMPLETED);
        }

        if (!userId.equals(order.userId())) {
            throw new ReviewException.ReviewOrderNotCompletedException(
                    ErrorType.REVIEW_ORDER_NOT_COMPLETED
            );
        }

        boolean hasProduct = order.orderLines().stream()
                .anyMatch(line -> productId.equals(line.productId()));
        if (!hasProduct) {
            throw new ReviewException.ReviewOrderNotCompletedException(
                    ErrorType.REVIEW_ORDER_NOT_COMPLETED
            );
        }
    }
}
