package com.concert.ticketing.services.kafka;

import com.concert.ticketing.dto.booking.BookingMessage;
import com.concert.ticketing.dto.booking.BookingRequest;
import com.concert.ticketing.services.booking.BookingService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingKafkaService {
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public void sendToQueue(BookingRequest request, String idempotencyKey) throws JsonProcessingException {
        log.info("Sending booking request to Kafka. CategoryId: {}, IdempotencyKey: {}", request.categoryId(),
                idempotencyKey);
        BookingMessage message = new BookingMessage(request, idempotencyKey);
        String payload = objectMapper.writeValueAsString(message);
        kafkaTemplate.send("booking-topic", request.categoryId().toString(), payload)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        log.info("Successfully sent booking request to Kafka. Offset: {}",
                                result.getRecordMetadata().offset());
                    } else {
                        log.error("Failed to send booking request to Kafka", ex);
                    }
                });
    }

    @Service
    @RequiredArgsConstructor
    public static class BookingWorker {
        private static final Logger log = LoggerFactory.getLogger(BookingWorker.class);
        private final BookingService bookingService;
        private final ObjectMapper objectMapper;

        @KafkaListener(topics = "booking-topic", groupId = "booking-group")
        public void consume(String message) {
            log.info("Received booking message from Kafka");
            try {
                BookingMessage msg = objectMapper.readValue(message, BookingMessage.class);
                log.debug("Processing booking message for IdempotencyKey: {}", msg.idempotencyKey());
                bookingService.processInternalBooking(msg);
                log.info("Successfully processed booking message for IdempotencyKey: {}", msg.idempotencyKey());
            } catch (JsonProcessingException e) {
                log.error("Failed to deserialize booking message: {}", message, e);
            } catch (Exception e) {
                log.error("Error processing booking message", e);
            }
        }
    }
}