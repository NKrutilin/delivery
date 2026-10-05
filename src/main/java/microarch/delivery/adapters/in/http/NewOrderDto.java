package microarch.delivery.adapters.in.http;

import java.util.UUID;

public record NewOrderDto(UUID id, AddressDto address, int volume) {
}
