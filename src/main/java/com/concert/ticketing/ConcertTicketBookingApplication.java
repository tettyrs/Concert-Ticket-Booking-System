package com.concert.ticketing;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ConcertTicketBookingApplication {

	public static void main(String[] args) {
		SpringApplication.run(ConcertTicketBookingApplication.class, args);
	}

}
