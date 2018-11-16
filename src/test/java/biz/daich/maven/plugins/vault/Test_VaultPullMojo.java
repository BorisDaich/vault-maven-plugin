/**
 * 
 */
package biz.daich.maven.plugins.vault;

import static biz.daich.maven.plugins.vault.VaultPullMojo.propName;
import static biz.daich.maven.plugins.vault.VaultPullMojo.KEY.ROLE_ID;
import static biz.daich.maven.plugins.vault.VaultPullMojo.KEY.SECRET_ID;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
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

import lombok.extern.slf4j.Slf4j;

/**
 * WARNING!!! THIS TEST runs VS real server and use REAL KEYs
 * 
 * @author boris
 *
 */
@Slf4j
public class Test_VaultPullMojo {

	private static final String RIGGOH_VAULT = "https://vault.riggoh.name:8200";
	private static final String SECRET_STR = "7a1c0a4e-af70-6cce-9d98-24858a865dc2";
	private static final String ROLE_ID_STR = "maven-role";
	protected VaultPullMojo mojo;
	protected Properties props;
	private List<Server> servers;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		mojo = new VaultPullMojo();
		props = new Properties();

		props.setProperty(propName("riggoh", ROLE_ID), ROLE_ID_STR);
		props.setProperty(propName("riggoh", SECRET_ID), SECRET_STR);
		mojo.project = new MavenProject() {
			@Override
			public Properties getProperties() {
				return props;
			}
		};

		servers = new ArrayList<>();
		{
			Server s = new Server();
			s.setUrl(RIGGOH_VAULT);
			s.setId("riggoh");
			Mapping m1 = new Mapping("prod", "maven.property.sf.prod");
			Mapping m2 = new Mapping("stage", "maven.property.sf.stage");
			List<Mapping> mappings = Arrays.asList(m1, m2);
			Path p1 = new Path("secret/revenova", mappings);
			List<Path> paths = Arrays.asList(p1);
			s.setPaths(paths);
			// s.setRole_id("maven-role");
			// s.setSecret_id("7a1c0a4e-af70-6cce-9d98-24858a865dc2");

			servers.add(s);

		}

		mojo.servers = servers;
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
	 * @throws MojoFailureException
	 * @throws MojoExecutionException
	 * 
	 * 
	 */
	@Test
	public void test_Execute_0() throws MojoExecutionException, MojoFailureException {
		assertNotNull(mojo);
		mojo.execute();

		String p1 = props.getProperty("maven.property.sf.prod");
		log.info("maven.property.sf.prod = {}", p1);

		String p2 = props.getProperty("maven.property.sf.stage");
		log.info("maven.property.sf.stage = {}", p2);

		assertNotNull(p1);
		assertNotNull(p2);

	}

}
