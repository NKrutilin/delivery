package microarch.delivery.core.application.command;

import java.util.UUID;

public record MoveCourierCommand(UUID courierId, int x, int y) {
}
