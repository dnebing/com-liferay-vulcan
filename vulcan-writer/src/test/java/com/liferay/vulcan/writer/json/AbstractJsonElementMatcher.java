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

import com.google.gson.JsonElement;
import com.spotify.hamcrest.util.LanguageUtils;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeDiagnosingMatcher;

import java.util.Objects;

import static com.liferay.vulcan.writer.json.JsonElementType.getJsonElementType;

public abstract class AbstractJsonElementMatcher<A extends JsonElement>
	extends TypeSafeDiagnosingMatcher<A> {

	private final JsonElementType type;

	AbstractJsonElementMatcher(final JsonElementType type) {
		super(JsonElement.class);
		this.type = Objects.requireNonNull(type);
	}

	@Override
	protected boolean matchesSafely(JsonElement item, Description mismatchDescription) {
		JsonElementType jsonElementType = getJsonElementType(item);

		if (jsonElementType == type) {
			@SuppressWarnings("unchecked") final A node = (A) item;

			return matchesNode(node, mismatchDescription);
		} else {
			mismatchDescription
				.appendText("was not ")
				.appendText(LanguageUtils.addArticle(type.name().toLowerCase()))
				.appendText(" node, but ")
				.appendText(LanguageUtils.addArticle(jsonElementType.name().toLowerCase()))
				.appendText(" node");
			return false;
		}
	}

	protected abstract boolean matchesNode(A node, Description mismatchDescription);
}
