package com.tick_tracker;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = "tick.ingest.on-startup=false")
class TickTrackerApplicationTests {

	@Test
	void contextLoads() {
	}

}
