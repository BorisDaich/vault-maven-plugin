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

package com.deciphernow.maven.plugins.vault.config;

import java.io.File;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a Vault server.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Server implements Serializable {
	/**
	 * unique ID of the server. the default value "default" useful in case there is only one and the default roleId etc that are set in the ~/.m2/settings.xml
	 */
	protected String id = "default";

	/**
	 * values required for the AppRole Auth Method see here https://www.vaultproject.io/docs/auth/approle.html
	 */
	protected String role_id;

	/**
	 * values required for the AppRole Auth Method see here https://www.vaultproject.io/docs/auth/approle.html
	 */
	protected String secret_id;

	/**
	 * the SSL certificate file for this server.
	 */
	protected File sslCertificate;

	/**
	 * a value indicating whether SSL connections are verified for this server.
	 *
	 * {@code true} if the SSL connection should be verified; otherwise, {@code false}
	 */
	protected boolean sslVerify;

	/**
	 * the URL of this server.
	 * */
	protected String url;
	/**
	 * the token used to access this server.
	 **/
	protected String token;
	/**
	 * paths for this server.
	 */
	protected List<Path> paths = Arrays.asList();

	/**
	 * Initializes a new instance of the {@link Server} class.
	 *
	 * @param url
	 *            the URL of the server
	 * @param token
	 *            the token for the server
	 * @param sslVerify
	 *            {@code true} if the SSL connection should be verified; otherwise, {@code false}
	 * @param sslCertificate
	 *            the SSL certificate file or null
	 * @param paths
	 *            the paths for the server
	 */
	public Server(String url, String token, boolean sslVerify, File sslCertificate, List<Path> paths) {
		this.paths = paths;
		this.sslCertificate = sslCertificate;
		this.sslVerify = sslVerify;
		this.token = token;
		this.url = url;
	}


	

}
