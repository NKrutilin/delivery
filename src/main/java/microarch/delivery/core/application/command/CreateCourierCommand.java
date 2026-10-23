package microarch.delivery.core.application.command;

import libs.errs.Error;
import libs.errs.Guard;

public record CreateCourierCommand(String name) {

    public CreateCourierCommand {
        Error.throwIf(Guard.againstNullOrEmpty(name, "name"));
    }
}
