package microarch.delivery.core.domain.model.order;

import libs.ddd.Aggregate;
import libs.errs.Error;
import libs.errs.Guard;
import libs.errs.Result;
import lombok.Getter;
import lombok.val;
import microarch.delivery.Constants;
import microarch.delivery.core.domain.model.Location;
import microarch.delivery.core.domain.model.Volume;

import java.util.Map;
import java.util.UUID;

@Getter
public class Order extends Aggregate<UUID> {

    private static final Map<OrderStatus, OrderStatus> NEXT_STATUS = Map.of(OrderStatus.Created, OrderStatus.Assigned,
            OrderStatus.Assigned, OrderStatus.Completed);

    private final Location location;
    private final Volume volume;
    private OrderStatus status;

    private Order(final UUID id, final Location location, final Volume volume, final OrderStatus status) {
        super(id);
        this.location = location;
        this.volume = volume;
        this.status = status;
    }

    public static Result<Order, Error> create(final UUID id, final Location location, final Volume volume) {
        val idError = (id == null) ? Error.of(Constants.ERR_CODE_OBJ_IS_NULL, "id must not be null") : null;
        val locationError = (location == null) ? Error.of(Constants.ERR_CODE_OBJ_IS_NULL, "location must not be null")
                : null;
        val volumeError = (volume == null) ? Error.of(Constants.ERR_CODE_OBJ_IS_NULL, "volume must not be null") : null;
        val error = Guard.combine(idError, locationError, volumeError);

        if (error != null) {
            return Result.failure(error);
        }

        return Result.success(new Order(id, location, volume, OrderStatus.Created));
    }

    public Result<Order, Error> changeStatus(final OrderStatus targetStatus) {
        if (targetStatus == null) {
            return Result.failure(Error.of(Constants.ERR_CODE_OBJ_IS_NULL, "targetStatus must not be null"));
        }

        if (NEXT_STATUS.get(status) != targetStatus) {
            return Result.failure(Error.of("order.transition.not.allowed",
                    "Transition %s -> %s is not allowed".formatted(status, targetStatus)));
        }

        status = targetStatus;
        return Result.success(this);
    }
}
