package biz.daich.maven.plugins.vault;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.bettercloud.vault.Vault;
import com.bettercloud.vault.VaultException;
import com.bettercloud.vault.api.Logical;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Test_mock_vault {
	Vault vault;

	@Before
	public void setUp() throws Exception {
		vault = mock(Vault.class, RETURNS_DEEP_STUBS);

		Map<String, String> m1 = new HashMap<>();
		m1.put("key", "value");

		Map<String, String> m2 = new HashMap<>();
		m2.put("kkk", "vxxxx");
		Logical logical = vault.logical();
		when(vault.logical().read(anyString()).getData()).thenReturn(m1);

		when(vault.logical().read(matches("000")).getData()).thenReturn(m2);
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test_mock() throws VaultException {

		Map<String, String> data2 = vault.logical().read("1bbb1").getData();
		assertNotNull(data2);
		assertTrue(data2.entrySet().size() == 1);
		assertTrue(data2.containsKey("kkk"));
		assertTrue(data2.get("kkk").equals("vxxxx"));
		log.debug("data2: {}", data2);

		Map<String, String> data = vault.logical().read("zzzz").getData();
		assertNotNull(data);
		assertTrue(data.entrySet().size() == 1);
		assertTrue(data.containsKey("key"));
		assertTrue(data.get("key").equals("value"));
		log.debug("data: {}", data);

	}

}
