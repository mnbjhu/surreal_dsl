package functions

import types.BooleanType
import types.ReturnType

object Is {
    fun alphaNum(value: ReturnType<*>) = BooleanType.createReference("is::alphanum(${value.reference})")
    fun alpha(value: ReturnType<*>) = BooleanType.createReference("is::alpha(${value.reference})")
    fun ascii(value: ReturnType<*>) = BooleanType.createReference("is::ascii(${value.reference})")
    fun domain(value: ReturnType<*>) = BooleanType.createReference("is::domain(${value.reference})")
    fun email(value: ReturnType<*>) = BooleanType.createReference("is::email(${value.reference})")
    fun hexadecimal(value: ReturnType<*>) = BooleanType.createReference("is::hexadecimal(${value.reference})")
    fun latitude(value: ReturnType<*>) = BooleanType.createReference("is::latitude(${value.reference})")
    fun longitude(value: ReturnType<*>) = BooleanType.createReference("is::longitude(${value.reference})")
    fun numeric(value: ReturnType<*>) = BooleanType.createReference("is::numeric(${value.reference})")
    fun semver(value: ReturnType<*>) = BooleanType.createReference("is::semver(${value.reference})")
    fun uuid(value: ReturnType<*>) = BooleanType.createReference("is::uuid(${value.reference})")
}