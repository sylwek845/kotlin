// FILE: AnnotationOwner.java

public class AnnotationOwner implements Annotated {
    private final String annotations;

    public AnnotationOwner(String annotations) {
        this.annotations = annotations;
    }

    public String getAnnotations() {
        return annotations;
    }
}

// FILE: Declaration.java

public interface Declaration extends Annotated {

}

// FILE: DeclarationImpl.java

public class DeclarationImpl extends AnnotationOwner implements Declaration {
    public DeclarationImpl(String annotations) {
        super(annotations);
    }
}

// FILE: PropertyDescriptor.java

public interface PropertyDescriptor extends DescriptorWithAccessor {
    String getSetter();
}

// FILE: signatureClash.kt

interface Annotated {
    val annotations: String
}

class SomeDeclaration(annotations: String) : DeclarationImpl(annotations)

interface DescriptorWithAccessor {
    val setter: String
}

class WrappedPropertyDescriptor : PropertyDescriptor {
    override val setter: String get() = "K"
}

fun box(): String {
    return SomeDeclaration("O").annotations + WrappedPropertyDescriptor().setter
}
