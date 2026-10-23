package microarch.delivery.core.application.command.handler;

import lombok.val;
import microarch.delivery.core.application.command.MoveCourierCommand;
import microarch.delivery.core.domain.model.Location;
import microarch.delivery.core.domain.model.courier.Courier;
import microarch.delivery.core.ports.CourierRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MoveCourierHandlerTest {

    private final CourierRepository courierRepository = mock(CourierRepository.class);
    private final MoveCourierHandler handler = new MoveCourierHandler(courierRepository);

    @Test
    @DisplayName("делает один шаг в сторону целевой локации")
    void makesSingleStepTowardsTargetLocation() {
        val courier = Courier.create("Ваня", Location.create(1, 1).getValueOrThrow()).getValueOrThrow();
        when(courierRepository.findById(courier.getId())).thenReturn(Optional.of(courier));

        val result = handler.handle(new MoveCourierCommand(courier.getId(), 3, 2));

        assertThat(result.isSuccess()).isTrue();
        assertThat(courier.getLocation().getX()).isEqualTo(2);
        assertThat(courier.getLocation().getY()).isEqualTo(1);
        verify(courierRepository).update(courier);
    }

    @Test
    @DisplayName("возвращает ошибку, если курьер не найден")
    void failsWhenCourierDoesNotExist() {
        when(courierRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        val result = handler.handle(new MoveCourierCommand(UUID.randomUUID(), 2, 2));

        assertThat(result.isFailure()).isTrue();
        assertThat(result.getError().getCode()).isEqualTo("courier.not.found");
    }
}
