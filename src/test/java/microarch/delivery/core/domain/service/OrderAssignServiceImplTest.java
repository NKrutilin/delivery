package microarch.delivery.core.domain.service;

import lombok.val;
import microarch.delivery.core.domain.model.Location;
import microarch.delivery.core.domain.model.Volume;
import microarch.delivery.core.domain.model.courier.Courier;
import microarch.delivery.core.domain.model.order.Order;
import microarch.delivery.core.domain.model.order.OrderStatus;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class OrderAssignServiceImplTest {

    private final OrderAssignService service = new OrderAssignServiceImpl();

    @Test
    void assignsOrderToTheNearestFreeCourier() {
        val near = Courier.create("Near", Location.create(2, 2).getValue()).getValue();
        val far = Courier.create("Far", Location.create(9, 9).getValue()).getValue();
        val order = newOrder(Location.create(1, 1).getValue(), 5);

        val result = service.assign(order, List.of(far, near));

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getValue()).isEqualTo(near);
        assertThat(near.getAssignments()).hasSize(1);
        assertThat(far.getAssignments()).isEmpty();
        assertThat(order.getStatus()).isEqualTo(OrderStatus.Assigned);
    }

    @Test
    void skipsOverloadedCourierEvenIfItIsCloser() {
        val overloaded = Courier.create("Full", Location.create(2, 2).getValue()).getValue();
        val free = Courier.create("Free", Location.create(9, 9).getValue()).getValue();
        overloaded.takeOrder(newOrder(Location.create(8, 8).getValue(), 20));

        val order = newOrder(Location.create(1, 1).getValue(), 5);

        val result = service.assign(order, List.of(overloaded, free));

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getValue()).isEqualTo(free);
        assertThat(overloaded.getAssignments()).hasSize(1);
    }

    @Test
    void failsWhenThereAreNoCouriersAtAll() {
        val order = newOrder(Location.create(1, 1).getValue(), 5);

        val result = service.assign(order, List.of());

        assertThat(result.isFailure()).isTrue();
        assertThat(result.getError().getCode()).isEqualTo("assign.service.no.couriers");
    }

    @Test
    void failsWhenEveryCourierIsOverloaded() {
        val full = Courier.create("Full", Location.create(2, 2).getValue()).getValue();
        full.takeOrder(newOrder(Location.create(3, 3).getValue(), 20));

        val order = newOrder(Location.create(1, 1).getValue(), 5);

        val result = service.assign(order, List.of(full));

        assertThat(result.isFailure()).isTrue();
        assertThat(result.getError().getCode()).isEqualTo("assign.service.no.couriers");
    }

    @Test
    void failsWhenOrderIsNotInCreatedStatus() {
        val courier = Courier.create("Ivan", Location.create(2, 2).getValue()).getValue();
        val order = newOrder(Location.create(1, 1).getValue(), 5);
        order.markAsAssigned();

        val result = service.assign(order, List.of(courier));

        assertThat(result.isFailure()).isTrue();
        assertThat(result.getError().getCode()).isEqualTo("assign.service.wrong.order.status");
    }

    @Test
    void rejectsNullOrder() {
        val courier = Courier.create("Ivan", Location.create(2, 2).getValue()).getValue();

        val result = service.assign(null, List.of(courier));

        assertThat(result.isFailure()).isTrue();
        assertThat(result.getError().getCode()).isEqualTo("object.is.null");
    }

    @Test
    void rejectsNullCouriers() {
        val order = newOrder(Location.create(1, 1).getValue(), 5);

        val result = service.assign(order, null);

        assertThat(result.isFailure()).isTrue();
        assertThat(result.getError().getCode()).isEqualTo("object.is.null");
    }

    private Order newOrder(final Location location, final int volume) {
        return Order.create(UUID.randomUUID(), location, Volume.create(volume).getValue()).getValue();
    }
}
