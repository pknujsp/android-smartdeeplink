package com.pknujsp.annotationprocessor

import com.pknujsp.annotation.RequireDeepNavArgs
import com.squareup.kotlinpoet.DelicateKotlinPoetApi
import com.squareup.kotlinpoet.asTypeName
import org.jetbrains.annotations.Nullable
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.RoundEnvironment
import javax.annotation.processing.SupportedAnnotationTypes
import javax.annotation.processing.SupportedSourceVersion
import javax.lang.model.SourceVersion
import javax.lang.model.element.TypeElement
import javax.tools.Diagnostic


@SupportedSourceVersion(SourceVersion.RELEASE_17)
@SupportedAnnotationTypes("com.pknujsp.annotation.RequireDeepNavArgs")
class DeepLinkNavArgsProcessor : AbstractProcessor() {

    private companion object {
        // availableTypes is a set of types of nav args that can be used in deeplink
        private val availableTypes = setOf(
            String::class.java.name,
            Int::class.qualifiedName,
            Float::class.qualifiedName,
            Boolean::class.qualifiedName,
            Long::class.qualifiedName,
        )

    }

    @OptIn(DelicateKotlinPoetApi::class)
    override fun process(set: MutableSet<out TypeElement>, roundEnv: RoundEnvironment): Boolean {
        roundEnv.getElementsAnnotatedWith(RequireDeepNavArgs::class.java).filter {
            it.kind.isClass
        }.forEach { element ->
            element.enclosedElements.filter {
                it.kind.isField
            }.forEach { field ->
                field.annotationMirrors.any {
                    it.annotationType.asTypeName() == Nullable::class.asTypeName()
                }.run {
                    if (this) {
                        processingEnv.messager.printMessage(
                            Diagnostic.Kind.ERROR,
                            "Nullable property found in data class: ${element.simpleName}.${field.simpleName}"
                        )
                    } else if (field.asType().asTypeName().toString() !in availableTypes) {
                        processingEnv.messager.printMessage(
                            Diagnostic.Kind.ERROR,
                            "Not supported type! -> ${field.asType().asTypeName()}, available types: $availableTypes"
                        )
                    }

                }
            }
        }
        return true
    }
}