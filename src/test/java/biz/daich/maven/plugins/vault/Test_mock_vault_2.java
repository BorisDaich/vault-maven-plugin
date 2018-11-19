package biz.daich.maven.plugins.vault;

import static org.hamcrest.CoreMatchers.any;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.bettercloud.vault.Vault;
import com.bettercloud.vault.VaultException;
import com.deciphernow.maven.plugins.vault.config.Mapping;
import com.deciphernow.maven.plugins.vault.config.Path;
import com.deciphernow.maven.plugins.vault.config.Server;

import biz.daich.maven.plugins.vault.VaultPullMojo.IVaultFactory;
import biz.daich.maven.plugins.vault.VaultPullMojo.KEY;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Test_mock_vault_2 {

	private static final String PROP_SECRET_ID = "prop_secret";

	private static final String PROP_ROLE_ID = "prop_role";

	VaultPullMojo vaultPullMojo;

	// mock the Vault and the Logical returning values from vault
	Vault mockVault;

	static final String SERVER_ID_1 = "id1";
	static final String PATH_IN_VAULT = "secret/revenova";

	static final String KEY_IN_PROPERTY_1 = "key_in_property_1";
	static final String KEY_IN_PROPERTY_2 = "key_in_property_2";

	static final String KEY_IN_SECRET_1 = "key_in_secret_1";
	static final String KEY_IN_SECRET_2 = "key_in_secret_2";

	static final String VALUE_IN_SECRET_1 = "value_of_secret_1";
	static final String VALUE_IN_SECRET_2 = "value_of_secret_2";

	static final String rolePropName1 = VaultPullMojo.propName(SERVER_ID_1, KEY.ROLE_ID);
	static final String tokenPropName1 = VaultPullMojo.propName(SERVER_ID_1, KEY.TOKEN);
	static final String secretPropName1 = VaultPullMojo.propName(SERVER_ID_1, KEY.SECRET_ID);

	static final String server_token = "server_token";
	static final String prop_token = "prop_token";
	static final String token_from_role_id = "token_from_role_id";

	Map<String, String> vault_contents;
	Server server;
	Properties properties;
	IVaultFactory vaultFactory;

	MavenProject mockMavenProject;

	@Before
	public void setUp() throws Exception {

		properties = new Properties();

		mockMavenProject = mock(MavenProject.class);
		when(mockMavenProject.getProperties()).thenReturn(properties);

		mockVault = mock(Vault.class, RETURNS_DEEP_STUBS);

		vault_contents = new HashMap<>();
		vault_contents.put(KEY_IN_SECRET_1, VALUE_IN_SECRET_1);
		vault_contents.put(KEY_IN_SECRET_2, VALUE_IN_SECRET_2);

		when(mockVault.logical().read(same(PATH_IN_VAULT)).getData()).thenReturn(vault_contents);

		when(mockVault.auth().loginByAppRole(same("approle"), same(PROP_ROLE_ID), same(PROP_SECRET_ID)).getAuthClientToken()).thenReturn(token_from_role_id);

		server = new Server();

		server.setUrl("url");
		server.setId(SERVER_ID_1);

		Mapping ma1 = new Mapping(KEY_IN_SECRET_1, KEY_IN_PROPERTY_1);
		Mapping ma2 = new Mapping(KEY_IN_SECRET_2, KEY_IN_PROPERTY_2);

		List<Mapping> mappings = Arrays.asList(ma1, ma2);

		Path path1 = new Path(PATH_IN_VAULT, mappings);
		List<Path> paths = Arrays.asList(path1);

		server.setPaths(paths);

		vaultFactory = (vc) -> mockVault;

		vaultPullMojo = new VaultPullMojo();
		vaultPullMojo.setVaultFactory(vaultFactory);
		vaultPullMojo.setProject(mockMavenProject);
		vaultPullMojo.setServers(Arrays.asList(server));
	}

	@Test
	public void test_0() {
		Properties properties2 = vaultPullMojo.getProject().getProperties();
		assertNotNull(properties2);
	}

	@After
	public void tearDown() throws Exception {
		Mockito.validateMockitoUsage();
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

	@Test
	public void test_pullServer_no_credentials() {

		// code under test

		// if there is no token and no Role_id and secret_id - throw
		try {
			vaultPullMojo.pullServer(server, properties);
			fail("was suppose to throw");
		} catch (Exception e) {
			assertTrue(e.getClass().equals(MojoFailureException.class));
			assertEquals("There is no token or role_id/secret_id for the server [" + SERVER_ID_1 + "] with url: [url]  Nothing we can do!", e.getMessage());
		}

	}

	// if there is a role_id and no secret_id - should throw
	@Test
	public void test_pullServer_no_secret_id_or_role_id() throws MojoExecutionException {
		properties.setProperty(rolePropName1, "value");

		try {
			// no secret_id
			vaultPullMojo.pullServer(server, properties);
			fail("was suppose to throw on no secret_id");
		} catch (MojoFailureException e) {
			log.info(e.getMessage());
			assertTrue(e.getClass().equals(MojoFailureException.class));
		}
		properties.remove(rolePropName1);
		assertTrue(!properties.containsKey(rolePropName1));

		properties.setProperty(secretPropName1, "value");

		try {
			// no role_id
			vaultPullMojo.pullServer(server, properties);
			fail("was suppose to throw on no role_id");
		} catch (MojoFailureException e) {
			log.info(e.getMessage());
			assertTrue(e.getClass().equals(MojoFailureException.class));
		}

	}

	/**
	 * test that if there is token it used and other not retrieved
	 * 
	 * @throws MojoExecutionException
	 * @throws MojoFailureException
	 */
	@Test
	public void test_pullServer_right_retrieval() throws MojoFailureException, MojoExecutionException {

		properties.setProperty(tokenPropName1, prop_token);
		server.setToken(server_token);
		vaultPullMojo.pullServer(server, properties);

		assertEquals(VALUE_IN_SECRET_2, properties.getProperty(KEY_IN_PROPERTY_2)); // values from vault were pulled
		assertEquals(server_token, server.getToken()); // token was not changed
	}

	@Test
	public void test_pullServer_token_by_roleId() throws MojoFailureException, MojoExecutionException {
		properties.setProperty(rolePropName1, PROP_ROLE_ID);
		properties.setProperty(secretPropName1, PROP_SECRET_ID);
		vaultPullMojo.pullServer(server, properties);
		assertEquals(token_from_role_id, server.getToken());
		assertEquals(VALUE_IN_SECRET_2, properties.getProperty(KEY_IN_PROPERTY_2)); // values from vault were pulled
		assertEquals(VALUE_IN_SECRET_1, properties.getProperty(KEY_IN_PROPERTY_1)); // values from vault were pulled
	}

	@Test
	public void test_execute_token_in_props() throws MojoExecutionException, MojoFailureException {
		properties.setProperty(tokenPropName1, prop_token);
		vaultPullMojo.execute();

		assertEquals(prop_token, server.getToken()); // token was retrieved from property
	}

	@Test
	public void test_multi_server() {
		fail("not implemented");
	}

	@Test
	public void test_default_server_id() {
		fail("not implemented");
	}

	@Test
	public void test_many_no_server_id() {
		fail("not implemented");
	}

}
