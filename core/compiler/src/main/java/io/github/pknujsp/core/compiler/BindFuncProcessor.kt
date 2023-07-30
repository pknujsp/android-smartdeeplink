package io.github.pknujsp.core.compiler

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.DelicateKotlinPoetApi
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.LambdaTypeName
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeVariableName
import com.squareup.kotlinpoet.UNIT
import com.squareup.kotlinpoet.asTypeName
import com.squareup.kotlinpoet.metadata.KotlinPoetMetadataPreview
import io.github.pknujsp.core.annotation.BindFunc
import org.jetbrains.kotlin.builtins.jvm.JavaToKotlinClassMap
import org.jetbrains.kotlin.name.FqName
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.RoundEnvironment
import javax.annotation.processing.SupportedAnnotationTypes
import javax.lang.model.SourceVersion
import javax.lang.model.element.Element
import javax.lang.model.element.Modifier
import javax.lang.model.element.PackageElement
import javax.lang.model.element.TypeElement
import javax.tools.Diagnostic

@SupportedAnnotationTypes("io.github.pknujsp.core.annotation.BindFunc")
internal class BindFuncProcessor : AbstractProcessor() {

  override fun getSupportedSourceVersion(): SourceVersion = SourceVersion.latestSupported()

  private companion object {
    const val SuffixBindFunc = "BindFunc"
  }

  private data class BindFileEntity(
    val packageElement: PackageElement,
    val annotated: Element,
    val implementations: List<Element>,
  )

  private fun print(message: String) {
    processingEnv.messager.printMessage(Diagnostic.Kind.NOTE, message)
  }

  override fun process(set: MutableSet<out TypeElement>, roundEnv: RoundEnvironment): Boolean {

    val bindEntities = roundEnv.getElementsAnnotatedWith(BindFunc::class.java).filter {
      it.kind.isClass or it.kind.isInterface
    }.map { annotated ->
      val impls = annotated.enclosedElements.filterIsInstance<Element>().filter { it.kind.isClass or it.kind.isInterface }
      print("annotated: $annotated")
      impls.forEach { print("impl: $it") }
      BindFileEntity(processingEnv.elementUtils.getPackageOf(annotated), annotated, impls)
    }

    bindEntities.forEach { createBindingFile(it) }
    return true
  }


  private fun createBindingFile(bindFileEntity: BindFileEntity): Boolean {
    bindFileEntity.run {
      val annotatedName = ClassName(packageElement.qualifiedName.toString(), annotated.simpleName.toString())
      val bindFileName = ClassName(packageElement.qualifiedName.toString(), "${annotated.simpleName}$SuffixBindFunc")
      print("annotatedName: $annotatedName, bindFileName: $bindFileName")

      val funcSpecs = implementations.map { createMethods(annotated, it) }
      //print("funSpecs : $funcSpecs")

      val newFileSpec = FileSpec.builder(packageElement.qualifiedName.toString(), "${annotated.simpleName}$SuffixBindFunc").apply {
        funcSpecs.forEach {
          addFunction(it)
        }
      }.build()

      print("newFileSpec: $newFileSpec")
      newFileSpec.writeTo(processingEnv.filer)
    }
    return true
  }

  @OptIn(DelicateKotlinPoetApi::class, KotlinPoetMetadataPreview::class)
  private fun createMethods(annotated: Element, element: Element): FunSpec {
    print("${element}--------------------------")

    val annotatedType = annotated.asType()
    val implName = annotatedType.asTypeName().toString().split(".").last()
    val isGeneric = implName.contains("<")
    val typeVariableName = if (isGeneric) implName.substringAfter("<").substringBefore(">")
    else ""

    print("implName: ${annotated.javaToKotlinType()}")
    print("typeVariableName: $typeVariableName")
    print("enclosedElements: ${element.enclosedElements}")
    val fields = element.enclosedElements?.filter {
      it.kind.isField and (Modifier.STATIC !in it.modifiers)
    } ?: emptyList()
    print("fields: $fields")

    return FunSpec.builder("on${element.simpleName}").run {
      addModifiers(KModifier.INLINE)
      if (isGeneric) addTypeVariable(TypeVariableName(typeVariableName))
      receiver(
        ClassName.bestGuess(annotated.toString()).run {
          if (isGeneric) parameterizedBy(TypeVariableName(typeVariableName))
          else this
        },
      )
      addParameter(
        ParameterSpec.builder(
          "block",
          LambdaTypeName.get(
            parameters = fields.map {
              ParameterSpec.unnamed(
                it.javaToKotlinType(),
              )
            },
            returnType = UNIT,
          ),
        ).build(),
      )
      returns(annotatedType.asTypeName())
      addStatement("if (this is ${annotated.simpleName}.${element.simpleName})")
      addStatement(
        if (fields.isNotEmpty()) {
          fields.mapIndexed { i, v ->
            if (i < fields.size - 1) "$v," else v
          }.joinToString("").run { "{ block(${this}) }" }
        } else {
          "{ block() }"
        },
      )
      addStatement("return this")
      build()
    }
  }


  @OptIn(DelicateKotlinPoetApi::class)
  private fun Element.javaToKotlinType(): TypeName = asType().asTypeName().javaToKotlinType()

  private fun TypeName.javaToKotlinType(): TypeName {
    return if (this is ParameterizedTypeName) {
      (rawType.javaToKotlinType() as ClassName).parameterizedBy(
        *typeArguments.map { it.javaToKotlinType() }.toTypedArray(),
      )
    } else {
      val className = JavaToKotlinClassMap.mapJavaToKotlin(FqName(toString()))?.asSingleFqName()?.asString()

      return if (className == null) this
      else ClassName.bestGuess(className)
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
