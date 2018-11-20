# vault-maven-plugin

This Maven plugin supports pulling Maven project properties from secrets stored in [HashiCorp](https://www.hashicorp.com) [Vault](https://www.vaultproject.io/).

This is deeply reworked fork of the https://github.com/DecipherNow/vault-maven-plugin 

<u>***Important changes:***</u>

Support for the Vault's ***approle*** flow see here https://www.vaultproject.io/docs/auth/approle.html

Support for <u>`token`</u>, <u>`role_id`</u>, <u>`secret_id</u>` to be configured as properties in the user's `~/.m2/settings.xml`

So no credentials need to be checked into the source control.


## Usage

To include the vault-maven-plugin in your project add the following plugin to your `pom.xml` file:

```xml
<build>
    <plugins>
        <plugin>
            <groupId>biz.daich</groupId>
            <artifactId>vault-maven-plugin</artifactId>
            <version>2.0.0</version>
        </plugin>
    </plugins>
</build>
```

### Pulling Secrets

In order to pull secrets you must add an execution to the plugin.  The following execution will pull secrets from `secret/user` path on the Vault server`https://vault.example.com` referenced here as `vault_78`.  In particular, this configuration will set the value of the `${my.secret.password}` and `${my.secret.username}` Maven properties to the secrets `${vault.password}` and `${vault.username}` respectively.

Plugin assumes that in your `~/.m2/settings.xml` you have the credentials to access the Vault server 

it can be a `token` or a `role_id/secret_id` pair 

the properties naming convention in the settings.xml 

`vault.server.<SERVER_ID>.[token|role_id|secret_id]`

so for server with ID **vault_78** it will be:

```xml
<properties>
	<vault.server.vault_78.token><THE_TOKEN></vault.server.vault_78.token>
	<vault.server.vault_78.role_id><THE_ROLE_ID></vault.server.vault_78.role_id>
	<vault.server.vault_78.secret_id><THE_SECRET_ID></vault.server.vault_78.secret_id> 
</properties>
```

plugin will first look for token if not found or failed to login will go for the `role_id/secret_id` pair

```xml
<build>
    <plugins>
        <plugin>
            <groupId>com.deciphernow</groupId>
            <artifactId>vault-maven-plugin</artifactId>
            <version>2.0.0</version>
            <executions>
                <execution>
                    <id>pull</id>
                    <phase>initialize</phase>
                    <goals>
                        <goal>pull</goal>
                    </goals>
                    <configuration>
                        <servers>
                            <server>
                                <id>vault_78</id>
                                <url>https://vault.example.com</url>             
                                <paths>
                                    <path>
                                        <name>secret/user</name>
                                        <mappings>
                                            <mapping>
                                                <key>vault.password</key>
                                                <property>my.secret.password</property>
                                            </mapping>
                                            <mapping>
                                                <key>vault.username</key>
                                                <property>my.secret.username</property>
                                            </mapping>
                                        </mappings>
                                    </path>
                                </paths>
                            </server>
                        </servers>
                    </configuration>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
```

Notes:

- `token` or a `role_id/secret_id` pair can be part of the server configuration in the pom.xml in form of 

```xml
 <server>
	...
	<token>ttttt</token>
	<secret_id>aaaaaa</secret_id>
	<role_id>bbbbb</role_id>
	....
</server>
```

but this is not recommended. 

- The execution will fail if neither is found for a server in configuration or properties.

## Building

This build uses standard Maven build commands but assumes that the following are installed and configured locally:

- Java (1.8 or greater)

- Maven (3.5 or greater)

