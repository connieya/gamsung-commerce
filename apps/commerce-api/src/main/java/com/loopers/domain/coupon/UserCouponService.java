package com.loopers.domain.coupon;

import com.loopers.domain.coupon.exception.CouponException;
import com.loopers.domain.user.User;
import com.loopers.domain.user.UserRepository;
import com.loopers.domain.user.exception.UserException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserCouponService {
    private final UserCouponRepository userCouponRepository;
    private final UserRepository userRepository;

    @Transactional
    public void use(UserCouponCommand userCouponCommand) {
        User user = userRepository.findByUserId(userCouponCommand.getUserId())
                .orElseThrow(() -> new UserException.UserNotFoundException(ErrorType.USER_NOT_FOUND));
        UserCoupon userCoupon = userCouponRepository.findByUserId(user.getId())
                .orElseThrow(() -> new CouponException.UserCouponNotFoundException(ErrorType.USER_COUPON_NOT_FOUND));

        userCoupon.use();
        userCouponRepository.updateUsedStatus(userCoupon.getId() ,userCoupon.isUsed());

    }
}
