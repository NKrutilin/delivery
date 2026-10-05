package microarch.delivery.core.domain.model;

import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class DistanceTest {

    @ParameterizedTest
    @ValueSource(ints = { 0, 1, 18 })
    void acceptsNonNegativeValue(final int value) {
        val result = Distance.create(value);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getValue().getValue()).isEqualTo(value);
    }

    @ParameterizedTest
    @ValueSource(ints = { -1, -100 })
    void rejectsNegativeValue(final int value) {
        val result = Distance.create(value);

        assertThat(result.isFailure()).isTrue();
        assertThat(result.getError().getCode()).isEqualTo("value.must.be.greater.than");
    }

    @Test
    void equalDistancesAreEquivalent() {
        val a = Distance.create(7).getValue();
        val b = Distance.create(7).getValue();

        assertThat(a).isEqualTo(b);
    }
}
