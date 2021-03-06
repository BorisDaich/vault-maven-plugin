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

package biz.daich.maven.plugins.vault.config;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a mapping between a Vault key and a Maven property.
 */

/* (non-Javadoc)
 * @see java.lang.Object#toString()
 */

/* (non-Javadoc)
 * @see java.lang.Object#toString()
 */
@Data

/**
 * Instantiates a new mapping.
 */
@NoArgsConstructor

/**
 * Instantiates a new mapping.
 *
 * @param key
 *            the key
 * @param property
 *            the property
 */

/**
 * Instantiates a new mapping.
 *
 * @param key
 *            the key
 * @param property
 *            the property
 */
@AllArgsConstructor
public class Mapping implements Serializable {

	/** The key. */
	private String key;

	/** The property. */
	private String property;

}
