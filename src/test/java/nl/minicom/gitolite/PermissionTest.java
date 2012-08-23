package nl.minicom.gitolite;

import java.util.Iterator;
import java.util.SortedSet;

import junit.framework.Assert;

import nl.minicom.gitolite.Permission;

import org.junit.Test;

import com.google.common.collect.Sets;

public class PermissionTest {
	
	@Test
	public void testComparatorForInOrderPermissions() {
		SortedSet<Permission> rights = Sets.newTreeSet(Permission.SORT_ON_ORDINAL);
		rights.add(Permission.ALL);
		rights.add(Permission.READ_ONLY);
		
		Iterator<Permission> iter = rights.iterator();
		Assert.assertEquals(Permission.ALL, iter.next());
		Assert.assertEquals(Permission.READ_ONLY, iter.next());
	}
	
	@Test
	public void testComparatorForPermissionsInReversedOrder() {
		SortedSet<Permission> rights = Sets.newTreeSet(Permission.SORT_ON_ORDINAL);
		rights.add(Permission.READ_ONLY);
		rights.add(Permission.ALL);
		
		Iterator<Permission> iter = rights.iterator();
		Assert.assertEquals(Permission.ALL, iter.next());
		Assert.assertEquals(Permission.READ_ONLY, iter.next());
	}
	
	@Test
	public void testGetByNameMethodWhenPermissionIsNotPresent() {
		Assert.assertEquals(null, Permission.getByName("permission-which-doesnt-exist"));
	}
	
	@Test
	public void testGetByNameMethodWhenPermissionIsPresent() {
		Assert.assertEquals(Permission.ALL, Permission.getByName("RW+"));
	}
	
	@Test(expected = NullPointerException.class)
	public void testThatGetByNameMethodThrowsExceptionOnInputNull() {
		Permission.getByName(null);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testThatGetByNameMethodThrowsExceptionsOnEmptyInput() {
		Permission.getByName("");
	}
	
}
