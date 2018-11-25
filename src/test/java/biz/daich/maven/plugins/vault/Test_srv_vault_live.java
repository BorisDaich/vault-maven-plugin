package biz.daich.maven.plugins.vault;

import static biz.daich.maven.plugins.vault.VaultPullMojo.propName;
import static biz.daich.maven.plugins.vault.VaultPullMojo.KEY.ROLE_ID;
import static biz.daich.maven.plugins.vault.VaultPullMojo.KEY.SECRET_ID;
import static biz.daich.maven.plugins.vault.VaultPullMojo.KEY.TOKEN;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.bettercloud.vault.SslConfig;
import com.bettercloud.vault.VaultConfig;
import com.bettercloud.vault.VaultException;
import com.bettercloud.vault.api.Auth;
import com.google.common.base.Stopwatch;
import com.google.common.base.Strings;

import biz.daich.maven.plugins.vault.config.Mapping;
import biz.daich.maven.plugins.vault.config.Path;
import biz.daich.maven.plugins.vault.config.Server;
import lombok.extern.slf4j.Slf4j;

/**
 * WARNING!!! THIS TEST runs VS real server and use REAL KEYs
 * uses auth/approle/role/test-role
 *
 * IMPORTANT you have to have env variables with value like:
 * <br>
 * </b>vault.server.id </b> == "aaaa"
 * <br>
 * <b>vault.server.url</b> == http://localhost:8200
 * <br>
 * <b>vault.server.role_id</b> === aaaaaaaaaaaaaaaaaaaaaaaaaa
 * <br>
 * <b>vault.server.secret_id</b> === bbbbbbbbbbbbbbb
 * <br>
 * to make this work
 * on the vault server you have to have
 * secret at
 * <b>secret/test/test</b>
 * with value
 * 
 * <pre>
 * {
 * 		"test_key_1": "test_value_1",
 * 		"test_key_2": "test_value_2"
 * }
 * </pre>
 * 
 * <br>
 * 
 * 
 * 
 * @author boris
 *
 */

@Slf4j
@Ignore
public class Test_srv_vault_live {

	private static final String MAVEN_PROPERTY_VALUE_1 = "maven.property.value.one";
	private static final String MAVEN_PROPERTY_VALUE_2 = "maven.property.value.two";

	// /secret/test/test is a test secret in the vault for testing only
	private static final String TEST_SECRET_IN_VAULT = "secret/test/test";

	private static String VAULT_SERVER_URL = null;

	// this is auth/approle/role/test-role
	private static String ROLE_ID_STR =null;
	private static String SECRET_STR =null;


	private static String SERVER_ID_STR = null;

	protected VaultPullMojo mojo;
	protected Properties properties;
	private List<Server> servers;

	@BeforeClass
	static public void classSetUp() {
		SERVER_ID_STR = System.getProperty("vault.server.id", SERVER_ID_STR);		
		VAULT_SERVER_URL = System.getProperty("vault.server.url", VAULT_SERVER_URL);
		ROLE_ID_STR = System.getProperty("vault.server.role_id", null);
		SECRET_STR= System.getProperty("vault.server.secret_id", null);
		
		log.debug("SERVER_ID_STR = {}", SERVER_ID_STR);
		log.debug("VAULT_SERVER_URL = {}", VAULT_SERVER_URL);
		log.debug("ROLE_ID_STR = {}", ROLE_ID_STR);
		log.debug("SECRET_STR = {}", SECRET_STR);
		
		assertNotNull(ROLE_ID_STR);
		assertNotNull(SERVER_ID_STR);
		assertNotNull(VAULT_SERVER_URL);
		assertNotNull(SECRET_STR);
	}
	
	@Before
	public void setUp() throws Exception {

		properties = new Properties();
		MavenProject mockMavenProject = mock(MavenProject.class);
		when(mockMavenProject.getProperties()).thenReturn(properties);

		Mapping m1 = new Mapping("test_key_1", MAVEN_PROPERTY_VALUE_1);
		Mapping m2 = new Mapping("test_key_2", MAVEN_PROPERTY_VALUE_2);
		List<Mapping> mappings = Arrays.asList(m1, m2);

		final Server s = new Server();
		s.setUrl(VAULT_SERVER_URL);
		s.setId(SERVER_ID_STR);
		s.setPaths(Arrays.asList(new Path(TEST_SECRET_IN_VAULT, mappings)));

		servers = Arrays.asList(s);

		mojo = new VaultPullMojo(mockMavenProject, servers, null);
	}

	@Test
	public void test_that_appRoleFlowWorks() throws VaultException {
		Stopwatch stopwatch = Stopwatch.createStarted();
		VaultConfig vaultConfig = new VaultConfig().address(VAULT_SERVER_URL).sslConfig(new SslConfig().verify(false).build());
		String token = new Auth(vaultConfig).loginByAppRole("approle", ROLE_ID_STR, SECRET_STR).getAuthClientToken();
		log.info("token = {}       request for a token through appRole flow took {} ", token, stopwatch.stop());
		assertNotNull(token);

	}

	/**
	 * Test method for {@link biz.daich.maven.plugins.vault.VaultPullMojo#execute()}.
	 * 
	 * with use of a token
	 * 
	 * @throws MojoFailureException
	 * @throws MojoExecutionException
	 * @throws VaultException
	 * 
	 * 
	 */
	@Test
	public void test_srv_vault_with_token() throws MojoExecutionException, MojoFailureException, VaultException {

		Stopwatch stopwatch = Stopwatch.createStarted();
		String token = new Auth(new VaultConfig().address(VAULT_SERVER_URL).sslConfig(new SslConfig().verify(false).build())).loginByAppRole("approle", ROLE_ID_STR, SECRET_STR).getAuthClientToken();
		log.info("token = {}       request for a token through appRole flow took {} ", token, stopwatch);
		assertNotNull(token);

		properties.setProperty(propName(SERVER_ID_STR, TOKEN), token);

		mojo.execute();

		final String property1 = properties.getProperty(MAVEN_PROPERTY_VALUE_1);
		final String property2 = properties.getProperty(MAVEN_PROPERTY_VALUE_2);

		log.info(" {} = {}", MAVEN_PROPERTY_VALUE_1, property1);
		log.info("{} = {}", MAVEN_PROPERTY_VALUE_2, property2);

		assertTrue(!Strings.isNullOrEmpty(property1));
		assertTrue(!Strings.isNullOrEmpty(property2));
		assertEquals("test_value_1", property1);
		assertEquals("test_value_2", property2);

		log.info("DONE!! in {}", stopwatch);
	}

	/**
	 * Test method for {@link biz.daich.maven.plugins.vault.VaultPullMojo#execute()}.
	 * 
	 * with use of approle flow
	 * 
	 * @throws MojoFailureException
	 * @throws MojoExecutionException
	 * 
	 * 
	 */
	@Test
	public void test_srv_vault_with_approle() throws MojoExecutionException, MojoFailureException {
		final String rolePropName = propName(SERVER_ID_STR, ROLE_ID);
		final String secretPropName = propName(SERVER_ID_STR, SECRET_ID);
		log.debug("setting properties:\n\t{} = {}\n\t{} = {}", rolePropName, ROLE_ID_STR, secretPropName, SECRET_STR);
		properties.setProperty(rolePropName, ROLE_ID_STR);
		properties.setProperty(secretPropName, SECRET_STR);

		mojo.execute();

		final String property1 = properties.getProperty(MAVEN_PROPERTY_VALUE_1);
		final String property2 = properties.getProperty(MAVEN_PROPERTY_VALUE_2);

		log.info("{} = {}", MAVEN_PROPERTY_VALUE_1, property1);
		log.info("{} = {}", MAVEN_PROPERTY_VALUE_2, property2);

		assertTrue(!Strings.isNullOrEmpty(property1));
		assertTrue(!Strings.isNullOrEmpty(property2));

		assertEquals("test_value_1", property1);
		assertEquals("test_value_2", property2);

	}

}
