package microarch.delivery.core.application.command;

import java.util.UUID;

import libs.errs.Error;
import libs.errs.Guard;
import lombok.val;
import microarch.delivery.core.domain.model.Volume;

public record CreateOrderCommand(UUID orderId, String country, String city, String street, String house,
        String apartment, int volume) {

    public CreateOrderCommand {
        val volumeResult = Volume.create(volume);
        val err = Guard.combine(Guard.againstNullOrEmpty(orderId, "orderId"),
                Guard.againstNullOrEmpty(country, "country"), Guard.againstNullOrEmpty(city, "city"),
                Guard.againstNullOrEmpty(street, "street"), Guard.againstNullOrEmpty(house, "house"),
                Guard.againstNullOrEmpty(apartment, "apartment"),
                volumeResult.isFailure() ? volumeResult.getError() : null);
        Error.throwIf(err);
    }
}
