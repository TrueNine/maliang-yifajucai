package com.tnmaster.application.config

import io.github.truenine.composeserver.data.extract.service.ILazyAddressService
import io.github.truenine.composeserver.data.extract.service.impl.LazyAddressCsvServiceImpl
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Component

@Component
class LazyCsvAddressApiConfiguration(private val lazyAddressService: ILazyAddressService) : ApplicationRunner {
  override fun run(args: ApplicationArguments?) {
    if (lazyAddressService is LazyAddressCsvServiceImpl) {
      lazyAddressService += "2010" to LazyAddressCsvServiceImpl.CsvDefine("area_code_2010.csv")
      lazyAddressService += "2018" to LazyAddressCsvServiceImpl.CsvDefine("area_code_2018.csv")
      lazyAddressService += "2019" to LazyAddressCsvServiceImpl.CsvDefine("area_code_2019.csv")
      lazyAddressService += "2020" to LazyAddressCsvServiceImpl.CsvDefine("area_code_2020.csv")
      lazyAddressService += "2021" to LazyAddressCsvServiceImpl.CsvDefine("area_code_2021.csv")
      lazyAddressService += "2022" to LazyAddressCsvServiceImpl.CsvDefine("area_code_2022.csv")
      lazyAddressService += "2023" to LazyAddressCsvServiceImpl.CsvDefine("area_code_2023.csv")
    }
  }
}
