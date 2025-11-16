package com.firstcircle.bankservice.model

import com.firstcircle.bankservice.exception.InsufficientFundsException
import com.firstcircle.bankservice.exception.InvalidAmountException
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.UUID

class Account(
    val id: UUID = UUID.randomUUID(),
    val ownerName: String,
    val currency: String,
    initBalance: BigDecimal
) {

    private var balance: BigDecimal = initBalance.scaled()

    init {
        require(ownerName.isNotBlank()) { "Owner name must not be blank" }
        require(initBalance >= BigDecimal.ZERO) { "Initial balance cannot be negative" }
    }

    fun deposit(amount: BigDecimal) {
        validateAmount(amount)
        balance = balance.add(amount.scaled())
    }

    fun withdraw(amount: BigDecimal) {
        validateAmount(amount)
        val scaledAmount = amount.scaled()
        if (balance < scaledAmount) {
            throw InsufficientFundsException(id, scaledAmount, balance)
        }
        balance = balance.subtract(scaledAmount)
    }

    fun currentBalance(): BigDecimal = balance

    private fun validateAmount(amount: BigDecimal?) {
        if (amount == null || amount <= BigDecimal.ZERO) {
            throw InvalidAmountException(amount)
        }
    }
}

private fun BigDecimal.scaled(): BigDecimal =
    this.setScale(2, RoundingMode.HALF_UP)