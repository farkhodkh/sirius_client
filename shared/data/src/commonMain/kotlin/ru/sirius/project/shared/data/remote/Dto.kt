package ru.sirius.project.shared.data.remote

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonEncoder
import kotlinx.serialization.json.JsonUnquotedLiteral
import kotlinx.serialization.json.jsonPrimitive

/**
 * BigDecimal на backend передаётся JSON-числом (12.50). В клиенте деньги живут строкой, поэтому
 * читаем число как текст без потери точности и отправляем обратно числом, а не строкой.
 */
@OptIn(ExperimentalSerializationApi::class)
internal object DecimalAsString : KSerializer<String> {
    private val NUMBER = Regex("^-?\\d+(\\.\\d+)?$")

    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("DecimalAsString", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): String {
        val json = decoder as? JsonDecoder ?: throw SerializationException("DecimalAsString работает только с JSON")
        return json.decodeJsonElement().jsonPrimitive.content
    }

    override fun serialize(
        encoder: Encoder,
        value: String,
    ) {
        val json = encoder as? JsonEncoder ?: throw SerializationException("DecimalAsString работает только с JSON")
        if (!NUMBER.matches(value)) throw SerializationException("Не число: $value")
        json.encodeJsonElement(JsonUnquotedLiteral(value))
    }
}

@Serializable
internal data class ItemDto(
    val sku: String,
    val name: String,
    val quantity: Int,
    @Serializable(with = DecimalAsString::class) val unitPrice: String,
)

@Serializable
internal data class CreateOrderRequestDto(
    val currency: String,
    val items: List<ItemDto>,
)

@Serializable
internal data class OrderDto(
    val id: String,
    val status: String,
    @Serializable(with = DecimalAsString::class) val totalAmount: String,
    val currency: String,
    val createdAt: String,
    val items: List<ItemDto> = emptyList(),
)

/** Краткое представление из списка: без позиций. */
@Serializable
internal data class OrderSummaryDto(
    val id: String,
    val status: String,
    @Serializable(with = DecimalAsString::class) val totalAmount: String,
    val currency: String,
    val createdAt: String,
)

@Serializable
internal data class PageDto<T>(
    val content: List<T>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int,
)

/** Ошибка backend в формате problem+json (RFC 9457). Нужны только текстовые поля. */
@Serializable
internal data class ProblemDto(
    val title: String? = null,
    val detail: String? = null,
    val status: Int? = null,
)
