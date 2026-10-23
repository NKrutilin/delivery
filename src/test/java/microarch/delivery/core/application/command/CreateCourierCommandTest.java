package microarch.delivery.core.application.command;

import libs.errs.DomainInvariantException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CreateCourierCommandTest {

    @Test
    @DisplayName("принимает непустое имя")
    void acceptsValidName() {
        assertThatCode(() -> new CreateCourierCommand("Ваня")).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("отклоняет null и пустое имя")
    void rejectsNullAndBlankName() {
        assertThatThrownBy(() -> new CreateCourierCommand(null)).isInstanceOf(DomainInvariantException.class);
        assertThatThrownBy(() -> new CreateCourierCommand("")).isInstanceOf(DomainInvariantException.class);
        assertThatThrownBy(() -> new CreateCourierCommand("   ")).isInstanceOf(DomainInvariantException.class);
    }
}
