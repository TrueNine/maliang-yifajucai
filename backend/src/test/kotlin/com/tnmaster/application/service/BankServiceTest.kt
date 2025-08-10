package com.tnmaster.application.service

import com.tnmaster.entities.Bank
import com.tnmaster.repositories.IBankRepo
import com.tnmaster.service.BankService
import io.github.truenine.composeserver.enums.ISO4217
import io.github.truenine.composeserver.testtoolkit.RDBRollback
import io.github.truenine.composeserver.testtoolkit.testcontainers.IDatabasePostgresqlContainer
import io.github.truenine.composeserver.testtoolkit.testcontainers.IOssMinioContainer
import jakarta.annotation.Resource
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.Commit
import org.springframework.transaction.annotation.Transactional
import kotlin.test.assertTrue

@SpringBootTest
@RDBRollback
class BankServiceTest : IDatabasePostgresqlContainer, IOssMinioContainer {

  @Resource
  lateinit var bankService: BankService

  @Resource
  lateinit var bankRepo: IBankRepo

  @Test
  @Transactional
  fun `fetchAllBanks should return saved banks`() {
    // Given
    val b1 = bankRepo.save(
      Bank {
        groupType = "PUBLIC"
        bankName = "Test Bank A"
        region = ISO4217.CNY
      }
    )
    val b2 = bankRepo.save(
      Bank {
        groupType = "PUBLIC"
        bankName = "Test Bank B"
        region = ISO4217.USD
      }
    )

    // When
    val all = bankService.fetchAllBanks()

    // Then
    assertTrue(all.isNotEmpty(), "Should have at least some banks")
    assertTrue(all.any { it.bankName == "Test Bank A" }, "Should contain Test Bank A")
    assertTrue(all.any { it.bankName == "Test Bank B" }, "Should contain Test Bank B")
  }
}

