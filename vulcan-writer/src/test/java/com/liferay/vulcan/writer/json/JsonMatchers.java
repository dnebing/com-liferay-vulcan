/*-
 * -\-\-
 * hamcrest-jackson
 * --
 * Copyright (C) 2016 - 2017 Spotify AB
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


import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;

public final class JsonMatchers {

	private JsonMatchers() {
	}

	public static Matcher<String> aJsonObject(Conditions where) {
		return new JsonObjectMatcher(where);
	}

	private static class JsonObjectMatcher extends TypeSafeDiagnosingMatcher<String> {
		private final Matcher<JsonObject> _where;

		public JsonObjectMatcher(Matcher<JsonObject> where) {
			_where = where;
		}

		@Override
		protected boolean matchesSafely(String item, Description description) {
			final JsonElement jsonElement = new Gson().fromJson(item, JsonElement.class);

			if (jsonElement.isJsonObject()) {
				JsonObject jsonObject = jsonElement.getAsJsonObject();

				if (_where.matches(jsonObject)) {
					return true;
				} else {
					description.appendText("was a json object ");

					_where.describeMismatch(jsonObject, description);

					return false;
				}
			} else {
				description.appendText("was a failure");

				return false;
			}
		}

		@Override
		public void describeTo(Description description) {
			description.appendText(
				"a json object "
			).appendDescriptionOf(
				_where
			);
		}
	}
}
