package com.tnmaster.application.service

import com.tnmaster.entities.Bank
import com.tnmaster.application.repositories.IBankRepo
import org.springframework.stereotype.Service

@Service
class BankService(private val bankRepo: IBankRepo) {
  fun fetchAllBanks(): List<Bank> {
    return bankRepo.findAll()
  }
}
