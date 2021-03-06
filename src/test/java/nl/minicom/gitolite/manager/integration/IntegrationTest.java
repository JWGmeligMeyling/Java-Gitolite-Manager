package nl.minicom.gitolite.manager.integration;

import com.google.common.io.Files;
import nl.minicom.gitolite.manager.exceptions.ModificationException;
import nl.minicom.gitolite.manager.git.GitManager;
import nl.minicom.gitolite.manager.git.JGitManager;
import nl.minicom.gitolite.manager.git.KeyGenerator;
import nl.minicom.gitolite.manager.models.Config;
import nl.minicom.gitolite.manager.models.ConfigManager;
import nl.minicom.gitolite.manager.models.Group;
import nl.minicom.gitolite.manager.models.Repository;
import nl.minicom.gitolite.manager.models.User;

import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.common.base.Strings;

import java.io.File;
import java.io.FileWriter;
import java.util.concurrent.Future;

public class IntegrationTest {

	private static String gitUri;
	private static String adminUsername;

	@BeforeClass
	public static void beforeClass() {
		Assume.assumeTrue(Strings.isNullOrEmpty(System.getProperty("skipIntegrationTests")));
		gitUri = System.getProperty("gitUri", "ssh://git@localhost:2222/gitolite-admin");
		adminUsername = System.getProperty("gitAdmin", "git");
	}

	private ConfigManager manager;

	@Before
	public void setUp() throws Exception {
		manager = ConfigManager.create(gitUri);
		clearEverything();
	}

	@After
	public void tearDown() throws Exception {
		clearEverything();
	}
	
	private void clearEverything() throws Exception {
		if(!Strings.isNullOrEmpty(System.getProperty("skipIntegrationTests"))) {
			return;
		}

		Config config = manager.get();

		for (User user : config.getUsers()) {
			if (!adminUsername.equals(user.getName())) {
				config.removeUser(user);
			}
		}
		
		for (Group group : config.getGroups()) {
			config.removeGroup(group);
		}
		
		for (Repository repo : config.getRepositories()) {
			if (!"gitolite-admin".equals(repo.getName())) {
				config.removeRepository(repo);
			}
		}
		
		manager.apply(config);
	}
	
	@Test
	public void testSequentialRepositoryModification() throws Exception {
		Config config = manager.get();
		config.createRepository("test-repo");
		manager.apply(config);
		
		config = manager.get();
		Repository repository = config.getRepository("test-repo");
		config.removeRepository(repository);
		manager.apply(config);
		
		config = manager.get();
		Assert.assertNull(config.getRepository("test-repo"));
		assertConfigPushedCorrectly(config);
	}
	
	@Test(expected = ModificationException.class)
	public void testConcurrentRepositoryCreation() throws Exception {
		Config config1 = manager.get();
		Config config2 = manager.get();
		
		config1.createRepository("test-repo");
		config2.createRepository("test-repo");

		Future<?> future = manager.applyAsync(config1);
		manager.apply(config2);
		future.get();
	}
	
	@Test(expected = ModificationException.class)
	public void testConcurrentRepositoryRemoval() throws Exception {
		Config config = manager.get();
		config.createRepository("test-repo");
		manager.apply(config);
		
		Config config1 = manager.get();
		Config config2 = manager.get();
		
		config1.removeRepository(config1.getRepository("test-repo"));
		config2.removeRepository(config2.getRepository("test-repo"));
		
		manager.applyAsync(config1);
		manager.apply(config2);
	}
	
	@Test
	public void testSequentialGroupModification() throws Exception {
		Config config = manager.get();
		config.createGroup("@test-group").add(config.getUser(adminUsername));
		manager.apply(config);
		
		config = manager.get();
		config.removeGroup(config.getGroup("@test-group"));
		manager.apply(config);
		
		config = manager.get();
		Assert.assertNull(config.getGroup("@test-group"));
		assertConfigPushedCorrectly(config);
	}
	
	@Test(expected = ModificationException.class)
	public void testConcurrentGroupCreation() throws Exception {
		Config config1 = manager.get();
		Config config2 = manager.get();
		
		config1.createGroup("@test-group").add(config1.getUser(adminUsername));
		config2.createGroup("@test-group").add(config2.getUser(adminUsername));

		Future<?> future = manager.applyAsync(config1);
		manager.apply(config2);
		future.get();
	}
	
	@Test(expected = ModificationException.class)
	public void testConcurrentGroupRemoval() throws Exception {
		Config config = manager.get();
		config.createGroup("@test-group").add(config.getUser(adminUsername));
		manager.apply(config);
		
		Config config1 = manager.get();
		Config config2 = manager.get();
		
		config1.removeGroup(config1.getGroup("@test-group"));
		config2.removeGroup(config2.getGroup("@test-group"));

		Future<?> future = manager.applyAsync(config1);
		manager.apply(config2);
		future.get();
	}
	
	@Test
	public void testSequentialUserModification() throws Exception {
		Config config = manager.get();
		config.createUser("test-user").setKey("key", KeyGenerator.generateRandomPublicKey());
		manager.apply(config);
		
		config = manager.get();
		config.removeUser(config.getUser("test-user"));
		manager.apply(config);
		
		config = manager.get();
		Assert.assertNull(config.getUser("test-user"));
		assertConfigPushedCorrectly(config);
	}

	@Test
	public void testMoreUserModification() throws Exception {
		Config config = manager.get();
		config.createUser("test-user").setKey("key", KeyGenerator.generateRandomPublicKey());
		manager.apply(config);

		config = manager.get();
		config.removeUser(config.getUser("test-user"));
		manager.apply(config);

		config = manager.get();
		config.createUser("test-user2").setKey("key", KeyGenerator.generateRandomPublicKey());
		manager.apply(config);

		config = manager.get();
		Assert.assertNull(config.getUser("test-user"));
		Assert.assertNotNull(config.getUser("test-user2"));
		assertConfigPushedCorrectly(config);
	}

	public static void assertConfigPushedCorrectly(Config config) throws Exception {
		Assert.assertEquals(config, ConfigManager.create(gitUri).get());
	}
	
	@Test(expected = ModificationException.class)
	public void testConcurrentUserCreation() throws Exception {
		Config config1 = manager.get();
		Config config2 = manager.get();

		config1.createUser("test-user").setKey("key", KeyGenerator.generateRandomPublicKey());
		config2.createUser("test-user").setKey("key", KeyGenerator.generateRandomPublicKey());

		Future<?> future = manager.applyAsync(config1);
		manager.apply(config2);
		future.get();
	}
	
	@Test(expected = ModificationException.class)
	public void testConcurrentUserRemoval() throws Exception {
		Config config = manager.get();
		config.createUser("test-user").setKey("key", KeyGenerator.generateRandomPublicKey());
		manager.apply(config);
		
		Config config1 = manager.get();
		Config config2 = manager.get();

		config1.removeUser(config.getUser("test-user"));
		config2.removeUser(config.getUser("test-user"));
		
		Future<?> future = manager.applyAsync(config1);
		manager.apply(config2);
		future.get();
	}

	@Test(expected = Exception.class)
	public void testApplyConfigOnAheadRemote() throws Exception {
		Config config = manager.get();

		File copyWorkingDirectory = Files.createTempDir();
		GitManager gitManager = new JGitManager(copyWorkingDirectory, null);
		gitManager.clone(gitUri);

		FileWriter writer = new FileWriter(new File(copyWorkingDirectory, "test.txt"));
		writer.write("Hello world");
		writer.close();

		gitManager.commitChanges();
		gitManager.push();
		// The remote is now ahead

		config.createRepository("test-repo");
		manager.apply(config);
	}
	
}
