package io.github.pknujsp.core.compiler

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import io.github.pknujsp.core.annotation.KBindFunc


class BindFuncKspProcessor(
  private val codeGenerator: CodeGenerator,
  private val logger: KSPLogger,
  private val options: Map<String, String>,
) : SymbolProcessor {

  private companion object {
    const val ANNOTATION_TYPE = "io.github.pknujsp.core.annotation.KBindFunc"
    const val PREFIX_OUTPUT_FILE_NAME = ANNOTATION_TYPE
  }

  override fun process(resolver: Resolver): List<KSAnnotated> {
    val declarations = resolver.getSymbolsWithAnnotation(KBindFunc::class.qualifiedName.toString()).toList()

    println("declarations: $declarations")


    return emptyList()
  }

}
