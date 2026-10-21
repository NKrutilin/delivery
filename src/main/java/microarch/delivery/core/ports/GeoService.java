package microarch.delivery.core.ports;

import libs.errs.Error;
import libs.errs.Result;
import microarch.delivery.core.domain.model.Location;

public interface GeoService {

    Result<Location, Error> getGeolocation(String street);
}
