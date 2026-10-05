package microarch.delivery.core.domain.service;

import libs.errs.Error;
import libs.errs.Result;
import lombok.val;
import microarch.delivery.Constants;
import microarch.delivery.core.domain.model.courier.Courier;
import microarch.delivery.core.domain.model.order.Order;
import microarch.delivery.core.domain.model.order.OrderStatus;
import org.springframework.stereotype.Service;

import java.util.Collection;

@Service
public class OrderAssignServiceImpl implements OrderAssignService {

    @Override
    public Result<Courier, Error> assign(final Order order, final Collection<Courier> couriers) {
        if (order == null) {
            return Result.failure(Error.of(Constants.ERR_CODE_OBJ_IS_NULL, "order must not be null"));
        }
        if (couriers == null) {
            return Result.failure(Error.of(Constants.ERR_CODE_OBJ_IS_NULL, "couriers must not be null"));
        }
        if (order.getStatus() != OrderStatus.Created) {
            return Result.failure(
                    Error.of("assign.service.wrong.order.status", "Only orders in Created status can be assigned"));
        }

        Courier nearestCourier = null;
        int nearestDistance = Integer.MAX_VALUE;

        for (val courier : couriers) {
            if (courier.canTakeOneMoreOrder(order.getVolume()).isFailure()) {
                continue;
            }

            val distance = courier.getLocation().computeDistance(order.getLocation()).getValueOrThrow();
            if (distance.getValue() < nearestDistance) {
                nearestCourier = courier;
                nearestDistance = distance.getValue();
            }
        }

        if (nearestCourier == null) {
            return Result.failure(
                    Error.of("assign.service.no.couriers", "No courier is available for order " + order.getId()));
        }

        val taken = nearestCourier.takeOrder(order);
        if (taken.isFailure()) {
            return Result.failure(taken.getError());
        }

        val marked = order.markAsAssigned();
        if (marked.isFailure()) {
            return Result.failure(marked.getError());
        }

        return Result.success(nearestCourier);
    }
}
