package functions

import data.surrealJson
import kotlinx.serialization.builtins.serializer
import types.BooleanType
import types.LongType
import types.StringType
import types.array

object SurrealString {
    fun concat(first: StringType, second: StringType) = StringType.createReference("string::concat(${first.reference},${second.reference})")

    fun endsWith(first: StringType, second: StringType) = BooleanType.createReference("string::endsWith(${first.reference},${second.reference})")
    fun endsWith(first: String, second: StringType) = BooleanType.createReference("string::endsWith(${surrealJson.encodeToString(second.serializer, first)},${second.reference})")
    fun endsWith(first: StringType, second: String) = BooleanType.createReference("string::endsWith(${first.reference},${surrealJson.encodeToString(first.serializer, second)}")

    fun startsWith(first: StringType, second: StringType) = BooleanType.createReference("string::startsWith(${first.reference},${second.reference})")
    fun startsWith(first: String, second: StringType) = BooleanType.createReference("string::startsWith(${surrealJson.encodeToString(second.serializer, first)},${second.reference})")
    fun startsWith(first: StringType, second: String) = BooleanType.createReference("string::startsWith(${first.reference},${surrealJson.encodeToString(first.serializer, second)})")

    fun replace(text: StringType, searchText: String, replaceText: String) = StringType.createReference("string::replace(${text.reference},${surrealJson.encodeToString(String.serializer(), searchText)},${surrealJson.encodeToString(String.serializer(), replaceText)})")
    fun replace(text: StringType, searchText: StringType, replaceText: String) = StringType.createReference("string::replace(${text.reference},${searchText.reference},${surrealJson.encodeToString(String.serializer(), replaceText)})")
    fun replace(text: String, searchText: StringType, replaceText: String) = StringType.createReference("string::replace(${surrealJson.encodeToString(String.serializer(), text)},${searchText.reference},${surrealJson.encodeToString(String.serializer(), replaceText)})")
    fun replace(text: String, searchText: StringType, replaceText: StringType) = StringType.createReference("string::replace(${surrealJson.encodeToString(String.serializer(), text)},${searchText.reference},${replaceText.reference})")
    fun replace(text: String, searchText: String, replaceText: StringType) = StringType.createReference("string::replace(${surrealJson.encodeToString(String.serializer(), text)},${surrealJson.encodeToString(String.serializer(), searchText)},${replaceText.reference})")
    fun replace(text: StringType, searchText: String, replaceText: StringType) = StringType.createReference("string::replace(${text.reference},${surrealJson.encodeToString(String.serializer(), searchText)},${replaceText.reference})")
    fun replace(text: StringType, searchText: StringType, replaceText: StringType) = StringType.createReference("string::replace(${text.reference},${searchText.reference},${replaceText.reference})")

    fun join(deliminator: StringType, vararg items: StringType) = StringType.createReference("string::join(${deliminator.reference},${items.joinToString(","){ it.reference }})")

    fun length(value: StringType) = LongType.createReference("string::length(${value.reference})")

    fun lowercase(value: StringType) = StringType.createReference("string::lowercase(${value.reference})")

    fun uppercase(value: StringType) = StringType.createReference("string::uppercase(${value.reference})")

    fun repeat(value: StringType, count: LongType) = StringType.createReference("string::repeat(${value.reference},${count.reference})")
    fun repeat(value: String, count: LongType) = StringType.createReference("string::repeat(${surrealJson.encodeToString(String.serializer(),value)},${count.reference})")
    fun repeat(value: StringType, count: Long) = StringType.createReference("string::repeat(${value.reference},${surrealJson.encodeToString(Long.serializer(),count)})")

    fun reverse(value: StringType) = StringType.createReference("string::reverse(${value.reference})")

    fun slice(text: StringType, start: Long, length: Long) = StringType.createReference("string::slice(${text.reference},${surrealJson.encodeToString(Long.serializer(), start)},${surrealJson.encodeToString(Long.serializer(), length)})")
    fun slice(text: String, start: LongType, length: Long) = StringType.createReference("string::slice(${surrealJson.encodeToString(String.serializer(), text)},${start.reference},${surrealJson.encodeToString(Long.serializer(), length)})")
    fun slice(text: String, start: Long, length: LongType) = StringType.createReference("string::slice(${surrealJson.encodeToString(String.serializer(), text)},${surrealJson.encodeToString(Long.serializer(), start)},${length.reference})")
    fun slice(text: StringType, start: LongType, length: Long) = StringType.createReference("string::slice(${text.reference},${start.reference},${surrealJson.encodeToString(Long.serializer(), length)})")
    fun slice(text: String, start: LongType, length: LongType) = StringType.createReference("string::slice(${surrealJson.encodeToString(String.serializer(), text)},${start.reference},${length.reference})")
    fun slice(text: StringType, start: Long, length: LongType) = StringType.createReference("string::slice(${text.reference},${surrealJson.encodeToString(Long.serializer(), start)},${length.reference})")
    fun slice(text: StringType, start: LongType, length: LongType) = StringType.createReference("string::slice(${text.reference},${start.reference},${length.reference})")

    fun slug(value: StringType) = StringType.createReference("string::slug(${value.reference})")

    fun split(value: StringType, deliminator: StringType) = StringType.createReference("string::split(${value.reference},${deliminator.reference})")
    fun split(value: String, deliminator: StringType) = StringType.createReference("string::split(${surrealJson.encodeToString(String.serializer(), value)},${deliminator.reference})")
    fun split(value: StringType, deliminator: String) = StringType.createReference("string::split(${value.reference},${surrealJson.encodeToString(String.serializer(), deliminator)})")
    fun trim(value: StringType) = StringType.createReference("string::trim(${value.reference})")
    fun words(value: StringType) = array(StringType).createReference("string::words(${value.reference})")
}
