/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.vulcan.writer;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;

/**
 * This class provides {@code Hamcrest} {@link Matcher}s that can be used for
 * testing the JSON objects.
 *
 * @author Alejandro Hernández
 * @review
 */
public class JsonStringMatcher {

	public static Matcher<String> aString(final Matcher<String> matcher) {
		return new IsJsonString(matcher);
	}

	private static class IsJsonString
		extends TypeSafeDiagnosingMatcher<String> {

		public IsJsonString(final Matcher<String> matcher) {
			_matcher = matcher;
		}

		@Override
		public void describeTo(final Description description) {
			description.appendText(
				"a string value "
			).appendDescriptionOf(
				_matcher
			);
		}

		@Override
		protected boolean matchesSafely(String item, Description description) {
			if (_matcher.matches(item)) {
				return true;
			}
			else {
				_matcher.describeMismatch(item, description);

				return false;
			}
		}

		private final Matcher<String> _matcher;

	}

}