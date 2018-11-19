/*
 * Copyright 2017 Decipher Technology Studios LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package biz.daich.maven.plugins.vault;

import static biz.daich.maven.plugins.vault.VaultPullMojo.KEY.ROLE_ID;
import static biz.daich.maven.plugins.vault.VaultPullMojo.KEY.SECRET_ID;
import static biz.daich.maven.plugins.vault.VaultPullMojo.KEY.TOKEN;
import static com.google.common.base.Strings.isNullOrEmpty;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Properties;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import com.bettercloud.vault.SslConfig;
import com.bettercloud.vault.Vault;
import com.bettercloud.vault.VaultConfig;
import com.bettercloud.vault.VaultException;
import com.deciphernow.maven.plugins.vault.config.Mapping;
import com.deciphernow.maven.plugins.vault.config.Path;
import com.deciphernow.maven.plugins.vault.config.Server;
import com.google.common.base.Stopwatch;
import com.google.common.base.Strings;

import lombok.Getter;
import lombok.Setter;

/**
 * Provides a Mojo that pulls values from Vault and sets Maven properties.
 */
@Mojo(name = "pull", defaultPhase = LifecyclePhase.INITIALIZE)
public class VaultPullMojo extends AbstractMojo {

	// getter is for testing convenience
	@Getter
	@Parameter(defaultValue = "${project}", readonly = true, required = true)
	protected MavenProject project;

	// getter is for testing convenience
	@Getter
	@Parameter(required = true)
	protected List<Server> servers;

	protected Properties projectProperties;

	/**
	 * factory interface for more convenient testing.
	 */
	public interface IVaultFactory {

		public Vault vault(VaultConfig vc);
	}

	@Setter
	@Getter
	protected IVaultFactory vaultFactory = Vault::new;

	/**
	 * Defines the timeout when opening a connection with Vault.
	 */
	public static final int OPEN_TIMEOUT_SEC = 5;

	/**
	 * Defines the timeout when reading data from Vault.
	 */
	public static final int READ_TIMEOUT_SEC = 30;

	static final String SERVER_DEFAULT_ID = "default";

	public enum KEY {
		TOKEN("token"), ROLE_ID("role_id"), SECRET_ID("secret_id");
		public final String value;

		private KEY(String s) {
			value = s;
		}
	}

	public VaultPullMojo() {
		super();
	}

	/**
	 * c'tor for testing
	 * 
	 * will set the IVaultFactory vaultFactory only if argument is not null
	 */
	protected VaultPullMojo(MavenProject project, List<Server> servers, IVaultFactory vaultFactory) {
		super();
		this.project = project;
		this.servers = servers;
		if (vaultFactory != null)
			this.vaultFactory = vaultFactory;
	}

	/**
	 * Executes this Mojo which pulls project property values from Vault.
	 *
	 * @throws MojoExecutionException
	 *             if an exception is thrown based upon the project configuration
	 * @throws MojoFailureException
	 */
	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		projectProperties = project.getProperties();

		int defaultIdCount = 0;
		for (Server s : servers) {

			String id = s.getId();

			if (Strings.isNullOrEmpty(id))
				s.setId(id = SERVER_DEFAULT_ID);

			if (SERVER_DEFAULT_ID.equalsIgnoreCase(id))
				defaultIdCount++;

			if (defaultIdCount > 1)
				getLog().warn("There are more than one server without id or with id=" + SERVER_DEFAULT_ID + " this might cause a problem!");

			String token = s.getToken();
			String role_id = s.getRole_id();
			String secret_id = s.getSecret_id();

			// WARN about bad practice
			if (!Strings.isNullOrEmpty(role_id) || !Strings.isNullOrEmpty(secret_id) || !Strings.isNullOrEmpty(token)) {
				getLog().warn("Token, SecretId, RoleId for Vault server MUST NOT be set in the pom!! They must be defined in your ~/.m2/settings.xml as property of the form "
						+ "<propoerties>\n"
						+ "\t<" + propName(id, TOKEN) + ">YOUR_TOKEN</" + propName(id, TOKEN) + ">\n"
						+ "OR\n"
						+ "\t<" + propName(id, ROLE_ID) + ">YOUR_ROLE_ID</" + propName(id, ROLE_ID) + ">\n"
						+ "\t<" + propName(id, SECRET_ID) + ">YOUR_SECRET_ID</" + propName(id, SECRET_ID) + ">\n"
						+ "</propoerties>\n");
			}
			// try to get the values from properties if not set

			// token

			s.setToken(getValueFromPropIfMissing(id, token, TOKEN));
			// role_id
			s.setRole_id(getValueFromPropIfMissing(id, role_id, ROLE_ID));
			// secret_id
			s.setSecret_id(getValueFromPropIfMissing(id, secret_id, SECRET_ID));

			// now for every server that do pull values and set properties
			pullServer(s, projectProperties);
		}
	}

	/**
	 * pull information from a single Vault server
	 * if given a token - try it first
	 * -- failed? try to get a token through role_id/secret_id auth flow
	 * no token? - try to get a token through role_id/secret_id auth flow
	 * 
	 * if failed - than the plugin failed
	 * 
	 * @throws MojoExecutionException
	 * 
	 * @throws VaultException
	 * 
	 */
	protected void pullServer(Server server, Properties properties) throws MojoFailureException, MojoExecutionException {

		boolean isRoleUsed = false;
		VaultConfig vaultConfig;
		try {
			vaultConfig = new VaultConfig()
					.address(server.getUrl())
					.openTimeout(OPEN_TIMEOUT_SEC)
					.readTimeout(READ_TIMEOUT_SEC)
					.token(server.getToken())
					.sslConfig(
							new SslConfig()
									.verify(false)
									.build());
		} catch (VaultException e1) {
			throw new MojoExecutionException("FAILED to build a VaultConfig", e1);
		}
		while (true) {
			if (isNullOrEmpty(server.getToken()))
				isRoleUsed = updateMissingTokenUsingAppRoleAuthFlowOrThrow(server, vaultConfig);
			final Vault vault = vaultFactory.vault(vaultConfig);
			// try as is
			try {
				pullVault(vault, server, properties);
				return;
				// if failed
			} catch (VaultException e) { // we have failed to access the vault
				if (isRoleUsed) // we already tried role_id/secret_id to get the token
				{
					String errorMsg = "Failed to access the Vault server [" + server.getId() + "] with url: [" + server.getUrl() + "] with the role_id/secret_id. Reason: " + e.getMessage();
					getLog().error(errorMsg);
					throw new MojoFailureException(errorMsg);
				} else {
					getLog().warn("Failed to access the Vault server [" + server.getId() + "] with url: [" + server.getUrl() + "] using provided token will try with the role_id/secret_id if present");
					vaultConfig.token(null);
					server.setToken(null);
				}
			} catch (NoSuchElementException x) {
				getLog().warn(x.getMessage() + "\n this might fail the build down the road.");
			}
		}
	}

	/**
	 * @return true if token retrieval and update was successful
	 */
	private boolean updateMissingTokenUsingAppRoleAuthFlowOrThrow(Server server, VaultConfig vaultConfig) throws MojoFailureException {
		// if we have role_id and secred_id we can try to get a token
		if (!isNullOrEmpty(server.getRole_id()) && !isNullOrEmpty(server.getSecret_id())) {
			try {
				getLog().debug("no token for server [" + server.getId() + "] but there is role_id and secret_id. Attempting to get token");
				// try getting a token by role_id & secret
				Stopwatch stopwatch = Stopwatch.createStarted();
				String token = vaultFactory.vault(vaultConfig).auth().loginByAppRole("approle", server.getRole_id(), server.getSecret_id()).getAuthClientToken();
				getLog().debug("request for a token through appRole flow took " + stopwatch.stop());
				server.setToken(token);
				vaultConfig.token(token);
				return true;
			} catch (VaultException e) {
				String errorMsg = "There is no token and failed to get a token using app role_id [" + server.getRole_id() + "] for the server [" + server.getId() + "] reason:\n"
						+ e.getMessage();
				getLog().error(errorMsg, e);
				// there was no token and we failed to get one - there is nothing we can do here
				throw new MojoFailureException(errorMsg);
			}
		} else { // no token and no role_id and secred_id there is nothing we can do here...
			String errorMsg = "There is no token or role_id/secret_id for the server [" + server.getId() + "] with url: [" + server.getUrl() + "]  Nothing we can do!";
			getLog().error(errorMsg);
			throw new MojoFailureException(errorMsg);
		}
	}

	protected void pullVault(Vault vault, Server server, Properties properties) throws VaultException {
		for (Path path : server.getPaths()) {
			Map<String, String> secrets = vault.logical().read(path.getName()).getData();
			for (Mapping mapping : path.getMappings()) {
				if (!secrets.containsKey(mapping.getKey())) {
					String message = String.format("No value found in path %s for key %s", path.getName(), mapping.getKey());
					throw new NoSuchElementException(message);
				}
				properties.setProperty(mapping.getProperty(), secrets.get(mapping.getKey()));
			}
		}
	}

	/**
	 * if currentValue is null or empty string will try to find it in project properties
	 * the property name of the form
	 * <br>
	 * "vault.server.<SERVER_ID>.[role_id|secret_id|token]"
	 * 
	 */
	protected String getValueFromPropIfMissing(String serverId, String currentValue, KEY valueType) {
		String propName = null;
		Object prop = null;
		if (Strings.isNullOrEmpty(currentValue)) {
			propName = propName(serverId, valueType);
			prop = projectProperties.get(propName);
			if (prop != null) {
				currentValue = (String) prop;
			}
			getLog().debug("try to find " + valueType + " for server = " + serverId + " in property " + propName + " " + (Strings.isNullOrEmpty(currentValue) ? "NOT FOUND" : "FOUND"));
		} else {
			getLog().debug("Token for server " + serverId + " - PRESENT");
		}
		return currentValue;
	}

	/**
	 * build the property name of the form
	 * "vault.server.<SERVER_ID>.[role_id|secret_id|token]"
	 */
	public static String propName(String serverId, KEY type) {
		return "vault.server." + serverId + "." + type.value;
	}

}
