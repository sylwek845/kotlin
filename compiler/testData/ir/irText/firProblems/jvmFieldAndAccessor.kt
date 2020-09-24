// FIR_IDENTICAL
// WITH_RUNTIME
class Some {
    @JvmField
    var foo: String? = null

    fun getFoo() = foo
}

@JvmField
val SOME_CONSTANT = "SOME_CONSTANT"