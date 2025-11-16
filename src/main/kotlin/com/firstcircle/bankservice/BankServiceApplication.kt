package com.firstcircle.bankservice

import com.firstcircle.bankservice.exception.InsufficientFundsException
import com.firstcircle.bankservice.exception.InvalidAmountException
import com.firstcircle.bankservice.service.BankService
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import java.math.BigDecimal
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors

@SpringBootApplication
class BankServiceApplication {

    @Bean
    fun demoRunner(bankService: BankService) = CommandLineRunner {

        println("========== BANK SERVICE DEMO ==========")

        // Create two accounts
        val alice = bankService.createAccount(
            ownerName = "Alice",
            initDeposit = BigDecimal("1000.00"),
            currency = "EUR"
        )
        val bob = bankService.createAccount(
            ownerName = "Bob",
            initDeposit = BigDecimal("200.00"),
            currency = "EUR"
        )

        println("\n-- After account creation --")
        println("Alice [${alice.id}]: ${bankService.getBalance(alice.id)} ${alice.currency}")
        println("Bob   [${bob.id}]: ${bankService.getBalance(bob.id)} ${bob.currency}")

        // Deposit into Alice
        println("\n-- Deposit 250.50 to Alice --")
        bankService.deposit(alice.id, BigDecimal("250.50"))
        println("Alice balance: ${bankService.getBalance(alice.id)}")

        // Withdraw from Bob
        println("\n-- Withdraw 50.00 from Bob --")
        bankService.withdraw(bob.id, BigDecimal("50.00"))
        println("Bob balance: ${bankService.getBalance(bob.id)}")

        // Transfer from Alice to Bob
        println("\n-- Transfer 300.00 from Alice to Bob --")
        bankService.transfer(alice.id, bob.id, BigDecimal("300.00"))
        println("Alice balance: ${bankService.getBalance(alice.id)}")
        println("Bob balance:   ${bankService.getBalance(bob.id)}")

        // Error scenarios
        println("\n-- Try invalid operations (expected errors) --")

        try {
            println("Trying to withdraw 10_000.00 from Bob")
            bankService.withdraw(bob.id, BigDecimal("10000.00"))
        } catch (ex: InsufficientFundsException) {
            println("Caught InsufficientFundsException: ${ex.message}")
        }

        try {
            println("Trying to deposit 0.00 to Alice")
            bankService.deposit(alice.id, BigDecimal.ZERO)
        } catch (ex: InvalidAmountException) {
            println("Caught InvalidAmountException: ${ex.message}")
        }

        // Concurrent transfers
        val concurrentAcc = bankService.createAccount(
            ownerName = "Concurrent",
            initDeposit = BigDecimal("0.00"),
            currency = "EUR"
        )

        val threads = 20
        val depositsPerThread = 100
        val depositAmount = BigDecimal("10")

        val executor = Executors.newFixedThreadPool(threads)
        val latch = CountDownLatch(threads)

        repeat(threads) {
            executor.submit {
                repeat(depositsPerThread) {
                    bankService.deposit(concurrentAcc.id, depositAmount)
                }
                latch.countDown()
            }
        }

        latch.await()
        executor.shutdown()

        val expectedDepositTotal = depositAmount * BigDecimal(threads * depositsPerThread)
        val finalDepositBalance = bankService.getBalance(concurrentAcc.id)

        println("Expected balance after concurrent deposits: $expectedDepositTotal")
        println("Actual balance: $finalDepositBalance")

        val from = bankService.createAccount(
            ownerName = "FromUser",
            initDeposit = BigDecimal("5000.00"),
            currency = "EUR"
        )
        val to = bankService.createAccount(
            ownerName = "ToUser",
            initDeposit = BigDecimal("0.00"),
            currency = "EUR"
        )

        val transferThreads = 20
        val transfersPerThread = 100
        val transferAmount = BigDecimal("1.00")

        val transferExecutor = Executors.newFixedThreadPool(transferThreads)
        val transferLatch = CountDownLatch(transferThreads)

        repeat(transferThreads) {
            transferExecutor.submit {
                repeat(transfersPerThread) {
                    bankService.transfer(from.id, to.id, transferAmount)
                }
                transferLatch.countDown()
            }
        }

        transferLatch.await()
        transferExecutor.shutdown()

        val fromBalance = bankService.getBalance(from.id)
        val toBalance = bankService.getBalance(to.id)
        val total = fromBalance + toBalance

        println("From balance: $fromBalance")
        println("To balance: $toBalance")
        println("Total system money: $total")

        println("\n========== DEMO FINISHED ==========")
    }
}

fun main(args: Array<String>) {
    runApplication<BankServiceApplication>(*args)
}
