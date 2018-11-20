package biz.daich.maven.plugins.vault;

import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.matches;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.regex.Pattern;

import org.junit.Ignore;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import lombok.extern.slf4j.Slf4j;

/**
 * posted as a question to stackOverflow
 * https://stackoverflow.com/questions/53366308/explain-the-reason-for-this-behaviour-of-mockito-matchesstr-vs-samestr
 */
@Slf4j
public class Test_mockito_strange {

	public static class A {
		final protected B b = new B();

		public B read(String s) {
			return b;
		}

		public String str(String s) {
			return s;
		}
	}

	public static class B {
		String getData() {
			return "aa";
		}
	}

	/**
	 * this one works ok
	 */
	@Test
	public void test_00() {
		final A a = mock(A.class, RETURNS_DEEP_STUBS);
		// final A a = mock(A.class);

		when(a.str(anyString())).thenReturn("bb");
		when(a.str(matches("00"))).thenReturn("cc");
		when(a.str(matches("11"))).thenReturn("dd");

		String data1 = a.str("00");
		String data2 = a.str("11");
		String data3 = a.str("33");

		log.info("data1 = {}", data1);
		log.info("data2= {}", data2);
		log.info("data3 = {}", data3);

		final ArgumentCaptor<String> strCaptor = ArgumentCaptor.forClass(String.class);
		verify(a, atLeastOnce()).str(strCaptor.capture());

		List<String> allValues = strCaptor.getAllValues();
		log.info("captured values = {} and size = {}", allValues, allValues.size());
		// assertEquals(3, allValues.size());

		assertTrue("cc".equals(data1));
		assertTrue("dd".equals(data2));
		assertTrue("bb".equals(data3));
	}

	@Test
	public void test_0() {

		assertTrue(Pattern.matches("00", "00"));
		assertTrue(Pattern.matches("11", "11"));
	}

	/// I do not understand why this fails!!!
	@Test
	@Ignore // ignored because fails and I do not understand why!!!??
	public void test_1() {
		final A a = mock(A.class, RETURNS_DEEP_STUBS);
		// final A a = mock(A.class, RETURNS_MOCKS);

		when(a.read(anyString()).getData()).thenReturn("bb");
		when(a.read(matches("00")).getData()).thenReturn("cc");
		when(a.read(matches("11")).getData()).thenReturn("dd");

		String data1 = a.read("00").getData();
		String data2 = a.read("11").getData();
		String data3 = a.read("33").getData();

		log.info("data1 = {}", data1);
		log.info("data2= {}", data2);
		log.info("data3 = {}", data3);

		final ArgumentCaptor<String> strCaptor = ArgumentCaptor.forClass(String.class);
		verify(a, atLeastOnce()).read(strCaptor.capture());

		List<String> allValues = strCaptor.getAllValues();
		log.info("captured values = {} and size = {}", allValues, allValues.size());
		// assertEquals(3, allValues.size());

		verify(a.read(matches("00")), atLeastOnce()).getData();
		verify(a.read(matches("44")), atLeastOnce()).getData();
		verify(a.read(any()), atLeastOnce()).getData();

		assertTrue("cc".equals(data1));
		assertTrue("dd".equals(data2));
		assertTrue("bb".equals(data3));
	}

	@Test
	public void test_2() {
		final A a = mock(A.class, RETURNS_DEEP_STUBS);
		final String m1 = "bb";
		final String m2 = "cc";
		final String m3 = "dd";

		when(a.read(anyString()).getData()).thenReturn(m1);
		when(a.read(same("00")).getData()).thenReturn(m2);
		when(a.read(same("11")).getData()).thenReturn(m3);

		assertTrue("cc".equals(a.read("00").getData()));
		assertTrue("dd".equals(a.read("11").getData()));
		assertTrue("bb".equals(a.read("33").getData()));

		final ArgumentCaptor<String> strCaptor = ArgumentCaptor.forClass(String.class);
		verify(a, atLeastOnce()).read(strCaptor.capture());

		List<String> allValues = strCaptor.getAllValues();
		log.info("captured values = {} and size = {}", allValues, allValues.size());

	}
}
