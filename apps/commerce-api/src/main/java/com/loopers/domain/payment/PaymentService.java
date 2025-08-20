package com.loopers.domain.payment;

import com.loopers.domain.user.User;
import com.loopers.domain.user.UserRepository;
import com.loopers.domain.user.exception.UserException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;

    @Transactional
    public Payment create(PaymentCommand paymentCommand, PaymentStatus paymentStatus) {
        User user = userRepository.findByUserId(paymentCommand.getUserId())
                .orElseThrow(() -> new UserException.UserNotFoundException(ErrorType.USER_NOT_FOUND));

        Payment payment = Payment.create(
                paymentCommand.getFinalAmount()
                , paymentCommand.getOrderId()
                , user.getId()
                , paymentCommand.getPaymentMethod()
                , paymentStatus);
        return paymentRepository.save(payment);
    }
}
