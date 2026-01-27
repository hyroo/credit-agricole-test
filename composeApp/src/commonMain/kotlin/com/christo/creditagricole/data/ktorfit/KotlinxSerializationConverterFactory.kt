package com.christo.creditagricole.data.ktorfit

import de.jensklingenberg.ktorfit.Ktorfit
import de.jensklingenberg.ktorfit.converter.Converter
import de.jensklingenberg.ktorfit.converter.KtorfitResult
import de.jensklingenberg.ktorfit.converter.TypeData
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import kotlin.reflect.KType

class KotlinxSerializationConverterFactory(
    private val json: Json
) : Converter.Factory {

    override fun suspendResponseConverter(
        typeData: TypeData,
        ktorfit: Ktorfit
    ): Converter.SuspendResponseConverter<HttpResponse, *>? {
        if (typeData.qualifiedName == Unit::class.qualifiedName) {
            return UnitSuspendConverter
        }

        val kType = typeData.typeInfo.kotlinType ?: return null
        val serializer = resolveSerializer(kType) ?: return null

        return KotlinxSuspendResponseConverter(
            json = json,
            serializer = serializer,
            isNullable = typeData.isNullable
        )
    }

    private fun resolveSerializer(kType: KType): KSerializer<Any?>? = try {
        @OptIn(ExperimentalSerializationApi::class)
        @Suppress("UNCHECKED_CAST")
        json.serializersModule.serializer(kType) as KSerializer<Any?>
    } catch (_: SerializationException) {
        null
    }
}

private object UnitSuspendConverter :
    Converter.SuspendResponseConverter<HttpResponse, Unit> {
    override suspend fun convert(result: KtorfitResult): Unit {
        when (result) {
            is KtorfitResult.Success -> {
                result.response.bodyAsText() // consume body
                return Unit
            }

            is KtorfitResult.Failure -> throw result.throwable
        }
    }
}

private class KotlinxSuspendResponseConverter<T>(
    private val json: Json,
    private val serializer: KSerializer<T>,
    private val isNullable: Boolean
) : Converter.SuspendResponseConverter<HttpResponse, T> {

    override suspend fun convert(result: KtorfitResult): T {
        val response = when (result) {
            is KtorfitResult.Success -> result.response
            is KtorfitResult.Failure -> throw result.throwable
        }

        val payload = response.bodyAsText()
        if (payload.isBlank()) {
            if (isNullable) {
                @Suppress("UNCHECKED_CAST")
                return null as T
            } else {
                throw SerializationException("Réponse vide alors qu'un corps JSON était attendu.")
            }
        }

        return json.decodeFromString(serializer, payload)
    }
}
