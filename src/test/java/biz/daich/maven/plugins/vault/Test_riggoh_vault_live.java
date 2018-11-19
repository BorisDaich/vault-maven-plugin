package biz.daich.maven.plugins.vault;

import static biz.daich.maven.plugins.vault.VaultPullMojo.propName;
import static biz.daich.maven.plugins.vault.VaultPullMojo.KEY.ROLE_ID;
import static biz.daich.maven.plugins.vault.VaultPullMojo.KEY.SECRET_ID;
import static biz.daich.maven.plugins.vault.VaultPullMojo.KEY.TOKEN;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.junit.Before;
import org.junit.Test;

import com.bettercloud.vault.SslConfig;
import com.bettercloud.vault.VaultConfig;
import com.bettercloud.vault.VaultException;
import com.bettercloud.vault.api.Auth;
import com.deciphernow.maven.plugins.vault.config.Mapping;
import com.deciphernow.maven.plugins.vault.config.Path;
import com.deciphernow.maven.plugins.vault.config.Server;
import com.google.common.base.Stopwatch;
import com.google.common.base.Strings;

import lombok.extern.slf4j.Slf4j;

/**
 * WARNING!!! THIS TEST runs VS real server and use REAL KEYs
 * 
 * @author boris
 *
 */

@Slf4j
public class Test_riggoh_vault_live {

	private static final String RIGGOH_VAULT = "https://vault.riggoh.name:8200";
	private static final String SECRET_STR = "7a1c0a4e-af70-6cce-9d98-24858a865dc2";
	private static final String ROLE_ID_STR = "maven-role";
	private static final String TOKEN_STR = "maven-role";

	private static final String RIGGOH_SERVER_ID_STR = "riggoh";

	protected VaultPullMojo mojo;
	protected Properties properties;
	private List<Server> servers;

	@Before
	public void setUp() throws Exception {

		properties = new Properties();
		MavenProject mockMavenProject = mock(MavenProject.class);
		when(mockMavenProject.getProperties()).thenReturn(properties);

		// properties.setProperty(propName(RIGGOH_SERVER_ID_STR, ROLE_ID), ROLE_ID_STR);
		// properties.setProperty(propName(RIGGOH_SERVER_ID_STR, SECRET_ID), SECRET_STR);
		// properties.setProperty(propName(RIGGOH_SERVER_ID_STR, TOKEN), TOKEN_STR);

		Mapping m1 = new Mapping("prod", "maven.property.sf.prod");
		Mapping m2 = new Mapping("stage", "maven.property.sf.stage");
		List<Mapping> mappings = Arrays.asList(m1, m2);

		final Server s = new Server();
		s.setUrl(RIGGOH_VAULT);
		s.setId("riggoh");
		s.setPaths(Arrays.asList(new Path("secret/revenova", mappings)));

		servers = Arrays.asList(s);

		mojo = new VaultPullMojo(mockMavenProject, servers, null);
	}

	@Test
	public void test_that_appRoleFlowWorks() throws VaultException {
		Stopwatch stopwatch = Stopwatch.createStarted();
		VaultConfig vaultConfig = new VaultConfig().address(RIGGOH_VAULT).sslConfig(new SslConfig().verify(false).build());
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
	 * 
	 * 
	 */
	@Test
	public void test_riggoh_vault_with_token() throws MojoExecutionException, MojoFailureException {
		properties.setProperty(propName(RIGGOH_SERVER_ID_STR, TOKEN), TOKEN_STR);

		mojo.execute();

		final String propertyProd = properties.getProperty("maven.property.sf.prod");
		final String propertyStage = properties.getProperty("maven.property.sf.stage");

		log.info("maven.property.sf.prod = {}", propertyProd);
		log.info("maven.property.sf.stage = {}", propertyStage);

		assertTrue(!Strings.isNullOrEmpty(propertyProd));
		assertTrue(!Strings.isNullOrEmpty(propertyStage));
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
	public void test_riggoh_vault_with_approle() throws MojoExecutionException, MojoFailureException {
		properties.setProperty(propName(RIGGOH_SERVER_ID_STR, ROLE_ID), ROLE_ID_STR);
		properties.setProperty(propName(RIGGOH_SERVER_ID_STR, SECRET_ID), SECRET_STR);

		mojo.execute();

		final String propertyProd = properties.getProperty("maven.property.sf.prod");
		final String propertyStage = properties.getProperty("maven.property.sf.stage");

		log.info("maven.property.sf.prod = {}", propertyProd);
		log.info("maven.property.sf.stage = {}", propertyStage);

		assertTrue(!Strings.isNullOrEmpty(propertyProd));
		assertTrue(!Strings.isNullOrEmpty(propertyStage));

	}

}
