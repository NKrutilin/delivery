package microarch.delivery.core.domain.model;

import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class VolumeTest {

    @ParameterizedTest
    @ValueSource(ints = { 1, 3, 100 })
    void acceptsPositiveVolume(final int value) {
        val result = Volume.create(value);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getValue().getValue()).isEqualTo(value);
    }

    @ParameterizedTest
    @ValueSource(ints = { 0, -5 })
    void rejectsZeroAndNegativeVolume(final int value) {
        val result = Volume.create(value);

        assertThat(result.isFailure()).isTrue();
        assertThat(result.getError().getCode()).isEqualTo("value.must.be.greater.or.equal");
    }

    @Test
    void equalVolumesAreEquivalent() {
        val a = Volume.create(4).getValue();
        val b = Volume.create(4).getValue();

        assertThat(a).isEqualTo(b);
        assertThat(a.hashCode()).isEqualTo(b.hashCode());
    }
}
