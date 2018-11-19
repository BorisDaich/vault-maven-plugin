package biz.daich.maven.plugins.vault;

import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.matches;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.regex.Pattern;

import org.junit.Ignore;
import org.junit.Test;

/**
 * posted as a question to stackOverflow
 * https://stackoverflow.com/questions/53366308/explain-the-reason-for-this-behaviour-of-mockito-matchesstr-vs-samestr
 */
public class Test_mockito_strange {

	public static class A {
		protected B b = new B();

		public B read(String s) {
			return b;
		}
	}

	public static class B {
		String[] getData() {
			return new String[] {
					"aa"
			};
		}
	}

	@Test
	public void test_0() {

		assertTrue(Pattern.matches("00", "00"));
		assertTrue(Pattern.matches("11", "11"));
	}

	/// I do not understand why this fails!!!
	@Test
	//@Ignore // ignored because fails and I do not understand why!!!??
	public void test_1() {
		A a = mock(A.class, RETURNS_DEEP_STUBS);
		String[] m1 = new String[] {
				"bb"
		};
		String[] m2 = new String[] {
				"cc"
		};
		String[] m3 = new String[] {
				"dd"
		};

		when(a.read(anyString()).getData()).thenReturn(m1);
		when(a.read(matches("00")).getData()).thenReturn(m2);
		when(a.read(matches("11")).getData()).thenReturn(m3);

		assertTrue("cc".equals(a.read("00").getData()[0]));
		assertTrue("dd".equals(a.read("11").getData()[0]));
		assertTrue("bb".equals(a.read("33").getData()[0]));
	}

	@Test
	public void test_2() {
		A a = mock(A.class, RETURNS_DEEP_STUBS);
		String[] m1 = new String[] {
				"bb"
		};
		String[] m2 = new String[] {
				"cc"
		};
		String[] m3 = new String[] {
				"dd"
		};

		when(a.read(anyString()).getData()).thenReturn(m1);
		when(a.read(same("00")).getData()).thenReturn(m2);
		when(a.read(same("11")).getData()).thenReturn(m3);

		assertTrue("cc".equals(a.read("00").getData()[0]));
		assertTrue("dd".equals(a.read("11").getData()[0]));
		assertTrue("bb".equals(a.read("33").getData()[0]));
	}
}
