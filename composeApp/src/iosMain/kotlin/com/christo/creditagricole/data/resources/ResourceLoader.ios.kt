package com.christo.creditagricole.data.resources

import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSBundle
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.stringWithContentsOfFile

@OptIn(ExperimentalForeignApi::class)
internal actual fun loadResource(resourcePath: String): String {
    val fileName = resourcePath.substringAfterLast('/')
    val directory = resourcePath.substringBeforeLast('/', missingDelimiterValue = "")

    val name = fileName.substringBeforeLast('.')
    val extension = fileName.substringAfterLast('.', "")

    val bundlePath = if (directory.isEmpty()) {
        NSBundle.mainBundle.pathForResource(name, extension)
    } else {
        NSBundle.mainBundle.pathForResource(name, extension, directory)
    } ?: throw IllegalArgumentException("Resource $resourcePath not found.")

    return NSString.stringWithContentsOfFile(bundlePath, NSUTF8StringEncoding, null) as String
}
