package com.firstcircle.bankservice.service

import com.firstcircle.bankservice.model.Account
import java.math.BigDecimal
import java.util.*

interface BankService {
    fun createAccount(
        ownerName: String,
        initDeposit: BigDecimal,
        currency: String
    ): Account

    fun deposit(accountId: UUID, amount: BigDecimal)
    fun withdraw(accountId: UUID, amount: BigDecimal)
    fun transfer(fromAccountId: UUID, toAccountId: UUID, amount: BigDecimal)
    fun getBalance(accountId: UUID): BigDecimal
}