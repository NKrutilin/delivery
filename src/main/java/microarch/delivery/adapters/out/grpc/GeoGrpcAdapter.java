package microarch.delivery.adapters.out.grpc;

import clients.geo.GeoGrpc;
import clients.geo.GeoProto;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import libs.errs.Error;
import libs.errs.Result;
import lombok.val;
import microarch.delivery.core.domain.model.Location;
import microarch.delivery.core.ports.GeoService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class GeoGrpcAdapter implements GeoService {

    private final GeoGrpc.GeoBlockingStub stub;

    public GeoGrpcAdapter(@Value("${geo.host:localhost}") final String host, @Value("${geo.port:5004}") final int port,
            @Value("${geo.deadline-ms:2000}") final long deadlineMs) {
        stub = GeoGrpc.newBlockingStub(ManagedChannelBuilder.forAddress(host, port).usePlaintext().build())
                .withDeadlineAfter(deadlineMs, TimeUnit.MILLISECONDS);
    }

    @Override
    public Result<Location, Error> getGeolocation(final String street) {
        if (street == null || street.isBlank()) {
            return Result.failure(Error.of("geo.street.is.blank", "Street must not be null or blank"));
        }

        val request = GeoProto.GetGeolocationRequest.newBuilder().setStreet(street).build();

        try {
            val reply = stub.getGeolocation(request);
            val location = Location.create(reply.getLocation().getX(), reply.getLocation().getY());
            return location.isSuccess() ? Result.success(location.getValue()) : Result.failure(location.getError());
        } catch (StatusRuntimeException ex) {
            return Result.failure(
                    Error.of("geo.service.unavailable", "Geo service call failed: " + ex.getStatus().getCode()));
        }
    }
}
