package com.loopers.domain.payment;

import lombok.Builder;
import lombok.Getter;

@Getter
public class TransactionInfo {
    private String transactionKey;
    private String orderNumber;
    private CardType cardType;
    private String cardNumber;
    private Long amount;
    private TransactionStatus status;

    @Builder
    private TransactionInfo(String transactionKey, String orderNumber, CardType cardType, String cardNumber, Long amount, TransactionStatus status) {
        this.transactionKey = transactionKey;
        this.orderNumber = orderNumber;
        this.cardType = cardType;
        this.cardNumber = cardNumber;
        this.amount = amount;
        this.status = status;
    }

    public static TransactionInfo of(String transactionKey, String orderNumber, CardType cardType, String cardNumber, Long amount, TransactionStatus status) {
        return TransactionInfo
                .builder()
                .transactionKey(transactionKey)
                .orderNumber(orderNumber)
                .cardType(cardType)
                .cardNumber(cardNumber)
                .amount(amount)
                .status(status)
                .build();
    }
}
