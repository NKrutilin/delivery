package microarch.delivery.adapters.in.http;

import libs.errs.Error;
import libs.errs.Result;
import libs.errs.UnitResult;
import lombok.AllArgsConstructor;
import lombok.val;
import microarch.delivery.core.application.command.CompleteOrderCommand;
import microarch.delivery.core.application.command.CreateCourierCommand;
import microarch.delivery.core.application.command.CreateOrderCommand;
import microarch.delivery.core.application.command.MoveCourierCommand;
import microarch.delivery.core.application.command.handler.AssignOrderHandler;
import microarch.delivery.core.application.command.handler.CompleteOrderHandler;
import microarch.delivery.core.application.command.handler.CreateCourierHandler;
import microarch.delivery.core.application.command.handler.CreateOrderHandler;
import microarch.delivery.core.application.command.handler.MoveCourierHandler;
import microarch.delivery.core.application.dto.CourierDto;
import microarch.delivery.core.application.dto.OrderDto;
import microarch.delivery.core.application.query.GetAllCouriersQuery;
import microarch.delivery.core.application.query.GetNotCompletedOrdersQuery;
import microarch.delivery.core.application.query.handler.GetAllCouriersHandler;
import microarch.delivery.core.application.query.handler.GetNotCompletedOrdersHandler;
import microarch.delivery.core.domain.model.Location;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@AllArgsConstructor
public class DeliveryController {

    private final CreateOrderHandler createOrderHandler;
    private final CreateCourierHandler createCourierHandler;
    private final MoveCourierHandler moveCourierHandler;
    private final AssignOrderHandler assignOrderHandler;
    private final CompleteOrderHandler completeOrderHandler;
    private final GetAllCouriersHandler getAllCouriersHandler;
    private final GetNotCompletedOrdersHandler getNotCompletedOrdersHandler;

    @PostMapping("/api/v1/orders")
    public ResponseEntity<?> createOrder(@RequestBody final NewOrderDto request) {
        val command = new CreateOrderCommand(request.id(), request.address().country(), request.address().city(),
                request.address().street(), request.address().house(), request.address().apartment(), request.volume());

        val result = createOrderHandler.handle(command);
        if (result.isFailure()) {
            return toErrorResponse(result.getError());
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(new CreateOrderResponseDto(request.id()));
    }

    @PostMapping("/api/v1/couriers")
    public ResponseEntity<?> createCourier(@RequestBody final NewCourierDto request) {
        val result = createCourierHandler.handle(new CreateCourierCommand(request.name()));
        if (result.isFailure()) {
            return toErrorResponse(result.getError());
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(new CreateCourierResponseDto(result.getValue()));
    }

    @PostMapping("/api/v1/couriers/{courierId}/move")
    public ResponseEntity<?> moveCourier(@PathVariable final UUID courierId, @RequestBody final LocationDto request) {
        val location = Location.create(request.x(), request.y());
        if (location.isFailure()) {
            return toBadRequest(location.getError());
        }

        val result = moveCourierHandler.handle(new MoveCourierCommand(courierId, location.getValue()));
        if (result.isFailure()) {
            return toErrorResponse(result.getError());
        }

        return ResponseEntity.ok().build();
    }

    @PostMapping("/api/v1/couriers/{courierId}/orders/{orderId}/complete")
    public ResponseEntity<?> completeOrder(@PathVariable final UUID courierId, @PathVariable final UUID orderId) {
        val result = completeOrderHandler.handle(new CompleteOrderCommand(courierId, orderId));
        if (result.isFailure()) {
            return toErrorResponse(result.getError());
        }

        return ResponseEntity.ok().build();
    }

    @GetMapping("/api/v1/couriers")
    public ResponseEntity<List<CourierResponseDto>> getCouriers() {
        val couriers = getAllCouriersHandler
                .handle(new GetAllCouriersQuery()).stream().map(courier -> new CourierResponseDto(courier.id(),
                        courier.name(), new LocationDto(courier.location().getX(), courier.location().getY())))
                .toList();

        return ResponseEntity.ok(couriers);
    }

    @GetMapping("/api/v1/orders/active")
    public ResponseEntity<List<OrderResponseDto>> getOrders() {
        val orders = getNotCompletedOrdersHandler.handle(new GetNotCompletedOrdersQuery()).stream()
                .map(order -> new OrderResponseDto(order.id(),
                        new LocationDto(order.location().getX(), order.location().getY())))
                .toList();

        return ResponseEntity.ok(orders);
    }

    private ResponseEntity<?> toErrorResponse(final Error error) {
        val status = isConflict(error) ? HttpStatus.CONFLICT : HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(status).body(new ErrorResponseDto(status.value(), error.getMessage()));
    }

    private ResponseEntity<?> toBadRequest(final Error error) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponseDto(HttpStatus.BAD_REQUEST.value(), error.getMessage()));
    }

    private boolean isConflict(final Error error) {
        val code = error.getCode();
        return code.contains("not.found") || code.contains("already.assigned") || code.contains("not.allowed")
                || code.contains("far.from.order") || code.contains("not.exist");
    }
}
