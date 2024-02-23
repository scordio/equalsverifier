package nl.jqno.equalsverifier.integration.extended_contract;

import java.util.Objects;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.internal.testhelpers.ExpectedException;
import org.junit.jupiter.api.Test;

class SealedTypesRecursionTest {

    @Test
    public void testEV() {
        // A container with a field of a sealed interface.
        // The sealed interface permits only 1 type, which refers back to the container.
        ExpectedException
            .when(() -> EqualsVerifier.forClass(SealedContainer.class).verify())
            .assertFailure()
            .assertMessageContains(
                "Recursive datastructure",
                "Add prefab values for one of the following types",
                "SealedContainer",
                "SealedInterface"
            );
    }

    static final class SealedContainer {

        public final SealedInterface sealed;

        public SealedContainer(SealedInterface sealed) {
            this.sealed = sealed;
        }

        @Override
        public int hashCode() {
            return Objects.hash(sealed);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof SealedContainer)) {
                return false;
            }
            SealedContainer other = (SealedContainer) obj;
            return Objects.equals(sealed, other.sealed);
        }
    }

    sealed interface SealedInterface permits OnlyPermittedImplementation {}

    static final class OnlyPermittedImplementation implements SealedInterface {

        public final SealedContainer container;

        public OnlyPermittedImplementation(SealedContainer container) {
            this.container = container;
        }

        @Override
        public int hashCode() {
            return Objects.hash(container);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof OnlyPermittedImplementation)) {
                return false;
            }
            OnlyPermittedImplementation other = (OnlyPermittedImplementation) obj;
            return Objects.equals(container, other.container);
        }
    }
}
