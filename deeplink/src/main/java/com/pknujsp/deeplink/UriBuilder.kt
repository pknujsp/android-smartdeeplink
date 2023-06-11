package com.pknujsp.deeplink

import android.net.Uri
import android.os.Bundle
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.navigation.NavArgs
import androidx.navigation.NavArgument
import androidx.navigation.NavController
import androidx.navigation.NavDeepLink
import androidx.navigation.NavDeepLinkRequest
import androidx.navigation.NavOptions
import androidx.navigation.NavType
import java.lang.ref.WeakReference

/**
 * Creates an HTTP GET Uri based on the DeepLinkUri to move and the parameters you pass in.
 *
 * @param deepLinkUrl "yourappname://search/result"
 * @param parameter mapOf("name" to "yourname", "age" to 5)
 *
 * @return Pair<Uri, Uri>
 *  - first: baseUri "yourappname://search/result?name={name}&age={age}"
 *  - second: queryUri "yourappname://search/result?name=yourName&age=5"
 */
@PublishedApi
internal fun toQueryUri(deepLinkUrl: String, parameter: Map<String, Any>): Pair<Uri, Uri> {
    val baseUriBuilder = WeakReference(StringBuilder("$deepLinkUrl?")).get()!!
    val queryUriBuilder = WeakReference(StringBuilder("$deepLinkUrl?")).get()!!

    parameter.onEachIndexed { index, entry ->
        baseUriBuilder.append("${entry.key}={${entry.key}}")
        queryUriBuilder.append("${entry.key}=${entry.value}")

        if (index != parameter.keys.size - 1) {
            baseUriBuilder.append('&')
            queryUriBuilder.append('&')
        }
    }

    val baseUri = baseUriBuilder.toString().toUri()
    val queryUri = queryUriBuilder.toString().toUri()

    return Pair(baseUri, queryUri)
}

/**
 * Creates an HTTP GET Uri based on the DeepLinkUri to move and the parameters you pass in.
 * This is the same as the toQueryUri function, but the parameter is not replaced with a value.
 *
 * @param deepLinkUrl "yourappname://search/result"
 * @param args mapOf("name" to "yourname", "age" to 5)
 * @param navOptions(optional) NavOptions.Builder().setPopUpTo(R.id.mainFragment, true).build()
 */
inline fun <reified Args : NavArgs> NavController.deepNavigate(
    deepLinkUrl: String, args: Args, navOptions: NavOptions? = null
) {
    val parameters = args.toMap()
    val (baseUri, finalUri) = toQueryUri(deepLinkUrl, parameters)

    graph.matchDeepLink(NavDeepLinkRequest(baseUri, null, null))?.apply {
        // Add new deeplink to destination if not exist in graph
        if (!destination.arguments.contains(DEEP_NAV_ARG_KEY)) {
            destination.addArgument(DEEP_NAV_ARG_KEY, DEEP_NAV_ARG)
            destination.addDeepLink(NavDeepLink.Builder().setUriPattern(baseUri.toString()).build())
        }
    }

    navigate(finalUri, navOptions)


}

/**
 * same role as by navArgs in Fragment
 *
 * Returns an empty object if there are no Arguments passed in the bundle
 * @return NavArgsLazy
 */
inline fun <reified Args : Any> Fragment.navArguments(): WapNavArgsLazy<Args> = WapNavArgsLazy(Args::class) {
    (arguments ?: Bundle()).apply {
        remove(DEEP_NAV_ARG_KEY)
    }
}

// used in DeepNavArgs to get the class name of the NavArgs
@PublishedApi
internal const val DEEP_NAV_ARGS_CLASS_NAME = "-*-*-*-*----*-__*-*"

// used in DeepNavArgs to get the unique key of the NavArgs for the new deeplink
@PublishedApi
internal const val DEEP_NAV_ARG_KEY = "**__****--*-*-*--**__"

// used in DeepNavArgs to get the unique value of the NavArgs for the new deeplink
@PublishedApi
internal val DEEP_NAV_ARG = NavArgument.Builder().setType(NavType.BoolType).setDefaultValue(true).build()