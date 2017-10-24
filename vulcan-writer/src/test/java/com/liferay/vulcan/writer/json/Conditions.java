package com.liferay.vulcan.writer.json;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.spotify.hamcrest.util.DescriptionUtils;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.StringDescription;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

public class Conditions extends AbstractJsonElementMatcher<JsonObject> {

	private final Map<String, Matcher<? extends JsonElement>> entryMatchers;

	public static class Builder {


		public Builder(Map<String, Matcher<? extends JsonElement>> newMap) {
			entryMatchers = newMap;
		}

		public Builder() {
			entryMatchers = new HashMap<>();
		}

		public Builder where(String key, Matcher<? extends JsonElement> valueMatcher) {
			Map<String, Matcher<? extends JsonElement>> newMap
				= new HashMap<>(entryMatchers);

			newMap.put(key, valueMatcher);

			return new Builder(newMap);
		}

		public Conditions build() {
			return new Conditions(this);
		}

		private final Map<String, Matcher<? extends JsonElement>> entryMatchers;
	}

	private Conditions(Builder builder) {

		super(JsonElementType.OBJECT);
		this.entryMatchers = Objects.requireNonNull(builder.entryMatchers);
	}

	@Override
	protected boolean matchesNode(JsonObject node, Description mismatchDescription) {
		Map<String, Consumer<Description>> mismatchedKeys = new HashMap<>();
		for (Map.Entry<String, Matcher<? extends JsonElement>> entryMatcher : entryMatchers.entrySet()) {
			String key = entryMatcher.getKey();

			Matcher<? extends JsonElement> valueMatcher = entryMatcher.getValue();

			JsonElement value = node.get(key);

			if (!valueMatcher.matches(value)) {
				mismatchedKeys.put(key, d -> valueMatcher.describeMismatch(value, d));
			}
		}

		if (!mismatchedKeys.isEmpty()) {
			DescriptionUtils.describeNestedMismatches(
				entryMatchers.keySet(),
				mismatchDescription,
				mismatchedKeys,
				(key, description) -> description.appendText(key));
			return false;
		}
		return true;
	}

	@Override
	public void describeTo(Description description) {
		description.appendText("{\n");
		for (Map.Entry<String, Matcher<? extends JsonElement>> entryMatcher : entryMatchers.entrySet()) {
			final String key = entryMatcher.getKey();
			final Matcher<? extends JsonElement> valueMatcher = entryMatcher.getValue();

			description.appendText("  ");
			description.appendText(key);
			description.appendText(": ");

			final Description innerDescription = new StringDescription();
			valueMatcher.describeTo(innerDescription);
			DescriptionUtils.indentDescription(description, innerDescription);
		}
		description.appendText("}");
	}
}
