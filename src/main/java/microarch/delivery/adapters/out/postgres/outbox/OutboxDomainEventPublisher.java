package microarch.delivery.adapters.out.postgres.outbox;

import com.fasterxml.jackson.databind.ObjectMapper;
import libs.ddd.Aggregate;
import libs.ddd.DomainEventPublisher;
import lombok.val;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

@Repository
@Primary
public class OutboxDomainEventPublisher implements DomainEventPublisher {

    private final OutboxJpaRepository jpa;
    private final ObjectMapper objectMapper;

    public OutboxDomainEventPublisher(final OutboxJpaRepository jpa, final ObjectMapper objectMapper) {
        this.jpa = jpa;
        this.objectMapper = objectMapper;
    }

    @Override
    public void publish(final Iterable<? extends Aggregate<?>> aggregates) {
        for (Aggregate<?> aggregate : aggregates) {
            for (var domainEvent : aggregate.getDomainEvents()) {
                try {
                    val payload = objectMapper.writeValueAsString(domainEvent);

                    val outboxMessage = new OutboxMessage(domainEvent.getEventId(), domainEvent.getClass().getName(),
                            aggregate.getId().toString(), aggregate.getClass().getSimpleName(), payload,
                            domainEvent.getOccurredOnUtc());
                    jpa.save(outboxMessage);
                } catch (Exception e) {
                    throw new IllegalStateException("Failed to serialize domain event for outbox", e);
                }
            }

            aggregate.clearDomainEvents();
        }
    }
}
