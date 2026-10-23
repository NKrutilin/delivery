package microarch.delivery.core.application.command;

import java.util.UUID;

import libs.errs.Error;
import libs.errs.Guard;
import lombok.val;
import microarch.delivery.core.domain.model.Location;

public record MoveCourierCommand(UUID courierId, int x, int y) {

    public MoveCourierCommand {
        val target = Location.create(x, y);
        val err = Guard.combine(Guard.againstNullOrEmpty(courierId, "courierId"),
                target.isFailure() ? target.getError() : null);
        Error.throwIf(err);
    }
}
