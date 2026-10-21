package microarch.delivery.core.domain.model.order;

import lombok.val;
import microarch.delivery.core.domain.model.Location;
import microarch.delivery.core.domain.model.Volume;
import microarch.delivery.core.domain.model.order.events.OrderAssignedDomainEvent;
import microarch.delivery.core.domain.model.order.events.OrderCompletedDomainEvent;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class OrderTest {

    @Test
    void createsOrderInCreatedStatus() {
        val order = newOrder();

        assertThat(order.getId()).isNotNull();
        assertThat(order.getStatus()).isEqualTo(OrderStatus.Created);
        assertThat(order.getLocation()).isEqualTo(Location.create(4, 7).getValue());
        assertThat(order.getVolume().getValue()).isEqualTo(3);
    }

    @Test
    void rejectsNullId() {
        val result = Order.create(null, Location.create(1, 1).getValue(), Volume.create(1).getValue());

        assertThat(result.isFailure()).isTrue();
        assertThat(result.getError().getCode()).isEqualTo("object.is.null");
    }

    @Test
    void rejectsNullLocation() {
        val result = Order.create(UUID.randomUUID(), null, Volume.create(1).getValue());

        assertThat(result.isFailure()).isTrue();
        assertThat(result.getError().getCode()).isEqualTo("object.is.null");
    }

    @Test
    void rejectsNullVolume() {
        val result = Order.create(UUID.randomUUID(), Location.create(1, 1).getValue(), null);

        assertThat(result.isFailure()).isTrue();
        assertThat(result.getError().getCode()).isEqualTo("object.is.null");
    }

    @Test
    void allowsTransitionFromCreatedToAssigned() {
        val order = newOrder();

        val result = order.markAsAssigned();

        assertThat(result.isSuccess()).isTrue();
        assertThat(order.getStatus()).isEqualTo(OrderStatus.Assigned);
    }

    @Test
    void allowsTransitionFromAssignedToCompleted() {
        val order = newOrder();
        order.markAsAssigned();

        val result = order.markAsCompleted();

        assertThat(result.isSuccess()).isTrue();
        assertThat(order.getStatus()).isEqualTo(OrderStatus.Completed);
    }

    @Test
    void rejectsTransitionFromCreatedStraightToCompleted() {
        val order = newOrder();

        val result = order.markAsCompleted();

        assertThat(result.isFailure()).isTrue();
        assertThat(result.getError().getCode()).isEqualTo("order.transition.not.allowed");
    }

    @Test
    void rejectsSecondAssignment() {
        val order = newOrder();
        order.markAsAssigned();

        val result = order.markAsAssigned();

        assertThat(result.isFailure()).isTrue();
        assertThat(result.getError().getCode()).isEqualTo("order.transition.not.allowed");
    }

    @Test
    void raisesAssignedEventOnSuccessfulTransition() {
        val order = newOrder();

        order.markAsAssigned();

        assertThat(order.getDomainEvents()).hasSize(1);
        assertThat(order.getDomainEvents().get(0)).isInstanceOf(OrderAssignedDomainEvent.class);
        assertThat(((OrderAssignedDomainEvent) order.getDomainEvents().get(0)).getOrderId()).isEqualTo(order.getId());
    }

    @Test
    void raisesCompletedEventOnSuccessfulCompletion() {
        val order = newOrder();
        order.markAsAssigned();

        order.markAsCompleted();

        assertThat(order.getDomainEvents()).hasSize(2);
        val completed = order.getDomainEvents().get(1);
        assertThat(completed).isInstanceOf(OrderCompletedDomainEvent.class);
        assertThat(((OrderCompletedDomainEvent) completed).getOrderId()).isEqualTo(order.getId());
    }

    @Test
    void raisesNoEventOnFailedTransition() {
        val order = newOrder();

        order.markAsCompleted();

        assertThat(order.getDomainEvents()).isEmpty();
    }

    private Order newOrder() {
        return Order.create(UUID.randomUUID(), Location.create(4, 7).getValue(), Volume.create(3).getValue())
                .getValue();
    }
}
