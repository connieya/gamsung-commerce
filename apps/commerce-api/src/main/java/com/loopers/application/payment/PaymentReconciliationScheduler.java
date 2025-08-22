package com.loopers.application.payment;

import com.loopers.domain.payment.*;
import com.loopers.infrastructure.payment.client.PgSimulatorResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentReconciliationScheduler {

    private final PaymentService paymentService;
    private final PaymentAdapter paymentAdapter;

    @Scheduled(initialDelay = 300_000, fixedDelay = 600_000)
    public void reconcilePendingPayments() {

        // 너무 최근에 생성된 결제는 제외한다. (콜백이 올 시간을 충분히 주기 위함)
        // 예를 들어, 생성된 지 10분 이상 지났지만 여전히 PENDING인 결제만 조회
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(10);
        List<Payment> payments = paymentService.getPendingPayment(threshold);

        for (Payment payment : payments) {
            PaymentCommand.Search search = PaymentCommand.Search.of(payment.getOrderNumber(), payment.getOrderNumber());
            PgSimulatorResponse.TransactionDetail transactionDetail = paymentAdapter.getTransactionDetail(search);
            if (transactionDetail.transactionStatus() == TransactionStatus.SUCCESS) {
                paymentService.paid(payment.getId());
            }else if (transactionDetail.transactionStatus() == TransactionStatus.FAILED) {
                paymentService.fail(payment.getId());
            }
        }
    }
}
