package microarch.delivery.core.application.command;

import java.util.UUID;

public record CompleteOrderCommand(UUID courierId, UUID orderId) {
}
