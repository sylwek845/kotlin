// TARGET_BACKEND: JVM
// IGNORE_BACKEND: JVM
// IGNORE_LIGHT_ANALYSIS

fun box(): String {
    val list = ArrayList<Long>()
    list.add(5.inv())
    return if (list[0] == -6L) "OK" else "fail: ${list[0]} != -6"
}
