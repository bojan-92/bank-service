package com.firstcircle.bankservice.exception

import java.math.BigDecimal

class InvalidAmountException(amount: BigDecimal?) :
    RuntimeException("Amount must be > 0. Provided: $amount")