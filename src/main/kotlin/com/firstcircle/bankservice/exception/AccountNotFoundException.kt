package com.firstcircle.bankservice.exception

import java.util.*

class AccountNotFoundException(accountId: UUID) :
    RuntimeException("Account with id: $accountId not found")