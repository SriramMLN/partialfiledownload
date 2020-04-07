package com.infosys.filedownload;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@TestPropertySource("classpath:application_test.properties")
public class MainControllerTest {

	@Value("${KEY_ONE}")
	private String keyOne;

	@Test
	public void getKeyOne() {
		String expected = "app_test.properties";
		assertEquals("----Message----",expected, keyOne);
	}
}
