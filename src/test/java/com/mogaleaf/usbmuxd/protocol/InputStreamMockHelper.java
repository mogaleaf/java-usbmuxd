package com.mogaleaf.usbmuxd.protocol;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;

import java.util.HashMap;
import java.util.Map;

public class InputStreamMockHelper extends BaseMatcher<byte[]> {

	Map<Integer, byte[]> bySize = new HashMap<>();

	InputStreamMockHelper(byte[]... bytesToMatch) {
		for (byte[] toMatch : bytesToMatch) {
			bySize.put(toMatch.length, toMatch);
		}
	}

	@Override
	public boolean matches(Object o) {
		if (o instanceof byte[]) {
			byte[] copy = (byte[]) o;
			byte[] bytes = bySize.get(copy.length);
			int i = 0;
			for (byte b : bytes) {
				copy[i++] = b;
			}
			return true;
		}
		return false;
	}

	@Override
	public void describeMismatch(Object o, Description description) {

	}

	@Override
	public void describeTo(Description description) {

	}
}