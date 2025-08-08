package com.tnmaster.service

import com.tnmaster.entities.Bank
import com.tnmaster.repositories.IBankRepo
import org.springframework.stereotype.Service

@Service
class BankService(private val bankRepo: IBankRepo) {
  fun fetchAllBanks(): List<Bank> {
    return bankRepo.findAll()
  }
}
