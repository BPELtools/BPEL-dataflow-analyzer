/**
 * 
 */
package de.uni_stuttgart.iaas.bpel_d.test;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import analysis.Activity;

/**
 * @author gao
 *
 */
public class JoinConditionTests {

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}
	
	@Test
	public void OneLinkTrue() {
		assertFalse(Activity.negatesLinkStatus("$l1")); 
	}

	@Test
	public void OneLinkFlase() {
		assertTrue(Activity.negatesLinkStatus("not($l1)")); 
	}

	@Test
	public void NoLink() {
		assertFalse(Activity.negatesLinkStatus(null)); 
	}

	@Test
	public void TwoLinks() {
		assertFalse(Activity.negatesLinkStatus("$l1 and $l2")); 
	}

}
