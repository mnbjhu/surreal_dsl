package functions

import types.LongType
import types.StringType

object Parse {

    object Email {

        fun domain(value: StringType) = StringType.createReference("parse::email::domain(${value.reference})")
        fun user(value: StringType) = StringType.createReference("parse::email::user(${value.reference})")
    }

    object Url {
        fun domain(value: StringType) = StringType.createReference("parse::url::domain(${value.reference})")
        fun fragment(value: StringType) = StringType.createReference("parse::url::fragment(${value.reference})")
        fun host(value: StringType) = StringType.createReference("parse::url::host(${value.reference})")
        fun port(value: StringType) = LongType.createReference("parse::url::port(${value.reference})")
        fun query(value: StringType) = StringType.createReference("parse::url::query(${value.reference})")
    }
}