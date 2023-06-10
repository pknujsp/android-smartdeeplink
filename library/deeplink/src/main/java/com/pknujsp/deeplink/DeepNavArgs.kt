package com.pknujsp.deeplink

import android.os.Bundle
import androidx.navigation.NavArgs
import com.pknujsp.annotation.RequireDeepNavArgs
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.full.starProjectedType

@Suppress("UNCHECKED_CAST")
@RequireDeepNavArgs
abstract class DeepNavArgs : NavArgs {

    companion object {

        @JvmStatic
        fun fromBundle(bundle: Bundle): DeepNavArgs {
            val dataClassName =
                bundle.getString(DEEP_NAV_ARGS_CLASS_NAME) ?: throw IllegalArgumentException("Not found class name in bundle")
            if (bundle.size() == 1) return empty(dataClassName)

            val argsDataClass: KClass<out DeepNavArgs> = Class.forName(dataClassName).kotlin as KClass<out DeepNavArgs>
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
        }

        @JvmStatic
        private fun empty(className: String): DeepNavArgs {
            val dataClass: KClass<DeepNavArgs> = Class.forName(className).kotlin as KClass<DeepNavArgs>
            val constructor = dataClass.primaryConstructor!!

            val args: List<Any> = constructor.parameters.map { constructorProperty ->
                defaultValues(constructorProperty.type)
            }
            return constructor.call(*args.toTypedArray())
        }

        @JvmStatic
        // NULL로 인한 오류를 막기 위해 기본값을 넣어준다.
        private fun defaultValues(type: KType) = when (type) {
            String::class.starProjectedType -> ""
            Int::class.starProjectedType -> 0
            Long::class.starProjectedType -> 0L
            Float::class.starProjectedType -> 0f
            Boolean::class.starProjectedType -> false
            else -> false
        }

        @JvmStatic
        private fun convertType(toType: KType, value: Any): Any = when (toType) {
            Int::class.starProjectedType -> value.toString().toInt()
            Long::class.starProjectedType -> value.toString().toLong()
            Float::class.starProjectedType -> value.toString().toFloat()
            Boolean::class.starProjectedType -> value.toString().toBoolean()
            String::class.starProjectedType -> value.toString()
            else -> value
        }
    }

    @PublishedApi
    internal fun toMap(): Map<String, Any> = this::class.memberProperties.associate { property ->
        property.name to property.getter.call(this)!!
    }
}