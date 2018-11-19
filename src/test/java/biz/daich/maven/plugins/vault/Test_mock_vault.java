package biz.daich.maven.plugins.vault;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.matches;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.bettercloud.vault.Vault;
import com.bettercloud.vault.VaultConfig;
import com.bettercloud.vault.VaultException;
import com.bettercloud.vault.api.Auth;
import com.bettercloud.vault.api.Logical;
import com.bettercloud.vault.response.LogicalResponse;
import com.deciphernow.maven.plugins.vault.config.Mapping;
import com.deciphernow.maven.plugins.vault.config.Path;
import com.deciphernow.maven.plugins.vault.config.Server;

import biz.daich.maven.plugins.vault.VaultPullMojo.IVaultFactory;
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

		when(vault.logical().read(same("000")).getData()).thenReturn(m2);
	}

	@After
	public void tearDown() throws Exception {
		Mockito.validateMockitoUsage();
	}

	// this one relays on the #setUp()
	@Test
	public void test_mock() throws VaultException {

		Map<String, String> data2 = vault.logical().read("000").getData();
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

	// just checking that I am not crazy and know how patterns work in Java
	@Test
	public void test_001() {

		assertTrue(Pattern.matches(".*aa.*", "aa"));
		assertTrue(Pattern.matches(".*", "cc"));
	}

	/**
	 * sample of the mocking method call with patterns on parameters
	 */
	@Test
	public void test_01() throws Exception {
		Vault mockVault = mock(Vault.class);
		Logical l = mock(Logical.class, RETURNS_DEEP_STUBS);
		when(mockVault.logical()).thenReturn(l);

		Map<String, String> m1 = new HashMap<>();
		m1.put("key", "value");

		Map<String, String> m2 = new HashMap<>();
		m2.put("kkk", "vxxxx");

		Map<String, String> m3 = new HashMap<>();
		m3.put("aaa", "bbb");

		final String ARG3 = "cc";

		when(l.read(anyString()).getData()).thenReturn(m3);
		// when(l.read(anyString()).getData()).thenReturn(m3);
		Mockito.validateMockitoUsage();

		when(l.read(same("aa")).getData()).thenReturn(m1);
		Mockito.validateMockitoUsage();

		when(l.read(same("bb")).getData()).thenReturn(m2);
		Mockito.validateMockitoUsage();
		// when(l.read(matches(ARG3)).getData()).thenReturn(m3);

		Map<String, String> map1 = mockVault.logical().read("aa").getData();
		log.info("map1 = {}", map1);
		assertTrue("value".equals(map1.get("key")));

		Map<String, String> map2 = mockVault.logical().read("bb").getData();
		log.info("map2 = {}", map2);
		assertTrue("vxxxx".equals(map2.get("kkk")));

		Map<String, String> map3 = mockVault.logical().read(ARG3).getData();
		log.info("map3 = {}", map3);
		assertTrue("bbb".equals(map3.get("aaa")));

		log.debug("DONE!");
	}

	/**
	 * test that the mocking of logical works as expected.
	 * the question was will it mock and work with the different string params and different depth of the chained calls
	 * answer it works :-)
	 */
	@Test
	public void test_2() throws VaultException {

		Vault mockVault = mock(Vault.class);
		Logical l = mock(Logical.class, RETURNS_DEEP_STUBS);
		when(mockVault.logical()).thenReturn(l);

		Map<String, String> m1 = new HashMap<>();
		m1.put("key", "value");

		Map<String, String> m2 = new HashMap<>();
		m2.put("kkk", "vxxxx");

		{
			LogicalResponse lr1 = mock(LogicalResponse.class);
			when(lr1.getData()).thenReturn(m1);

			LogicalResponse lr2 = mock(LogicalResponse.class);
			when(lr2.getData()).thenReturn(m2);

			when(l.read("a")).thenReturn(lr1);
			when(l.read("b")).thenReturn(lr2);

			Map<String, String> map1 = mockVault.logical().read("a").getData();

			assertTrue("value".equals(map1.get("key")));

			Map<String, String> map2 = mockVault.logical().read("b").getData();

			assertTrue("vxxxx".equals(map2.get("kkk")));
		}
		{
			when(l.read("a").getData()).thenReturn(m1);
			when(l.read("b").getData()).thenReturn(m2);

			Map<String, String> map1 = mockVault.logical().read("a").getData();

			assertTrue("value".equals(map1.get("key")));

			Map<String, String> map2 = mockVault.logical().read("b").getData();

			assertTrue("vxxxx".equals(map2.get("kkk")));

		}
		log.debug("DONE!");
	}

	// really deep nested mocking
	@Test
	public void test_3() throws VaultException {

		// setup the test
		Vault mockVault = mock(Vault.class, RETURNS_DEEP_STUBS);
		Map<String, String> m1 = new HashMap<>();
		m1.put("key", "value");

		when(mockVault.logical().read(same("secret/revenova")).getData()).thenReturn(m1);

		// code under test
		Map<String, String> map2 = mockVault.logical().read("secret/revenova").getData();

		// validate
		assertTrue("value".equals(map2.get("key")));

	}

	/**
	 * test the simplest happy case
	 * there is a single server in conf
	 * and token and a single mapping
	 * 
	 * testing #pullVault(Vault vault, Server server, Properties properties)
	 * 
	 * @throws VaultException
	 */
	@Test
	public void test_pullVault() throws VaultException {

		VaultPullMojo vaultPullMojo = new VaultPullMojo();

		// mock the Vault and the Logical returning values from vault
		Vault mockVault = mock(Vault.class, RETURNS_DEEP_STUBS);

		final String PATH_IN_VAULT = "secret/revenova";

		final String KEY_IN_PROPERTY_1 = "key_in_property_1";
		final String KEY_IN_PROPERTY_2 = "key_in_property_2";

		final String KEY_IN_SECRET_1 = "key_in_secret_1";
		final String KEY_IN_SECRET_2 = "key_in_secret_2";

		final String VALUE_IN_SECRET_1 = "value_of_secret_1";
		final String VALUE_IN_SECRET_2 = "value_of_secret_2";

		Map<String, String> vault_contents = new HashMap<>();

		vault_contents.put(KEY_IN_SECRET_1, VALUE_IN_SECRET_1);
		vault_contents.put(KEY_IN_SECRET_2, VALUE_IN_SECRET_2);

		when(mockVault.logical().read(same(PATH_IN_VAULT)).getData()).thenReturn(vault_contents);

		Server server = new Server();

		server.setUrl("url");
		server.setId("id");

		Mapping ma1 = new Mapping(KEY_IN_SECRET_1, KEY_IN_PROPERTY_1);
		Mapping ma2 = new Mapping(KEY_IN_SECRET_2, KEY_IN_PROPERTY_2);

		List<Mapping> mappings = Arrays.asList(ma1, ma2);

		Path p1 = new Path(PATH_IN_VAULT, mappings);
		List<Path> paths = Arrays.asList(p1);

		server.setPaths(paths);

		Properties properties = new Properties();

		// code under test
		vaultPullMojo.pullVault(mockVault, server, properties);

		// validation
		assertTrue(properties.containsKey(KEY_IN_PROPERTY_1));
		String property1 = properties.getProperty(KEY_IN_PROPERTY_1);
		assertEquals(VALUE_IN_SECRET_1, property1);

		assertTrue(properties.containsKey(KEY_IN_PROPERTY_2));
		String property2 = properties.getProperty(KEY_IN_PROPERTY_2);
		assertEquals(VALUE_IN_SECRET_2, property2);
		log.debug("DONE!");
	}

	
	
	
	/***/
	@Test
	public void test_0() {

		// mocking for the golden thread scenario when all works as expected
		IVaultFactory vf = new IVaultFactory() {

			@Override
			public Vault vault(VaultConfig vc) {

				Vault v = null;
				try {
					Logical l = mock(Logical.class, RETURNS_DEEP_STUBS);
					v = mock(Vault.class);
					Auth a = mock(Auth.class);
					when(v.auth()).thenReturn(a);
					when(v.logical()).thenReturn(l);

					// mock the login with approle with just a valid string
					when(a.loginByAppRole(anyString(), anyString()).getAuthClientToken()).thenReturn("mockAppRoleTocken");

				} catch (VaultException e) {
					log.error("the mocking exception", e);
					throw new RuntimeException(e);
				}
				return v;
			}

		};
		fail("implementation not finished");
	}

}
