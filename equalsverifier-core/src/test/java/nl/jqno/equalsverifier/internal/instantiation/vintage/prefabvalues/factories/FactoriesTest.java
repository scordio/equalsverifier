package nl.jqno.equalsverifier.internal.instantiation.vintage.prefabvalues.factories;

import static nl.jqno.equalsverifier.internal.testhelpers.Util.coverThePrivateConstructor;

import org.junit.jupiter.api.Test;

class FactoriesTest {

    @Test
    void coverTheConstructor() {
        coverThePrivateConstructor(Factories.class);
    }
}
