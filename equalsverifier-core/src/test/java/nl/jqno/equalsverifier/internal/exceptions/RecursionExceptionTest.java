package nl.jqno.equalsverifier.internal.exceptions;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.LinkedHashSet;

import nl.jqno.equalsverifier.internal.reflection.TypeTag;
import nl.jqno.equalsverifier.testhelpers.types.Point;
import org.junit.jupiter.api.Test;

class RecursionExceptionTest {

    @Test
    void descriptionContainsAllTypes() {
        LinkedHashSet<TypeTag> stack = new LinkedHashSet<>();
        stack.add(new TypeTag(String.class));
        stack.add(new TypeTag(Point.class));
        stack.add(new TypeTag(Object.class));

        String message = new RecursionException(stack).getDescription();

        for (TypeTag tag : stack) {
            assertThat(message.contains(tag.toString())).isTrue();
        }
    }
}
