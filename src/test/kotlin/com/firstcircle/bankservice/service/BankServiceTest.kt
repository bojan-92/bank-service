package com.firstcircle.bankservice.service

import com.firstcircle.bankservice.exception.AccountNotFoundException
import com.firstcircle.bankservice.exception.InsufficientFundsException
import com.firstcircle.bankservice.exception.InvalidAmountException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import java.math.BigDecimal
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import kotlin.test.Test

class BankServiceTest {
    private lateinit var service: BankServiceImpl

    @BeforeEach
    fun setup() {
        service = BankServiceImpl()
    }

    @Test
    fun `create account should initialize with correct balance`() {
        val acc = service.createAccount("Alice", BigDecimal("100"), "EUR")
        assertEquals(BigDecimal("100.00"), service.getBalance(acc.id))
    }

    @Test
    fun `deposit should increase balance`() {
        val acc = service.createAccount("Bob", BigDecimal("200"), "EUR")
        service.deposit(acc.id, BigDecimal("50"))
        assertEquals(BigDecimal("250.00"), service.getBalance(acc.id))
    }

    @Test
    fun `withdraw should decrease balance`() {
        val acc = service.createAccount("Charlie", BigDecimal("300"), "EUR")
        service.withdraw(acc.id, BigDecimal("100"))
        assertEquals(BigDecimal("200.00"), service.getBalance(acc.id))
    }

    @Test
    fun `withdraw should throw when insufficient funds`() {
        val acc = service.createAccount("Dave", BigDecimal("50"), "EUR")
        assertThrows(InsufficientFundsException::class.java) {
            service.withdraw(acc.id, BigDecimal("100"))
        }
    }

    @Test
    fun `deposit should throw on invalid amount`() {
        val acc = service.createAccount("Eve", BigDecimal("50"), "EUR")
        assertThrows(InvalidAmountException::class.java) {
            service.deposit(acc.id, BigDecimal.ZERO)
        }
    }

    @Test
    fun `withdraw should throw on invalid amount`() {
        val acc = service.createAccount("Frank", BigDecimal("50"), "EUR")
        assertThrows(InvalidAmountException::class.java) {
            service.withdraw(acc.id, BigDecimal("-10"))
        }
    }

    @Test
    fun `transfer should move money correctly between accounts`() {
        val acc1 = service.createAccount("Alice", BigDecimal("500"), "EUR")
        val acc2 = service.createAccount("Bob", BigDecimal("100"), "EUR")

        service.transfer(acc1.id, acc2.id, BigDecimal("200"))

        assertEquals(BigDecimal("300.00"), service.getBalance(acc1.id))
        assertEquals(BigDecimal("300.00"), service.getBalance(acc2.id))
    }

    @Test
    fun `transfer should throw when insufficient funds`() {
        val acc1 = service.createAccount("Alice", BigDecimal("50"), "EUR")
        val acc2 = service.createAccount("Bob", BigDecimal("100"), "EUR")

        assertThrows(InsufficientFundsException::class.java) {
            service.transfer(acc1.id, acc2.id, BigDecimal("200"))
        }
    }

    @Test
    fun `transfer should throw when currencies differ`() {
        val acc1 = service.createAccount("Alice", BigDecimal("500"), "USD")
        val acc2 = service.createAccount("Bob", BigDecimal("500"), "EUR")

        assertThrows(IllegalArgumentException::class.java) {
            service.transfer(acc1.id, acc2.id, BigDecimal("50"))
        }
    }

    @Test
    fun `transfer should throw when transferring to same account`() {
        val acc = service.createAccount("Alice", BigDecimal("500"), "EUR")

        assertThrows(IllegalArgumentException::class.java) {
            service.transfer(acc.id, acc.id, BigDecimal("100"))
        }
    }

    @Test
    fun `should throw when account not found on deposit`() {
        assertThrows(AccountNotFoundException::class.java) {
            service.deposit(java.util.UUID.randomUUID(), BigDecimal("10"))
        }
    }

    @Test
    fun `should throw when account not found on withdraw`() {
        assertThrows(AccountNotFoundException::class.java) {
            service.withdraw(java.util.UUID.randomUUID(), BigDecimal("10"))
        }
    }

    @Test
    fun `should throw when account not found on transfer`() {
        val acc = service.createAccount("Alice", BigDecimal("500"), "EUR")

        assertThrows(AccountNotFoundException::class.java) {
            service.transfer(acc.id, java.util.UUID.randomUUID(), BigDecimal("10"))
        }
    }

    @Test
    fun `concurrent deposits must not lose money`() {
        val acc = service.createAccount("ConcurrentUser", BigDecimal("0"), "EUR")

        val threads = 50
        val depositsPerThread = 100
        val executor = Executors.newFixedThreadPool(threads)
        val latch = CountDownLatch(threads)

        repeat(threads) {
            executor.submit {
                repeat(depositsPerThread) {
                    service.deposit(acc.id, BigDecimal("1"))
                }
                latch.countDown()
            }
        }

        latch.await()
        executor.shutdown()

        assertEquals(BigDecimal("5000.00"), service.getBalance(acc.id))
    }

    @Test
    fun `concurrent withdraws must not lose money`() {
        val acc = service.createAccount("ConcurrentWithdrawUser", BigDecimal("5000"), "EUR")

        val threads = 50
        val withdrawPerThread = 100
        val executor = Executors.newFixedThreadPool(threads)
        val latch = CountDownLatch(threads)

        repeat(threads) {
            executor.submit {
                repeat(withdrawPerThread) {
                    service.withdraw(acc.id, BigDecimal("1"))
                }
                latch.countDown()
            }
        }

        latch.await()
        executor.shutdown()

        assertEquals(BigDecimal("0.00"), service.getBalance(acc.id))
    }

    @Test
    fun `concurrent transfers must not lose or create money`() {
        val from = service.createAccount("FromUser", BigDecimal("5000"), "EUR")
        val to = service.createAccount("ToUser", BigDecimal("0"), "EUR")

        val threads = 50
        val transfersPerThread = 100
        val executor = Executors.newFixedThreadPool(threads)
        val latch = CountDownLatch(threads)

        repeat(threads) {
            executor.submit {
                repeat(transfersPerThread) {
                    service.transfer(from.id, to.id, BigDecimal("1"))
                }
                latch.countDown()
            }
        }

        latch.await()
        executor.shutdown()

        assertEquals(BigDecimal("0.00"), service.getBalance(from.id))
        assertEquals(BigDecimal("5000.00"), service.getBalance(to.id))
    }

}