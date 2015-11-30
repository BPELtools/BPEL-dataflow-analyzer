package de.uni_stuttgart.iaas.bpel_d.test;

import static org.junit.Assert.*;

import org.junit.Test;

import de.uni_stuttgart.iaas.bpel_d.algorithm.analysis.Activity;


public class JoinConditionTests {

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
