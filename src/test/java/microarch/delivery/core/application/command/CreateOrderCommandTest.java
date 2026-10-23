package microarch.delivery.core.application.command;

import libs.errs.DomainInvariantException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CreateOrderCommandTest {

    private static CreateOrderCommand valid() {
        return new CreateOrderCommand(UUID.randomUUID(), "Россия", "Москва", "Тверская", "1", "2", 5);
    }

    @Test
    @DisplayName("принимает корректные данные")
    void acceptsValidInput() {
        assertThatCode(CreateOrderCommandTest::valid).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("отклоняет null orderId")
    void rejectsNullOrderId() {
        assertThatThrownBy(() -> new CreateOrderCommand(null, "Россия", "Москва", "Тверская", "1", "2", 5))
                .isInstanceOf(DomainInvariantException.class);
    }

    @Test
    @DisplayName("отклоняет пустые адресные поля")
    void rejectsBlankAddressFields() {
        assertThatThrownBy(() -> new CreateOrderCommand(UUID.randomUUID(), "", "Москва", "Тверская", "1", "2", 5))
                .isInstanceOf(DomainInvariantException.class);
        assertThatThrownBy(() -> new CreateOrderCommand(UUID.randomUUID(), "Россия", "Москва", " ", "1", "2", 5))
                .isInstanceOf(DomainInvariantException.class);
    }

    @Test
    @DisplayName("отклоняет неположительный объём")
    void rejectsNonPositiveVolume() {
        assertThatThrownBy(() -> new CreateOrderCommand(UUID.randomUUID(), "Россия", "Москва", "Тверская", "1", "2", 0))
                .isInstanceOf(DomainInvariantException.class);
        assertThatThrownBy(
                () -> new CreateOrderCommand(UUID.randomUUID(), "Россия", "Москва", "Тверская", "1", "2", -3))
                        .isInstanceOf(DomainInvariantException.class);
    }
}
