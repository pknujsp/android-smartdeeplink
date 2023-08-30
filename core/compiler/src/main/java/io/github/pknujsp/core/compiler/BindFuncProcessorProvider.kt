package io.github.pknujsp.core.compiler

import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider

class BindFuncProcessorProvider : SymbolProcessorProvider {
  override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor = BindFuncKspProcessor(
    codeGenerator = environment.codeGenerator,
    logger = environment.logger,
    options = environment.options,
  )
}
