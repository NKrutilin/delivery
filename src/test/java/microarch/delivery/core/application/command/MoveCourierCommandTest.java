package microarch.delivery.core.application.command;

import libs.errs.DomainInvariantException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MoveCourierCommandTest {

    @Test
    @DisplayName("принимает корректные координаты внутри доски")
    void acceptsValidCoordinates() {
        assertThatCode(() -> new MoveCourierCommand(UUID.randomUUID(), 1, 1)).doesNotThrowAnyException();
        assertThatCode(() -> new MoveCourierCommand(UUID.randomUUID(), 10, 10)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("отклоняет null courierId")
    void rejectsNullCourierId() {
        assertThatThrownBy(() -> new MoveCourierCommand(null, 5, 5)).isInstanceOf(DomainInvariantException.class);
    }

    @Test
    @DisplayName("отклоняет координаты вне диапазона доски")
    void rejectsOutOfRangeCoordinates() {
        assertThatThrownBy(() -> new MoveCourierCommand(UUID.randomUUID(), 0, 5))
                .isInstanceOf(DomainInvariantException.class);
        assertThatThrownBy(() -> new MoveCourierCommand(UUID.randomUUID(), 5, 11))
                .isInstanceOf(DomainInvariantException.class);
    }
}
