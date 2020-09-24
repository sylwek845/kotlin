// FILE: PropertyDescriptor.java

public interface PropertyDescriptor extends DescriptorWithAccessor {
    String getSetter();
}

// FILE: test.kt

interface DescriptorWithAccessor {
    val setter: String
}

class WrappedPropertyDescriptor : PropertyDescriptor {
    override val setter: String get() = "K"
}

fun test() {
    val descriptor = WrappedPropertyDescriptor()
    val res1 = descriptor.setter
    val res2 = descriptor.<!UNRESOLVED_REFERENCE!>getSetter<!>() // Should be error
}