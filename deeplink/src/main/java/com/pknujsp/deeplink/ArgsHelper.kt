package com.pknujsp.deeplink

import android.annotation.SuppressLint
import android.os.Bundle
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.full.starProjectedType


@PublishedApi
@SuppressLint("BanUncheckedReflection") // needed for method.invoke
internal fun <Args : DeepArgs> Args.toMap(): Map<String, Any> = this::class.memberProperties.associate { property ->
    property.name to property.getter.call(this)!!
}

class WapNavArgsLazy<Args : DeepArgs>(
    private val navArgsClass: KClass<Args>,
    private val argumentProducer: () -> Bundle
) : Lazy<Args> {

    private var cached: Args? = null

    override val value: Args
        get() {
            var args = cached
            if (args == null) {
                val arguments = argumentProducer()
                args = fromBundle(navArgsClass.qualifiedName!!, arguments)
                cached = args
            }
            return args
        }

    override fun isInitialized(): Boolean = cached != null

    @PublishedApi
    internal fun Args.toMap(): Map<String, Any> = this::class.memberProperties.associate { property ->
        property.name to property.getter.call(this)!!
    }

    @Suppress("UNCHECKED_CAST")
    @PublishedApi
    internal fun fromBundle(className: String, bundle: Bundle): Args {
        val argsDataClass: KClass<Args> =
            Class.forName(className).kotlin as KClass<Args>
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

    @Suppress("UNCHECKED_CAST")
    @PublishedApi
    internal inline fun <reified Args : DeepArgs> Args.empty(): Args {
        val dataClass: KClass<Args> =
            Class.forName(Args::class.java.name).kotlin as KClass<Args>
        val constructor = dataClass.primaryConstructor!!

        val args: List<Any> = constructor.parameters.map { constructorProperty ->
            defaultValue(constructorProperty.type)
        }
        return constructor.call(*args.toTypedArray())
    }

    @PublishedApi
    internal fun defaultValue(type: KType): Any = when (type) {
        String::class.starProjectedType -> ""
        Int::class.starProjectedType -> 0
        Float::class.starProjectedType -> 0f
        Boolean::class.starProjectedType -> false
        Long::class.starProjectedType -> 0L
        else -> throw IllegalArgumentException("Not supported type")
    }

    @PublishedApi
    internal fun convertType(type: KType, `value`: Any): Any = when (type) {
        String::class.starProjectedType -> value.toString()
        Int::class.starProjectedType -> value.toString().toInt()
        Float::class.starProjectedType -> value.toString().toFloat()
        Boolean::class.starProjectedType -> value.toString().toBoolean()
        Long::class.starProjectedType -> value.toString().toLong()
        else -> throw IllegalArgumentException("Not supported type")
    }
}