package microarch.delivery.core.domain.model;

import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class LocationTest {

    @ParameterizedTest
    @ValueSource(ints = { 1, 2, 7, 10 })
    void acceptsXInRange(final int x) {
        val result = Location.create(x, 1);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getValue().getX()).isEqualTo(x);
        assertThat(result.getValue().getY()).isEqualTo(1);
    }

    @ParameterizedTest
    @ValueSource(ints = { -1, 0, 11, 25 })
    void rejectsXOutOfRange(final int x) {
        val result = Location.create(x, 1);

        assertThat(result.isFailure()).isTrue();
        assertThat(result.getError().getCode()).isEqualTo("value.is.out.of.range");
    }

    @ParameterizedTest
    @ValueSource(ints = { 1, 4, 9, 10 })
    void acceptsYInRange(final int y) {
        val result = Location.create(1, y);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getValue().getY()).isEqualTo(y);
    }

    @ParameterizedTest
    @ValueSource(ints = { -3, 0, 11, 100 })
    void rejectsYOutOfRange(final int y) {
        val result = Location.create(1, y);

        assertThat(result.isFailure()).isTrue();
        assertThat(result.getError().getCode()).isEqualTo("value.is.out.of.range");
    }

    @Test
    void equalLocationsAreEquivalent() {
        val a = Location.create(3, 8).getValue();
        val b = Location.create(3, 8).getValue();

        assertThat(a).isEqualTo(b);
        assertThat(a.hashCode()).isEqualTo(b.hashCode());
    }

    @Test
    void locationsWithDifferentCoordinatesAreNotEquivalent() {
        val a = Location.create(3, 8).getValue();
        val b = Location.create(8, 3).getValue();

        assertThat(a).isNotEqualTo(b);
    }

    @Test
    void distanceIsSumOfStepsOnBothAxes() {
        val from = Location.create(2, 6).getValue();
        val to = Location.create(4, 9).getValue();

        val result = from.computeDistance(to);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getValue().getValue()).isEqualTo(5);
    }

    @Test
    void distanceIsSymmetric() {
        val a = Location.create(1, 1).getValue();
        val b = Location.create(10, 10).getValue();

        assertThat(a.computeDistance(b).getValue().getValue()).isEqualTo(b.computeDistance(a).getValue().getValue());
    }

    @Test
    void distanceToSamePointIsZero() {
        val point = Location.create(5, 5).getValue();

        val result = point.computeDistance(point);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getValue().getValue()).isZero();
    }

    @Test
    void distanceToNullLocationFails() {
        val from = Location.create(2, 2).getValue();

        val result = from.computeDistance(null);

        assertThat(result.isFailure()).isTrue();
        assertThat(result.getError().getCode()).isEqualTo("location.is.null");
    }
}
