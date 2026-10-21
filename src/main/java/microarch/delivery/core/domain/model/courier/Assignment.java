package microarch.delivery.core.domain.model.courier;

import libs.ddd.BaseEntity;
import libs.errs.Error;
import libs.errs.Guard;
import libs.errs.Result;
import lombok.Getter;
import lombok.val;
import microarch.delivery.Constants;
import microarch.delivery.core.domain.model.Distance;
import microarch.delivery.core.domain.model.Location;
import microarch.delivery.core.domain.model.Status;
import microarch.delivery.core.domain.model.Volume;

import java.util.UUID;

@Getter
public class Assignment extends BaseEntity<UUID> {

    private static final Distance MAX_COMPLETION_DISTANCE = Distance.create(1).getValueOrThrow();

    private final UUID orderId;
    private final Volume volume;
    private final Location location;
    private Status status;

    private Assignment(final UUID id, final UUID orderId, final Volume volume, final Location location,
            final Status status) {
        super(id);
        this.orderId = orderId;
        this.volume = volume;
        this.location = location;
        this.status = status;
    }

    public static Result<Assignment, Error> create(final UUID orderId, final Volume volume, final Location location) {
        val error = Guard.combine(Guard.againstNullOrEmpty(orderId, "orderId"),
                (volume == null) ? Error.of(Constants.ERR_CODE_OBJ_IS_NULL, "volume must not be null") : null,
                (location == null) ? Error.of(Constants.ERR_CODE_OBJ_IS_NULL, "location must not be null") : null);

        if (error != null) {
            return Result.failure(error);
        }

        return Result.success(new Assignment(UUID.randomUUID(), orderId, volume, location, Status.Assigned));
    }

    static Result<Assignment, Error> restore(final UUID id, final UUID orderId, final Volume volume,
            final Location location, final Status status) {
        val error = Guard.combine(Guard.againstNullOrEmpty(id, "id"), Guard.againstNullOrEmpty(orderId, "orderId"),
                (volume == null) ? Error.of(Constants.ERR_CODE_OBJ_IS_NULL, "volume must not be null") : null,
                (location == null) ? Error.of(Constants.ERR_CODE_OBJ_IS_NULL, "location must not be null") : null,
                (status == null) ? Error.of(Constants.ERR_CODE_OBJ_IS_NULL, "status must not be null") : null);

        if (error != null) {
            return Result.failure(error);
        }

        return Result.success(new Assignment(id, orderId, volume, location, status));
    }

    Result<Assignment, Error> complete(final Location courierLocation) {
        val error = Guard.combine(
                (courierLocation == null) ? Error.of(Constants.ERR_CODE_OBJ_IS_NULL, "courierLocation must not be null")
                        : null,
                (status == Status.Completed)
                        ? Error.of("assignment.already.completed", "Assignment " + id + " is already completed")
                        : null);

        if (error != null) {
            return Result.failure(error);
        }

        val distance = courierLocation.computeDistance(location).getValueOrThrow();

        if (distance.isGreaterThan(MAX_COMPLETION_DISTANCE)) {
            return Result.failure(Error.of("out.of.completion.zone",
                    "Courier is too far from the order location to complete the assignment"));
        }

        status = Status.Completed;
        return Result.success(this);
    }
}
