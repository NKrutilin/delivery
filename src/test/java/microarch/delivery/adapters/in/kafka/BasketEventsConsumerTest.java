package microarch.delivery.adapters.in.kafka;

import libs.errs.UnitResult;
import lombok.val;
import microarch.delivery.core.application.command.CreateOrderCommand;
import microarch.delivery.core.application.command.handler.CreateOrderHandler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import queues.basket.events.BasketEventsProto;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class BasketEventsConsumerTest {

    private final CreateOrderHandler createOrderHandler = mock(CreateOrderHandler.class);
    private final BasketEventsConsumer consumer = new BasketEventsConsumer(createOrderHandler);

    @Test
    @DisplayName("создаёт заказ из события BasketConfirmed")
    void createsOrderFromBasketConfirmedEvent() {
        val event = BasketEventsProto.BasketConfirmedIntegrationEvent.newBuilder()
                .setBasketId(UUID.randomUUID().toString())
                .setAddress(BasketEventsProto.Address.newBuilder().setCountry("Россия").setCity("Москва")
                        .setStreet("Тверская").setHouse("1").setApartment("2").build())
                .setVolume(5).build();
        when(createOrderHandler.handle(any())).thenReturn(UnitResult.success());

        consumer.handle(event.toByteArray());

        val captor = ArgumentCaptor.forClass(CreateOrderCommand.class);
        verify(createOrderHandler).handle(captor.capture());
        val command = captor.getValue();
        assertThat(command.orderId()).isEqualTo(UUID.fromString(event.getBasketId()));
        assertThat(command.country()).isEqualTo("Россия");
        assertThat(command.city()).isEqualTo("Москва");
        assertThat(command.street()).isEqualTo("Тверская");
        assertThat(command.house()).isEqualTo("1");
        assertThat(command.apartment()).isEqualTo("2");
        assertThat(command.volume()).isEqualTo(5);
    }

    @Test
    @DisplayName("пропускает некорректный payload без создания заказа")
    void skipsMalformedPayload() {
        consumer.handle(new byte[] { 1, 2, 3 });

        verifyNoInteractions(createOrderHandler);
    }
}
