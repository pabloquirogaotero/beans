package com.quirogaotero.beans.ordering.domain;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MoneyTest {

    @Test
    void adds_amounts_of_the_same_currency() {
        assertEquals(Money.euros(3750), Money.euros(1250).plus(Money.euros(2500)));
    }

    @Test
    void refuses_to_add_different_currencies() {
        assertThrows(IllegalArgumentException.class,
            () -> Money.euros(1250).plus(new Money(1250, "USD")));
    }

    @Test
    void multiplies_by_a_quantity() {
        assertEquals(Money.euros(3750), Money.euros(1250).times(3));
    }

    @Test
    void cannot_be_negative() {
        assertThrows(IllegalArgumentException.class, () -> Money.euros(-1));
    }

}
