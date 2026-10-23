package microarch.delivery.core.application.command;

import libs.errs.DomainInvariantException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CompleteOrderCommandTest {

    @Test
    @DisplayName("принимает корректные идентификаторы")
    void acceptsValidIds() {
        assertThatCode(() -> new CompleteOrderCommand(UUID.randomUUID(), UUID.randomUUID())).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("отклоняет null идентификаторы")
    void rejectsNullIds() {
        assertThatThrownBy(() -> new CompleteOrderCommand(null, UUID.randomUUID()))
                .isInstanceOf(DomainInvariantException.class);
        assertThatThrownBy(() -> new CompleteOrderCommand(UUID.randomUUID(), null))
                .isInstanceOf(DomainInvariantException.class);
    }
}
