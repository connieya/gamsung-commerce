package com.loopers.infrastructure.order;

import com.loopers.domain.order.*;
import com.loopers.domain.order.exception.OrderException;
import com.loopers.support.error.ErrorType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

@Slf4j
@Component
public class OrderNoIssuerImpl implements OrderNoIssuer {

    private final SecureRandom secureRandom = new SecureRandom();
    private final String signatureSecret;
    private final IssuedOrderNoRepository issuedOrderNoRepository;

    public OrderNoIssuerImpl(
            @Value("${security.order.signature-secret:dev-order-secret}") String signatureSecret,
            IssuedOrderNoRepository issuedOrderNoRepository
    ) {
        this.signatureSecret = signatureSecret;
        this.issuedOrderNoRepository = issuedOrderNoRepository;
    }

    @Override
    public OrderNoIssue issue(boolean isNewOrderForm) {
        String orderNo = OrderNumberGenerator.generate();
        long timestamp = System.currentTimeMillis();
        String orderVerifyKey = String.format("%04d", secureRandom.nextInt(10000));
        String orderKey = randomHex(16);
        String signature = sign(orderNo, timestamp, orderKey, orderVerifyKey);

        OrderNoIssue issue = new OrderNoIssue(orderNo, signature, timestamp, orderVerifyKey, orderKey);

        // 발급 정보를 서버에 저장
        issuedOrderNoRepository.save(IssuedOrderNo.create(issue));
        log.info("[주문번호 발급] orderNo={}", orderNo);

        return issue;
    }

    @Override
    public void verify(String orderNo, String orderSignature, String orderKey) {
        IssuedOrderNo issued = issuedOrderNoRepository.findByOrderNo(orderNo)
                .orElseThrow(() -> new OrderException.OrderNoNotIssuedException(ErrorType.ORDER_NO_NOT_ISSUED));

        if (issued.isUsed()) {
            throw new OrderException.OrderNoAlreadyUsedException(ErrorType.ORDER_NO_ALREADY_USED);
        }

        // 서명 재생성 후 비교
        String expectedSignature = sign(issued.getOrderNo(), issued.getTimestamp(), issued.getOrderKey(), issued.getOrderVerifyKey());
        if (!expectedSignature.equals(orderSignature)) {
            log.warn("[주문 서명 검증 실패] orderNo={}", orderNo);
            throw new OrderException.OrderSignatureInvalidException(ErrorType.ORDER_SIGNATURE_INVALID);
        }

        // orderKey 검증
        if (!issued.getOrderKey().equals(orderKey)) {
            log.warn("[주문 키 검증 실패] orderNo={}", orderNo);
            throw new OrderException.OrderSignatureInvalidException(ErrorType.ORDER_SIGNATURE_INVALID);
        }

        // 사용 처리
        issued.markUsed();
        issuedOrderNoRepository.save(issued);
    }

    private String sign(String orderNo, long timestamp, String orderKey, String verifyKey) {
        try {
            String payload = orderNo + ":" + timestamp + ":" + orderKey + ":" + verifyKey;
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(signatureSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] raw = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(raw);
        } catch (Exception e) {
            throw new RuntimeException("주문 서명 생성에 실패했습니다.", e);
        }
    }

    private String randomHex(int bytes) {
        byte[] b = new byte[bytes];
        secureRandom.nextBytes(b);
        StringBuilder sb = new StringBuilder(bytes * 2);
        for (byte v : b) {
            sb.append(String.format("%02x", v));
        }
        return sb.toString();
    }
}

