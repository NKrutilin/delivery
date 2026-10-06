package microarch.delivery.core.domain.model.courier;

import lombok.val;
import microarch.delivery.core.domain.model.Location;
import microarch.delivery.core.domain.model.Volume;
import microarch.delivery.core.domain.model.order.Order;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class CourierTest {

    @Test
    void createsCourierWithDefaults() {
        val courier = Courier.create("Ivan", Location.create(3, 4).getValue()).getValue();

        assertThat(courier.getName()).isEqualTo("Ivan");
        assertThat(courier.getLocation()).isEqualTo(Location.create(3, 4).getValue());
        assertThat(courier.getMaxVolume().getValue()).isEqualTo(20);
        assertThat(courier.getAssignments()).isEmpty();
    }

    @Test
    void rejectsNullOrBlankName() {
        assertThat(Courier.create(null, Location.create(1, 1).getValue()).getError().getCode())
                .isEqualTo("courier.name.is.blank");
        assertThat(Courier.create("", Location.create(1, 1).getValue()).getError().getCode())
                .isEqualTo("courier.name.is.blank");
        assertThat(Courier.create("   ", Location.create(1, 1).getValue()).getError().getCode())
                .isEqualTo("courier.name.is.blank");
    }

    @Test
    void rejectsNullLocation() {
        val result = Courier.create("Ivan", null);

        assertThat(result.isFailure()).isTrue();
        assertThat(result.getError().getCode()).isEqualTo("object.is.null");
    }

    @Test
    void rejectsNullVolumeInCapacityCheck() {
        val courier = Courier.create("Ivan", Location.create(1, 1).getValue()).getValue();

        val result = courier.canTakeOneMoreOrder(null);

        assertThat(result.isFailure()).isTrue();
        assertThat(result.getError().getCode()).isEqualTo("object.is.null");
    }

    @Test
    void allowsOrderVolumeEqualToLimit() {
        val courier = Courier.create("Ivan", Location.create(1, 1).getValue()).getValue();

        val result = courier.canTakeOneMoreOrder(Volume.create(20).getValue());

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getValue()).isTrue();
    }

    @Test
    void reportsWhenPlannedVolumeExceedsLimit() {
        val courier = Courier.create("Ivan", Location.create(1, 1).getValue()).getValue();
        courier.takeOrder(newOrder(1, 15));

        val result = courier.canTakeOneMoreOrder(Volume.create(10).getValue());

        assertThat(result.isFailure()).isTrue();
        assertThat(result.getError().getCode()).isEqualTo("courier.volume.limit.exceeded");
    }

    @Test
    void takesOrder() {
        val courier = Courier.create("Ivan", Location.create(1, 1).getValue()).getValue();
        val order = newOrder(2, 5);

        val result = courier.takeOrder(order);

        assertThat(result.isSuccess()).isTrue();
        assertThat(courier.getAssignments()).hasSize(1);
        assertThat(courier.getAssignments().get(0).getOrderId()).isEqualTo(order.getId());
    }

    @Test
    void rejectsNullOrder() {
        val courier = Courier.create("Ivan", Location.create(1, 1).getValue()).getValue();

        val result = courier.takeOrder(null);

        assertThat(result.isFailure()).isTrue();
        assertThat(result.getError().getCode()).isEqualTo("object.is.null");
    }

    @Test
    void doesNotTakeSameOrderTwice() {
        val courier = Courier.create("Ivan", Location.create(1, 1).getValue()).getValue();
        val order = newOrder(2, 5);
        courier.takeOrder(order);

        val secondAttempt = courier.takeOrder(order);

        assertThat(secondAttempt.isFailure()).isTrue();
        assertThat(secondAttempt.getError().getCode()).isEqualTo("courier.order.already.assigned");
    }

    @Test
    void doesNotTakeOrderWhenVolumeLimitIsReached() {
        val courier = Courier.create("Ivan", Location.create(1, 1).getValue()).getValue();
        courier.takeOrder(newOrder(1, 15));

        val result = courier.takeOrder(newOrder(2, 10));

        assertThat(result.isFailure()).isTrue();
        assertThat(result.getError().getCode()).isEqualTo("courier.volume.limit.exceeded");
    }

    @Test
    void completesOrderWhenCourierIsNearby() {
        val courier = Courier.create("Ivan", Location.create(5, 5).getValue()).getValue();
        val order = Order.create(UUID.randomUUID(), Location.create(5, 5).getValue(), Volume.create(5).getValue())
                .getValue();
        courier.takeOrder(order);

        val result = courier.completeOrder(order.getId());

        assertThat(result.isSuccess()).isTrue();
        assertThat(courier.getAssignments()).isEmpty();
    }

    @Test
    void doesNotCompleteOrderWhenCourierIsFarAway() {
        val courier = Courier.create("Ivan", Location.create(1, 1).getValue()).getValue();
        val order = newOrder(2, 5);
        courier.takeOrder(order);

        val result = courier.completeOrder(order.getId());

        assertThat(result.isFailure()).isTrue();
        assertThat(result.getError().getCode()).isEqualTo("courier.far.from.order");
        assertThat(courier.getAssignments()).hasSize(1);
    }

    @Test
    void reportsUnknownOrderOnCompletion() {
        val courier = Courier.create("Ivan", Location.create(1, 1).getValue()).getValue();

        val result = courier.completeOrder(UUID.randomUUID());

        assertThat(result.isFailure()).isTrue();
        assertThat(result.getError().getCode()).isEqualTo("courier.order.not.found");
    }

    @Test
    void rejectsNullOrderIdOnCompletion() {
        val courier = Courier.create("Ivan", Location.create(1, 1).getValue()).getValue();

        val result = courier.completeOrder(null);

        assertThat(result.isFailure()).isTrue();
        assertThat(result.getError().getCode()).isEqualTo("object.is.null");
    }

    @Test
    void movesCourierAroundTheBoard() {
        val courier = Courier.create("Ivan", Location.create(5, 5).getValue()).getValue();

        assertThat(courier.moveUp().getValue().getLocation()).isEqualTo(Location.create(5, 6).getValue());
        assertThat(courier.moveRight().getValue().getLocation()).isEqualTo(Location.create(6, 6).getValue());
        assertThat(courier.moveDown().getValue().getLocation()).isEqualTo(Location.create(6, 5).getValue());
        assertThat(courier.moveLeft().getValue().getLocation()).isEqualTo(Location.create(5, 5).getValue());
    }

    @Test
    void doesNotMoveBeyondBoardBorders() {
        val courier = Courier.create("Ivan", Location.create(10, 10).getValue()).getValue();

        assertThat(courier.moveUp().isFailure()).isTrue();
        assertThat(courier.moveRight().isFailure()).isTrue();
        assertThat(courier.getLocation()).isEqualTo(Location.create(10, 10).getValue());
    }

    private Order newOrder(final long number, final int volume) {
        return Order.create(UUID.randomUUID(), Location.create(9, 9).getValue(), Volume.create(volume).getValue())
                .getValue();
    }
}
