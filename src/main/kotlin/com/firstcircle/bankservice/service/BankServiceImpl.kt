package com.firstcircle.bankservice.service

import com.firstcircle.bankservice.exception.AccountNotFoundException
import com.firstcircle.bankservice.model.Account
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.util.*
import java.util.concurrent.ConcurrentHashMap

@Service
class BankServiceImpl : BankService {

    private val accounts: MutableMap<UUID, Account> = ConcurrentHashMap()

    override fun createAccount(
        ownerName: String,
        initDeposit: BigDecimal,
        currency: String
    ): Account {
        require(ownerName.isNotBlank()) { "Owner name must not be blank" }
        require(initDeposit >= BigDecimal.ZERO) { "Initial deposit must be greater than zero" }

        val account = Account(
            ownerName = ownerName,
            currency = currency,
            initBalance = initDeposit
        )

        accounts[account.id] = account
        return account
    }

    override fun deposit(accountId: UUID, amount: BigDecimal) {
        val account = getAccount(accountId)
        synchronized(account) {
            account.deposit(amount)
        }
    }

    override fun withdraw(accountId: UUID, amount: BigDecimal) {
        val account = getAccount(accountId)
        synchronized(account) {
            account.withdraw(amount)
        }
    }

    override fun transfer(fromAccountId: UUID, toAccountId: UUID, amount: BigDecimal) {
        require(fromAccountId != toAccountId) { "Cannot transfer to the same account" }

        val from = getAccount(fromAccountId)
        val to = getAccount(toAccountId)

        require(from.currency == to.currency) {
            "Invalid currency transfer requested (from=${from.currency}, to=${to.currency})"
        }

        val (first, second) =
            if (from.id < to.id) from to to else to to from

        synchronized(first) {
            synchronized(second) {
                from.withdraw(amount)
                to.deposit(amount)
            }
        }
    }

    override fun getBalance(accountId: UUID): BigDecimal {
        val account = getAccount(accountId)
        return account.currentBalance()
    }

    private fun getAccount(accountId: UUID): Account =
        accounts[accountId] ?: throw AccountNotFoundException(accountId)
}