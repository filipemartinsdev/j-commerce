package com.products;

import io.floci.testcontainers.FlociContainer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest @ActiveProfiles("test")
@Import(TestContainerConfigs.class)
class MicroserviceProductsApplicationTests {
	@Autowired
	public FlociContainer flociContainer;

	@Test
	void contextLoads() {
	}

}
