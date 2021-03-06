package nl.minicom.gitolite.manager.models;

import java.util.Collection;
import java.util.Iterator;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Sets;

public class RepositoryTest {

	private static final String REPOSITORY_NAME = "test-repo";

	private Config config;
	private User user1;
	private User user2;

	@Before
	public void setUp() {
		config = new Config(new Recorder());
		user1 = new User("test-user-1", new Recorder());
		user2 = new User("test-user-2", new Recorder());
	}

	@Test
	public void testConstructorWithValidInputs() {
		Repository repository = config.createRepository(REPOSITORY_NAME);
		Assert.assertEquals(REPOSITORY_NAME, repository.getName());
	}

	@Test(expected = NullPointerException.class)
	public void testConstructorWithNullAsName() {
		new Repository(null, new Recorder());
	}
	
	@Test(expected = NullPointerException.class)
	public void testConstructorWithNullAsRecorder() {
		new Repository(REPOSITORY_NAME, null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testConstructorWithEmptyName() {
		new Repository("", new Recorder());
	}

	@Test
	public void testThatNewRepositoryHasNoPermissions() {
		Repository repository = new Repository(REPOSITORY_NAME, new Recorder());
		Assert.assertEquals(0, repository.getPermissions().size());
	}

	@Test
	public void testAddingUserWithReadOnlyPermission() {
		Repository repository = new Repository(REPOSITORY_NAME, new Recorder());
		repository.setPermission(user1, Permission.READ_ONLY);

		Assert.assertEquals(Sets.newHashSet(Permission.READ_ONLY), repository.getPermissions().keySet());
		Collection<Identifiable> entitiesWithReadOnlyPermission = repository.getPermissions().get(Permission.READ_ONLY);
		Assert.assertEquals(Sets.newHashSet(user1), Sets.newHashSet(entitiesWithReadOnlyPermission));
	}

	@Test
	public void testAddingTwoUsersWithReadOnlyPermission() {
		Repository repository = new Repository(REPOSITORY_NAME, new Recorder());
		repository.setPermission(user2, Permission.READ_ONLY);
		repository.setPermission(user1, Permission.READ_ONLY);

		Assert.assertEquals(Sets.newHashSet(Permission.READ_ONLY), repository.getPermissions().keySet());
		Iterator<Identifiable> iter = repository.getPermissions().get(Permission.READ_ONLY).iterator();
		Assert.assertEquals(user1, iter.next());
		Assert.assertEquals(user2, iter.next());
	}

	@Test
	public void testAddingTwoUsersWithDifferentPermission() {
		Repository repository = new Repository(REPOSITORY_NAME, new Recorder());
		repository.setPermission(user1, Permission.READ_ONLY);
		repository.setPermission(user2, Permission.ALL);

		Iterator<Permission> iter = repository.getPermissions().keySet().iterator();
		Assert.assertEquals(Permission.ALL, iter.next());
		Assert.assertEquals(Permission.READ_ONLY, iter.next());
	}

	@Test(expected = NullPointerException.class)
	public void testThatSettingPermissionWhenUserIsNullExceptionIsThrown() {
		User user = null;
		Repository repository = new Repository(REPOSITORY_NAME, new Recorder());
		repository.setPermission(user, Permission.ALL);
	}
	
	@Test(expected = NullPointerException.class)
	public void testThatSettingPermissionWhenGroupIsNullExceptionIsThrown() {
		Group group = null;
		Repository repository = new Repository(REPOSITORY_NAME, new Recorder());
		repository.setPermission(group, Permission.ALL);
	}

	@Test(expected = NullPointerException.class)
	public void testThatSettingPermissionWhenPermissionIsNullExceptionIsThrown() {
		Repository repository = new Repository(REPOSITORY_NAME, new Recorder());
		repository.setPermission(user1, null);
	}

	@Test
	public void testEqualsMethod() {
		EqualsVerifier.forClass(Repository.class).suppress(Warning.STRICT_INHERITANCE).verify();
	}

}
