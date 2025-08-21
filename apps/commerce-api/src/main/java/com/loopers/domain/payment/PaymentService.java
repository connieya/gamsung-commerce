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
    public Payment create(PaymentCommand.Create paymentCommand, PaymentStatus paymentStatus) {
        User user = userRepository.findByUserId(paymentCommand.userId())
                .orElseThrow(() -> new UserException.UserNotFoundException(ErrorType.USER_NOT_FOUND));

        Payment payment = Payment.create(
                paymentCommand.finalAmount()
                , paymentCommand.orderId()
                , user.getId()
                , paymentCommand.paymentMethod()
                , paymentStatus);
        return paymentRepository.save(payment);
    }
}
