package io.github.pknujsp.core.compiler

import com.google.devtools.ksp.getDeclaredProperties
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.LambdaTypeName
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeVariableName
import com.squareup.kotlinpoet.UNIT
import io.github.pknujsp.core.annotation.KBindFunc


class BindFuncKspProcessor(
  private val codeGenerator: CodeGenerator,
  private val logger: KSPLogger,
  private val options: Map<String, String>,
) : SymbolProcessor {

  private companion object {
    val ANNOTATION_TYPE: String = KBindFunc::class.java.canonicalName
    val PREFIX_OUTPUT_FILE_NAME = ANNOTATION_TYPE
  }

  override fun process(resolver: Resolver): List<KSAnnotated> {
    val declarations = resolver.getSymbolsWithAnnotation(ANNOTATION_TYPE).filterIsInstance<KSClassDeclaration>().toList()

    val files = declarations.map { declaration ->
      logger.info("Declaration: ${declaration.qualifiedName!!.asString()}--------------------")
      val typeParams = declaration.typeParameters.mapTo(mutableListOf()) { parameter ->
        "$parameter, ${parameter.variance.name}, ${parameter.isReified}, ${parameter.bounds.map { it.element }.toList()}"
      }
      val properties = declaration.getDeclaredProperties().mapTo(mutableListOf()) { property ->
        "${property.simpleName.asString()} : ${property.type.resolve()}"
      }
      val impls = declaration.getSealedSubclasses().mapTo(mutableListOf()) { impl ->
        "${impl.simpleName.asString()}"
      }

      logger.info("TypeParams: $typeParams")
      logger.info("Property: $properties")
      logger.info("Impls: $impls")

      declaration
    }
    files.forEach { createBindingFile(it) }
    return declarations.toList()
  }


  private fun createBindingFile(declaration: KSClassDeclaration) {
    val removeImports = mutableSetOf<String>()
    val funcSpecs = declaration.getSealedSubclasses().map { createMethods(declaration, it, removeImports) }

    val newFileSpec = FileSpec.builder(declaration.packageName.asString(), "${PREFIX_OUTPUT_FILE_NAME}${declaration.simpleName.asString()}").apply {
      funcSpecs.forEach {
        addFunction(it)
      }
    }.build()

    try {
      codeGenerator.createNewFile(
        dependencies = Dependencies(false, declaration.containingFile!!),
        packageName = declaration.packageName.asString(),
        fileName = "${PREFIX_OUTPUT_FILE_NAME}_${declaration.simpleName.asString()}",
      ).bufferedWriter().use {
        it.write(
          newFileSpec.toString().run {
            removeUnnecessaryImports(this, removeImports)
          },
        )
      }
    } catch (e: Exception) {
    }
  }

  private fun removeUnnecessaryImports(content: String, imports: MutableSet<String>): String {
    var content = content
    imports.forEach {
      content = content.replace("import $it\n", "")
    }
    return content
  }

  private fun createMethods(parent: KSClassDeclaration, sub: KSClassDeclaration, removes: MutableSet<String>): FunSpec {
    print("${sub}--------------------------")

    val isGeneric = parent.typeParameters.isNotEmpty()
    val typeParameters = if (isGeneric) parent.typeParameters
    else emptyList()
    val properties = sub.getDeclaredProperties().toList()

    logger.info("fields: $properties")

    return FunSpec.builder("on${sub.simpleName.asString()}").run {
      addModifiers(KModifier.PUBLIC)
      addModifiers(KModifier.INLINE)
      receiver(
        ClassName.bestGuess(parent.qualifiedName!!.asString()).run {
          if (isGeneric) parameterizedBy(typeParameters.map { TypeVariableName(it.name.asString()) }.toList())
          else this
        },
      )
      addTypeVariables(
        typeParameters.map { ksTypeParameter ->
          TypeVariableName(
            ksTypeParameter.name.asString(),
            variance = KModifier.values().find { it.name == ksTypeParameter.variance.name.lowercase() },
          ).let { typeVariableName ->
            if (ksTypeParameter.bounds.toList().isNotEmpty() && ksTypeParameter.bounds.any { it.element != null }) {
              typeVariableName.copy(
                bounds = ksTypeParameter.bounds.toList().map {
                  TypeVariableName(it.toString())
                },
              )
            } else {
              typeVariableName
            }
          }
        }.toList(),
      )
      addParameter(
        ParameterSpec.builder(
          "block",
          LambdaTypeName.get(
            parameters = properties.map {
              ParameterSpec.unnamed(
                if (it.type.resolve().declaration.typeParameters.isNotEmpty()) {
                  TypeVariableName(it.type.resolve().declaration.typeParameters.first().name.asString())
                } else {
                  ClassName.bestGuess(
                    it.type.resolve().declaration.qualifiedName!!.asString().run {
                      if (contains(sub.simpleName.getShortName())) {
                        it.type.resolve().declaration.simpleName.getShortName().apply { removes.add(this) }
                      } else this
                    },
                  )
                },
              )

            }.toList(),
            returnType = UNIT,
          ),
        ).build(),
      )
      returns(
        ClassName.bestGuess(parent.qualifiedName!!.asString()).run {
          if (isGeneric) parameterizedBy(typeParameters.map { TypeVariableName(it.name.asString()) }.toList())
          else this
        },
      )
      addStatement("if (this is ${parent.simpleName.asString()}.${sub.simpleName.asString()})")

      val block = if (properties.isNotEmpty()) {
        properties.mapIndexed { i, v ->
          if (i < properties.size - 1) "${v.simpleName.asString()}," else v.simpleName.asString()
        }.joinToString("").run { "block(${this})" }
      } else {
        "block()"
      }

      addStatement(
        """
        |  $block
        """.trimMargin(),
      )
      addStatement("return this")
      build()
    }
  }

}

/**
 * @BindFunc
 * to interface UiState<out T> {
 *   data class Success<out T>(val data: T) : UiState<T>
 *   data class Error(val exception: Throwable) : UiState<Nothing>
 *   object Loading : UiState<Nothing>
 * }
 *
 * inline fun <T> UiState<T>.onSuccess(block: (T) -> Unit): UiState<T> {
 *   if (this is UiState.Success) block(data)
 *   return this
 * }
 *
 * inline fun <T> UiState<T>.onError(block: (Throwable) -> Unit): UiState<T> {
 *   if (this is UiState.Error) block(exception)
 *   return this
 * }
 *
 * inline fun <T> UiState<T>.onLoading(block: () -> Unit): UiState<T> {
 *   if (this is UiState.Loading) block()
 *   return this
 * }
 */
