package com.pknujsp.annotation

import com.google.auto.service.AutoService
import com.squareup.kotlinpoet.asTypeName
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.Filer
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.Processor
import javax.annotation.processing.RoundEnvironment
import javax.annotation.processing.SupportedSourceVersion
import javax.lang.model.SourceVersion
import javax.lang.model.element.TypeElement
import javax.lang.model.util.Elements
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.full.starProjectedType


@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.CLASS)
annotation class RequireDeepNavArgs


@SupportedSourceVersion(SourceVersion.RELEASE_11)
@AutoService(Processor::class)
class DeepLinkNavArgsProcessor : AbstractProcessor() {

    // availableTypes is a set of types of nav args that can be used in deeplink
    private val availableTypes = setOf(
        String::class.asTypeName().simpleName,
        Int::class.asTypeName().simpleName,
        Long::class.asTypeName().simpleName,
        Float::class.asTypeName().simpleName,
        Boolean::class.asTypeName().simpleName,
    )

    private lateinit var filer: Filer
    private lateinit var elementUtils: Elements

    override fun init(processingEnv: ProcessingEnvironment) {
        super.init(processingEnv)
        filer = processingEnv.filer
        elementUtils = processingEnv.elementUtils
    }

    override fun getSupportedAnnotationTypes(): Set<String> {
        return setOf(RequireDeepNavArgs::class.java.name)
    }

    override fun getSupportedSourceVersion(): SourceVersion {
        return SourceVersion.latestSupported()
    }

    override fun process(set: MutableSet<out TypeElement>, roundEnv: RoundEnvironment): Boolean {

        roundEnv.getElementsAnnotatedWith(RequireDeepNavArgs::class.java).forEach { element ->
            val className = element.simpleName.toString()
            val packageName = processingEnv.elementUtils.getPackageOf(element).toString()

            val dataClass = Class.forName("$packageName.$className").kotlin

            //check nullable and available types
            val constructor = dataClass.primaryConstructor?.typeParameters?.forEach { typeParameter ->
                if (typeParameter.starProjectedType.isMarkedNullable) {
                    throw IllegalArgumentException("$packageName.$className properties can't be nullable")
                } else if (typeParameter::class.simpleName !in availableTypes) {
                    throw IllegalArgumentException("$packageName.$className properties can't be passed to deeplink")
                }
            }
        }

        return true
    }
}

/*
Supported argument types
The Navigation library supports the following argument types:

Type	app:argType syntax	Support for default values	Handled by routes	Nullable
Integer	app:argType="integer"	Yes	Yes	No
Float	app:argType="float"	Yes	Yes	No
Long	app:argType="long"	Yes - Default values must always end with an 'L' suffix (e.g. "123L").	Yes	No
Boolean	app:argType="boolean"	Yes - "true" or "false"	Yes	No
String	app:argType="string"	Yes	Yes	Yes
Resource Reference	app:argType="reference"	Yes - Default values must be in the form of "@resourceType/resourceName" (e.g. "@style/myCustomStyle") or "0"	Yes	No
Custom Parcelable	app:argType="<type>", where <type> is the fully-qualified class name of the Parcelable	Supports a default value of "@null". Does not support other default values.	No	Yes
Custom Serializable	app:argType="<type>", where <type> is the fully-qualified class name of the Serializable	Supports a default value of "@null". Does not support other default values.	No	Yes
Custom Enum	app:argType="<type>", where <type> is the fully-qualified name of the enum	Yes - Default values must match the unqualified name (e.g. "SUCCESS" to match MyEnum.SUCCESS).	No	No
 */