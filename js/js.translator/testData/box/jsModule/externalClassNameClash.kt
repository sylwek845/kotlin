// EXPECTED_REACHABLE_NODES: 1284
// FILE: a.kt
// MODULE_KIND: AMD
@file:JsModule("a")
package a

external class A {
    fun foo(): String
}

// FILE: b.kt
// MODULE_KIND: AMD
@file:JsModule("b")
package b

external class A {
    fun foo(): String
}

// FILE: main.kt

import a.A as O
import b.A as K

fun box(): String {
    return O().foo() + K().foo()
}