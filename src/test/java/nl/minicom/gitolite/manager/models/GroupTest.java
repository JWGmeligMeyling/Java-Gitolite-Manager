package nl.minicom.gitolite.manager.models;

import java.util.Iterator;
import java.util.Set;
import java.util.SortedSet;

import nl.jqno.equalsverifier.EqualsVerifier;

import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.Sets;

public class GroupTest {

	@Test
	public void testConstructorWithValidInputs() {
		Group group = new Group("@test-group");
		Assert.assertEquals("@test-group", group.getName());
	}

	@Test(expected = NullPointerException.class)
	public void testConstructorWithNullAsName() {
		new Group(null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testConstructorWithEmptyName() {
		new Group("");
	}

	@Test(expected = NullPointerException.class)
	public void testAddingNullGroupToGroup() {
		Group nullGroup = null;
		new Group("@parent").add(nullGroup);
	}
	
	@Test(expected = NullPointerException.class)
	public void testAddingNullUserToGroup() {
		User nullUser = null;
		new Group("@parent").add(nullUser);
	}

	@Test
	public void testAddingUserToGroup() {
		Group parent = new Group("@parent");
		User user = new User("test-user");
		parent.add(user);

		Assert.assertEquals(Sets.newHashSet(user), parent.getUsers());
	}

	@Test
	public void testAddingGroupToGroup() {
		Group parent = new Group("@parent");
		Group child = new Group("@child");
		parent.add(child);

		Assert.assertEquals(Sets.newHashSet(child), parent.getGroups());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testAddingSameGroupTwiceToGroup() {
		Group parent = new Group("@parent");
		Group child = new Group("@child");
		parent.add(child);
		parent.add(child);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testAddingUserToAllGroupThrowsException() {
		new Group("@all").add(new User("test-user"));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testAddingGroupToAllGroupThrowsException() {
		new Group("@all").add(new Group("@test-group"));
	}

	@Test(expected = NullPointerException.class)
	public void testThatContainsGroupMethodThrowsExceptionOnNullAsInput() {
		new Group("@parent").containsGroup(null);
	}

	@Test
	public void testContainsGroupMethodWithFirstDegreeChild() {
		Group parent = new Group("@parent");
		Group child = new Group("@child");
		parent.add(child);

		Assert.assertTrue(parent.containsGroup(child));
	}

	@Test
	public void testContainsGroupMethodWithSecondDegreeChild() {
		Group parent = new Group("@parent");
		Group intermediate = new Group("@intermediate");
		Group child = new Group("@child");

		parent.add(intermediate);
		intermediate.add(child);

		Assert.assertTrue(parent.containsGroup(child));
	}

	@Test
	public void testContainsGroupMethodWhenOtherGroupIsNoChild() {
		Group parent = new Group("@parent");
		Group other = new Group("@other");

		Assert.assertFalse(parent.containsGroup(other));
	}

	@Test
	public void testContainsGroupMethodWhenOnlyUsersArePresent() {
		Group parent = new Group("@parent");
		parent.add(new User("test-user"));

		Assert.assertFalse(parent.containsGroup(new Group("@other")));
	}

	@Test
	public void testContainsGroupsMethodWhenGroupHasChildren() {
		Group parent = new Group("@parent");
		Group child = new Group("@child");
		parent.add(child);

		Assert.assertTrue(parent.containsGroup(child));
	}

	@Test
	public void testContainsGroupsMethodWhenGroupOnlyContainsUsers() {
		Group parent = new Group("@parent");
		User user = new User("test-user");
		parent.add(user);
		
		Assert.assertEquals(1, parent.getUsers().size());
		Assert.assertTrue(parent.containsUser(user));
	}

	@Test
	public void testGetMembersMethod() {
		Group parent = new Group("@parent");
		Group child = new Group("@child");
		User user = new User("test-user");
		parent.add(user);
		parent.add(child);
		
		Set<Identifiable> members = Sets.newTreeSet(Identifiable.SORT_BY_TYPE_AND_NAME);
		members.add(child);
		members.add(user);

		Assert.assertEquals(members, parent.getAllMembers());
	}

	/**
	 * Test case tree:
	 * 
	 * <pre>
	 *          B
	 *        /
	 *      --&gt;     A
	 *        \
	 *          C - D
	 * </pre>
	 * 
	 * Expected ordering (bottom-up, alphabetical):<br>
	 * D, B, C, A
	 */
	@Test
	public void testGroupOrdering() {
		Group a = new Group("@a");
		Group b = new Group("@b");
		Group c = new Group("@c");
		Group d = new Group("@d");

		a.add(b);
		a.add(c);
		c.add(d);

		SortedSet<Group> groups = Sets.newTreeSet(Group.SORT_BY_NAME);
		groups.add(a);
		groups.add(b);
		groups.add(c);
		groups.add(d);

		Iterator<Group> iter = groups.iterator();
		Assert.assertEquals(a, iter.next());
		Assert.assertEquals(b, iter.next());
		Assert.assertEquals(c, iter.next());
		Assert.assertEquals(d, iter.next());
	}

	@Test
	public void testEqualsAndHashCode() {
		EqualsVerifier.forClass(Group.class).verify();
	}

}
