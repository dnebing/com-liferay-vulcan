/*-
 * -\-\-
 * hamcrest-jackson
 * --
 * Copyright (C) 2016 Spotify AB
 * --
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * -/-/-
 */

package com.liferay.vulcan.writer.json;

import com.google.gson.JsonPrimitive;
import org.hamcrest.Description;
import org.hamcrest.Matcher;

import java.util.Objects;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.core.Is.is;

public class IsJsonBoolean extends AbstractJsonElementMatcher<JsonPrimitive> {

	private final Matcher<? super Boolean> booleanMatcher;

	private IsJsonBoolean(Matcher<? super Boolean> booleanMatcher) {
		super(JsonElementType.BOOLEAN);
		this.booleanMatcher = Objects.requireNonNull(booleanMatcher);
	}

	public static Matcher<JsonPrimitive> jsonBoolean(boolean bool) {
		return new IsJsonBoolean(is(equalTo(bool)));
	}

	@Override
	protected boolean matchesNode(
		JsonPrimitive jsonPrimitive, Description mismatchDescription) {

		final boolean value = jsonPrimitive.getAsBoolean();

		if (booleanMatcher.matches(value)) {
			return true;
		} else {
			mismatchDescription.appendText("was a boolean element with value that ");
			booleanMatcher.describeMismatch(value, mismatchDescription);
			return false;
		}
	}

	@Override
	public void describeTo(Description description) {
		description.appendText("a boolean element with value that ").appendDescriptionOf(booleanMatcher);
	}
}
