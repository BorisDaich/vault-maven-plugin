/**
 * 
 */
package biz.daich.maven.plugins.vault;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import com.bettercloud.vault.Vault;
import com.bettercloud.vault.VaultException;

import biz.daich.maven.plugins.vault.VaultPullMojo.IVaultFactory;
import biz.daich.maven.plugins.vault.VaultPullMojo.KEY;
import biz.daich.maven.plugins.vault.config.Mapping;
import biz.daich.maven.plugins.vault.config.Path;
import biz.daich.maven.plugins.vault.config.Server;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Test_VaultPullMojo {

	private static final String URL1 = "url1";
	private static final String URL2 = "url2";

	private static final String PROP_SECRET_ID = "prop_secret";

	private static final String PROP_ROLE_ID = "prop_role";

	static final String SERVER_ID_1 = "id1";
	static final String SERVER_ID_2 = "id2";

	static final String PATH_IN_VAULT = "secret/revenova";

	static final String KEY_IN_PROPERTY_1 = "key_in_property_1";
	static final String KEY_IN_PROPERTY_2 = "key_in_property_2";

	static final String KEY_IN_PROPERTY_1_SERVER_2 = "key_in_property_1_SERVER_2";
	static final String KEY_IN_PROPERTY_2_SERVER_2 = "key_in_property_2_SERVER_2";

	static final String KEY_IN_SECRET_1 = "key_in_secret_1";
	static final String KEY_IN_SECRET_2 = "key_in_secret_2";

	static final String KEY_IN_SECRET_1_SERVER_2 = "key_in_secret_1_server_2";
	static final String KEY_IN_SECRET_2_SERVER_2 = "key_in_secret_2_server_2";

	static final String VALUE_IN_SECRET_1 = "value_of_secret_1";
	static final String VALUE_IN_SECRET_2 = "value_of_secret_2";

	static final String VALUE_IN_SECRET_1_SERVER_2 = "value_of_secret_1_server_2";
	static final String VALUE_IN_SECRET_2_SERVER_2 = "value_of_secret_2_server_2";

	static final String rolePropName1 = VaultPullMojo.propName(SERVER_ID_1, KEY.ROLE_ID);
	static final String tokenPropName1 = VaultPullMojo.propName(SERVER_ID_1, KEY.TOKEN);
	static final String secretPropName1 = VaultPullMojo.propName(SERVER_ID_1, KEY.SECRET_ID);

	static final String tokenPropName2 = VaultPullMojo.propName(SERVER_ID_2, KEY.TOKEN);

	static final String server_token = "server_token";
	static final String prop_token = "prop_token";
	static final String token_from_role_id = "token_from_role_id";

	VaultPullMojo vaultPullMojo;

	// mock the Vault and the Logical returning values from vault
	Vault mockVault;

	Map<String, String> vault_contents;
	Server server1;
	Server server2;
	Properties properties;
	IVaultFactory vaultFactory;

	MavenProject mockMavenProject;

	List<Mapping> mappings;

	List<Path> paths;

	List<Server> servers;

	@Before
	public void setUp() throws Exception {

		properties = new Properties();

		mockMavenProject = mock(MavenProject.class);
		when(mockMavenProject.getProperties()).thenReturn(properties);

		mockVault = mock(Vault.class, RETURNS_DEEP_STUBS);

		vault_contents = new HashMap<>();
		// for server 1
		vault_contents.put(KEY_IN_SECRET_1, VALUE_IN_SECRET_1);
		vault_contents.put(KEY_IN_SECRET_2, VALUE_IN_SECRET_2);
		// for server 2
		vault_contents.put(KEY_IN_SECRET_1_SERVER_2, VALUE_IN_SECRET_1_SERVER_2);
		vault_contents.put(KEY_IN_SECRET_2_SERVER_2, VALUE_IN_SECRET_2_SERVER_2);

		when(mockVault.logical().read(same(PATH_IN_VAULT)).getData()).thenReturn(vault_contents);

		when(mockVault.auth().loginByAppRole(same("approle"), same(PROP_ROLE_ID), same(PROP_SECRET_ID)).getAuthClientToken()).thenReturn(token_from_role_id);

		server1 = new Server();

		server1.setUrl(URL1);
		server1.setId(SERVER_ID_1);

		server2 = new Server();

		server2.setUrl(URL2);
		server2.setId(SERVER_ID_2);

		Mapping ma1 = new Mapping(KEY_IN_SECRET_1, KEY_IN_PROPERTY_1);
		Mapping ma2 = new Mapping(KEY_IN_SECRET_2, KEY_IN_PROPERTY_2);

		mappings = Arrays.asList(ma1, ma2);

		Path path1 = new Path(PATH_IN_VAULT, mappings);
		paths = Arrays.asList(path1);

		server1.setPaths(paths);

		vaultFactory = (vc) -> mockVault;

		servers = new ArrayList<>();
		servers.add(server1);

		vaultPullMojo = new VaultPullMojo(mockMavenProject, servers, vaultFactory);

	}

	/**
	 * simple not domain related test that proves that I use mockito and regex right  
	 * */
	@Test
	public void test_99() {
		String s = "a";
		String z;
		String b = z = s;
		assertEquals("a", b);
		assertEquals("a", z);

		assertTrue(Pattern.matches(".*MUST NOT.*", "Token, SecretId, RoleId for Vault server MUST NOT be set in the pom!!"));

		final String ss = "Token, SecretId, RoleId for Vault server MUST NOT be set in the pom!! They must be defined in your ~/.m2/settings.xml as property of the form <propoerties>\r\n" +
				"	<vault.server.default.token>YOUR_TOKEN</vault.server.default.token>\r\n" +
				"OR\r\n" +
				"	<vault.server.default.role_id>YOUR_ROLE_ID</vault.server.default.role_id>\r\n" +
				"	<vault.server.default.secret_id>YOUR_SECRET_ID</vault.server.default.secret_id>\r\n" +
				"</propoerties>\r\n" +
				"";
		assertTrue(Pattern.compile(".*MUST NOT.*", Pattern.DOTALL).matcher(ss).matches());
	}

	/**
	 * validate the {@link #setUp()}
	 * */
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
	 * there is a single server1 in conf
	 * and token and a single mapping
	 * 
	 * testing #pullVault(Vault vault, Server server1, Properties properties)
	 * 
	 * @throws VaultException
	 */
	@Test
	public void test_pullVault() throws VaultException {

		// code under test
		vaultPullMojo.pullVault(mockVault, server1, properties);

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
			vaultPullMojo.pullServer(server1, properties);
			fail("was suppose to throw");
		} catch (Exception e) {
			assertTrue(e.getClass().equals(MojoFailureException.class));
			assertEquals("There is no good token and one of role_id/secret_id is missing for the server [" + SERVER_ID_1 + "] with url: [" + URL1 + "]  Nothing we can do!", e.getMessage());
		}

	}

	// if there is a role_id and no secret_id - should throw
	@Test
	public void test_pullServer_no_secret_id_or_role_id() throws MojoExecutionException {
		properties.setProperty(rolePropName1, "value");

		try {
			// no secret_id
			vaultPullMojo.pullServer(server1, properties);
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
			vaultPullMojo.pullServer(server1, properties);
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
		server1.setToken(server_token);
		vaultPullMojo.pullServer(server1, properties);

		assertEquals(VALUE_IN_SECRET_2, properties.getProperty(KEY_IN_PROPERTY_2)); // values from vault were pulled
		assertEquals(server_token, server1.getToken()); // token was not changed
	}

	@Test
	public void test_pullServer_token_by_roleId() throws MojoFailureException, MojoExecutionException {
		properties.setProperty(rolePropName1, PROP_ROLE_ID);
		properties.setProperty(secretPropName1, PROP_SECRET_ID);
		vaultPullMojo.execute();
		assertEquals(token_from_role_id, server1.getToken());
		assertEquals(VALUE_IN_SECRET_2, properties.getProperty(KEY_IN_PROPERTY_2)); // values from vault were pulled
		assertEquals(VALUE_IN_SECRET_1, properties.getProperty(KEY_IN_PROPERTY_1)); // values from vault were pulled
	}

	@Test
	public void test_execute_token_in_props() throws MojoExecutionException, MojoFailureException {
		properties.setProperty(tokenPropName1, prop_token);
		vaultPullMojo.execute();

		assertEquals(prop_token, server1.getToken()); // token was retrieved from property
		log.debug("DONE!");
	}

	/**
	 * 2 servers
	 * 
	 * 1 has token in props
	 * 
	 * 1 has role_id/secret_id in props
	 */
	@Test
	public void test_multi_server() throws MojoExecutionException, MojoFailureException {

		properties.setProperty(rolePropName1, PROP_ROLE_ID);
		properties.setProperty(secretPropName1, PROP_SECRET_ID);
		properties.setProperty(tokenPropName2, prop_token);

		Mapping ma21 = new Mapping(KEY_IN_SECRET_1_SERVER_2, KEY_IN_PROPERTY_1_SERVER_2);
		Mapping ma22 = new Mapping(KEY_IN_SECRET_2_SERVER_2, KEY_IN_PROPERTY_2_SERVER_2);

		List<Mapping> mappings2 = Arrays.asList(ma21, ma22);

		Path path2 = new Path(PATH_IN_VAULT, mappings2);

		server2.setPaths(Arrays.asList(path2));

		servers.add(server2);

		vaultPullMojo.execute();

		assertEquals(VALUE_IN_SECRET_2, properties.getProperty(KEY_IN_PROPERTY_2)); // values from vault 1 were pulled
		assertEquals(VALUE_IN_SECRET_1, properties.getProperty(KEY_IN_PROPERTY_1)); // values from vault 1 were pulled

		assertEquals(VALUE_IN_SECRET_2_SERVER_2, properties.getProperty(KEY_IN_PROPERTY_2_SERVER_2)); // values from vault 2 were pulled
		assertEquals(VALUE_IN_SECRET_1_SERVER_2, properties.getProperty(KEY_IN_PROPERTY_1_SERVER_2)); // values from vault 2 were pulled

		log.debug("DONE!");
	}

	@Test
	public void test_default_server_id() throws MojoExecutionException, MojoFailureException {
		server1.setId(null);
		server1.setToken(server_token);
		vaultPullMojo.execute();
		assertEquals(VaultPullMojo.SERVER_DEFAULT_ID, server1.getId());
		log.debug("DONE!");
	}

	@Test
	public void test_many_no_server_id() throws MojoExecutionException, MojoFailureException {
		server1.setId(null);
		server1.setToken(server_token);

		server2.setId(null);
		server2.setToken(server_token);

		servers.add(server2);

		Log log = spy(vaultPullMojo.getLog());
		vaultPullMojo.setLog(log);

		vaultPullMojo.execute();

		ArgumentCaptor<String> strCaptor = ArgumentCaptor.forClass(String.class);
		verify(log, times(3)).warn(strCaptor.capture());

		List<String> allValues = strCaptor.getAllValues();
		assertEquals(3, allValues.size());

		final Pattern pattern1 = Pattern.compile(".*MUST NOT.*", Pattern.DOTALL);
		final Pattern pattern2 = Pattern.compile(".*more than one server without.*", Pattern.DOTALL);

		String string = allValues.get(0);
		assertTrue(pattern1.matcher(string).matches()); // server 1
		string = allValues.get(1);
		assertTrue(pattern2.matcher(string).matches());
		string = allValues.get(2);
		assertTrue(pattern1.matcher(string).matches()); // server 2
		log.debug("DONE!");
	}

}
