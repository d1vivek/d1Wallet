package com.d1vivek.wallet.domain.model;

public enum TransactionType {
    DEPOSIT(Direction.CREDIT),
    TRANSFER_IN(Direction.CREDIT),
    REFUND(Direction.CREDIT),
    PAYMENT(Direction.DEBIT),
    TRANSFER_OUT(Direction.DEBIT),
    WITHDRAWAL(Direction.DEBIT),
    FEE(Direction.DEBIT);

    public enum Direction { CREDIT, DEBIT }

    private final Direction direction;

    TransactionType(Direction direction) {
        this.direction = direction;
    }

    public Direction getDirection() {
        return direction;
    }

    public boolean isCredit() {
        return direction == Direction.CREDIT;
    }
}
