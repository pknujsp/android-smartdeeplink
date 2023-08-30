package io.github.pknujsp.annotationprocessor

import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.DelicateKotlinPoetApi
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.asTypeName
import org.jetbrains.annotations.Nullable
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.RoundEnvironment
import javax.annotation.processing.SupportedAnnotationTypes
import javax.lang.model.SourceVersion
import javax.lang.model.element.Element
import javax.lang.model.element.PackageElement
import javax.lang.model.element.TypeElement
import javax.tools.Diagnostic
import kotlin.reflect.KType

@SupportedAnnotationTypes("io.github.pknujsp.annotationprocessor.WapNavArgs")
internal class DeepLinkNavArgsProcessor : AbstractProcessor() {

  override fun getSupportedSourceVersion(): SourceVersion = SourceVersion.latestSupported()

  private companion object {

    // availableTypes is a set of types of nav args that can be used in deeplink
    private val availableTypes = setOf(
      String::class.java.name,
      Int::class.qualifiedName,
      Float::class.qualifiedName,
      Boolean::class.qualifiedName,
      Long::class.qualifiedName,
    )

    private const val newClassNameSuffix = "BindArgs"
  }

  @OptIn(DelicateKotlinPoetApi::class)
  override fun process(set: MutableSet<out TypeElement>, roundEnv: RoundEnvironment): Boolean {
    roundEnv.getElementsAnnotatedWith(WapNavArgs::class.java).filter {
      it.kind.isClass
    }.forEach { argsClass ->
      val fields = argsClass.enclosedElements.filter {
        it.kind.isField
      }

      fields.forEach { property ->
        if (property.annotationMirrors.any {
            it.annotationType.asTypeName() == Nullable::class.asTypeName()
          }) {
          // Nullable property found in data class
          processingEnv.messager.printMessage(
            Diagnostic.Kind.ERROR,
            "Nullable property found in data class: ${argsClass.simpleName}.${property.simpleName}",
          )

        } else if (property.asType().asTypeName().toString() !in availableTypes) {
          // Not supported type
          processingEnv.messager.printMessage(
            Diagnostic.Kind.ERROR,
            "Not supported type! -> ${property.asType().asTypeName()}, available types: $availableTypes",
          )

        }
      }

      // Create binding file
      /*
       if (canMoveToNextStep)
          createBindingFile(processingEnv.elementUtils.getPackageOf(argsClass), argsClass, fields)
       */
    }
    return true
  }


  private fun createBindingFile(packageElement: PackageElement, dataClassElement: Element, fields: List<Element>): Boolean {
    val baseClassName = ClassName(packageElement.qualifiedName.toString(), dataClassElement.simpleName.toString())
    val newClassName = ClassName(packageElement.qualifiedName.toString(), "${dataClassElement.simpleName}$newClassNameSuffix")

    val annotation = addAnnotation()
    val newClass = createPrimaryConstructor(fields, newClassName)
    val companionObject = createCompanion(baseClassName)
    val methods = createMethods()

    val newFileSpec = FileSpec.builder(newClassName).addImport("kotlin.reflect", "KClass", "KType").addImport(
      "kotlin.reflect.full", "memberProperties",
      "primaryConstructor", "starProjectedType",
    ).addType(
      newClass.addAnnotation(annotation).addType(companionObject).addFunction(methods).build(),
    ).build()

    newFileSpec.writeTo(processingEnv.filer)
    processingEnv.messager.printMessage(Diagnostic.Kind.WARNING, "Generated file: ${newFileSpec.name}")
    return true
  }

  private fun addAnnotation(): AnnotationSpec {
    return AnnotationSpec.builder(ClassName("kotlin", "Suppress")).addMember("%S", "UNCHECKED_CAST").build()
  }

  @OptIn(DelicateKotlinPoetApi::class)
  private fun createPrimaryConstructor(fields: List<Element>, newClassName: ClassName): TypeSpec.Builder {
    val constructorBuilder = FunSpec.constructorBuilder()
    fields.forEach { field ->
      constructorBuilder.addParameter(
        ParameterSpec.builder(
          field.simpleName.toString(),
          if (field.asType().asTypeName().toString() == String::class.java.name) String::class.asTypeName() else field.asType().asTypeName(),
        ).build(),
      )
    }

    val properties = fields.map { field ->
      PropertySpec.builder(
        field.simpleName.toString(),
        if (field.asType().asTypeName().toString() == String::class.java.name) String::class.asTypeName() else field.asType().asTypeName(),
      ).initializer(field.simpleName.toString()).build()
    }

    return TypeSpec.classBuilder(newClassName).addModifiers(KModifier.DATA).primaryConstructor(constructorBuilder.build()).addProperties(properties)
      .addSuperinterface(ClassName("androidx.navigation", "NavArgs"))
  }

  private fun createCompanion(baseClass: ClassName): TypeSpec {
    val returnClassSimpleName = baseClass.simpleName
    val returnClassFullName = baseClass.canonicalName

    // fromBundle ------------------------------------------------------
    val fromBundle =
      FunSpec.builder("fromBundle").addAnnotation(JvmStatic::class.java).addParameter("bundle", ClassName("android.os", "Bundle")).returns(baseClass)
        .addStatement(
          """
            val argsDataClass: KClass<$returnClassSimpleName> = Class.forName("$returnClassFullName").kotlin as
            KClass<$returnClassSimpleName>
            bundle.classLoader = argsDataClass.java.classLoader

            val constructor = argsDataClass.primaryConstructor!!
            val bundleKeySet = bundle.keySet()

            val properties = constructor.parameters.filter {
                it.name in bundleKeySet
            }.map { contructorProperty ->
                val realValueInBundle = bundle.get(contructorProperty.name)!!
                val realValueTypeInBundle = realValueInBundle::class.starProjectedType

                if (realValueTypeInBundle == contructorProperty.type) {
                    realValueInBundle
                } else {
                    // convert type of value in bundle to type of constructor property
                    convertType(contructorProperty.type, realValueInBundle)
                }
            }.toTypedArray()

            return constructor.call(*properties)
            """.trimIndent(),
        ).build()


    // empty -----------------------------------------------------------
    val empty = FunSpec.builder("empty").addAnnotation(JvmStatic::class.java).addModifiers(KModifier.PRIVATE).returns(baseClass).addStatement(
      """
            val dataClass: KClass<$returnClassSimpleName> = Class.forName("$returnClassFullName").kotlin as KClass<$returnClassSimpleName>
            val constructor = dataClass.primaryConstructor!!

            val args: List<Any> = constructor.parameters.map { constructorProperty ->
                defaultValues(constructorProperty.type)
            }
            return constructor.call(*args.toTypedArray())
            """.trimIndent(),
    ).build()


    // defaultValues ---------------------------------------------------
    val defaultValues =
      FunSpec.builder("defaultValues").addModifiers(KModifier.PRIVATE).addAnnotation(JvmStatic::class.java).addParameter("type", KType::class)
        .returns(Any::class).addStatement(
          """
            return when (type) {
                is KType -> {
                    when (type.classifier) {
                        String::class -> ""
                        Int::class -> 0
                        Float::class -> 0f
                        Boolean::class -> false
                        Long::class -> 0L
                        else -> throw IllegalArgumentException("Not supported type")
                    }
                }
                else -> throw IllegalArgumentException("Not supported type")
            }
            """.trimIndent(),
        ).build()


    // convertType -----------------------------------------------------
    val convertType =
      FunSpec.builder("convertType").addModifiers(KModifier.PRIVATE).addAnnotation(JvmStatic::class.java).addParameter("type", KType::class)
        .addParameter("value", Any::class).returns(Any::class).addStatement(
          """
            return when (type) {
                is KType -> {
                    when (type.classifier) {
                        String::class -> value.toString()
                        Int::class -> value.toString().toInt()
                        Float::class -> value.toString().toFloat()
                        Boolean::class -> value.toString().toBoolean()
                        Long::class -> value.toString().toLong()
                        else -> throw IllegalArgumentException("Not supported type")
                    }
                }
                else -> throw IllegalArgumentException("Not supported type")
            }
            """.trimIndent(),
        ).build()


    // companion object ------------------------------------------------
    return TypeSpec.companionObjectBuilder().addModifiers(KModifier.COMPANION).addFunction(fromBundle).addFunction(empty).addFunction(defaultValues)
      .addFunction(convertType).build()
  }

  @OptIn(DelicateKotlinPoetApi::class)
  private fun createMethods(): FunSpec {
    // toMap -----------------------------------------------------------
    val toMap = FunSpec.builder("toMap").returns(Map::class.asClassName().parameterizedBy(String::class.asClassName(), Any::class.asClassName()))
      .addAnnotation(AnnotationSpec.builder(PublishedApi::class.java).build()).addModifiers(KModifier.INTERNAL).addStatement(
        """
            return this::class.memberProperties.associate { property ->
            property.name to property.getter.call(this)!!
            }
            """.trimIndent(),
      ).build()

    return toMap
  }
}
