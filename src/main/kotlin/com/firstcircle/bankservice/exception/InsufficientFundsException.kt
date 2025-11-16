package com.firstcircle.bankservice.exception

import java.math.BigDecimal
import java.util.*

class InsufficientFundsException(
    accountId: UUID,
    requested: BigDecimal,
    available: BigDecimal
) : RuntimeException(
    "Insufficient funds on account $accountId. Requested: $requested, available: $available"
)