// IGNORE_BACKEND: JS_IR
// IGNORE_BACKEND_FIR: JVM_IR
// !LANGUAGE: +NewInference
// !DIAGNOSTICS: -EXPERIMENTAL_API_USAGE_ERROR -UNUSED_EXPRESSION
// WITH_RUNTIME

@OptIn(ExperimentalStdlibApi::class)
fun test(s: String?): Int {
    val list = buildList {
        s?.let(::add)
    }
    return list.size
}

fun box(): String {
    return when (test("hello")) {
        1 -> "OK"
        else -> "Error"
    }
}
