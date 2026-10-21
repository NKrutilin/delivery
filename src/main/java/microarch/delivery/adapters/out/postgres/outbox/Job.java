package microarch.delivery.adapters.out.postgres.outbox;

import com.fasterxml.jackson.databind.ObjectMapper;
import libs.ddd.DomainEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import microarch.delivery.core.ports.DomainEventProducer;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class Job {

    private final DomainEventProducer producer;
    private final OutboxJpaRepository jpa;
    private final ObjectMapper objectMapper;

    @Scheduled(fixedDelay = 1_000)
    public void run() {
        val outboxMessages = jpa.findUnprocessedMessages();
        for (val outboxMessage : outboxMessages) {
            try {
                val eventClass = Class.forName(outboxMessage.getEventType());
                val eventObject = objectMapper.readValue(outboxMessage.getPayload(), eventClass);

                if (!(eventObject instanceof DomainEvent domainEvent)) {
                    throw new IllegalStateException("Invalid outbox message type: " + eventClass);
                }

                producer.produce(domainEvent);

                outboxMessage.markAsProcessed();
                jpa.save(outboxMessage);
            } catch (Exception e) {
                log.error("Failed to publish outbox message {}: {}", outboxMessage.getId(), e.getMessage());
            }
        }
    }
}
