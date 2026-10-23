package microarch.delivery.core.application.command;

import java.util.UUID;

public record CreateOrderCommand(UUID orderId, String country, String city, String street, String house,
        String apartment, int volume) {
}
