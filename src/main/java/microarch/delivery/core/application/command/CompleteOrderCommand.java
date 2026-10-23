package microarch.delivery.core.application.command;

import java.util.UUID;

import libs.errs.Error;
import libs.errs.Guard;
import lombok.val;

public record CompleteOrderCommand(UUID courierId, UUID orderId) {

    public CompleteOrderCommand {
        val err = Guard.combine(Guard.againstNullOrEmpty(courierId, "courierId"),
                Guard.againstNullOrEmpty(orderId, "orderId"));
        Error.throwIf(err);
    }
}
