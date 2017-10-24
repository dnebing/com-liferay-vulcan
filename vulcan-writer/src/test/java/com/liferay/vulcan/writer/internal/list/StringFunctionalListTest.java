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

package com.liferay.vulcan.writer.internal.list;

import static co.unruly.matchers.StreamMatchers.contains;
import static co.unruly.matchers.StreamMatchers.empty;

import static com.spotify.hamcrest.optional.OptionalMatchers.emptyOptional;
import static com.spotify.hamcrest.optional.OptionalMatchers.optionalWithValue;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;

import java.util.Optional;
import java.util.stream.Stream;

import org.junit.Test;

/**
 * @author Alejandro Hernández
 */
public class StringFunctionalListTest {

	@Test
	public void testRetrievingHeadFromMultiElementListReturnsFirstElement() {
		StringFunctionalList stringFunctionalList = _getStringFunctionalList();

		String head = stringFunctionalList.head();

		assertThat(head, is(equalTo("element1")));
	}

	@Test
	public void testRetrievingHeadFromOneElementListReturnsElement() {
		StringFunctionalList stringFunctionalList = new StringFunctionalList(
			null, "test");

		String head = stringFunctionalList.head();

		assertThat(head, is(equalTo("test")));
	}

	@Test
	public void testRetrievingInitFromMultiElementListReturnsInitSublist() {
		StringFunctionalList stringFunctionalList = _getStringFunctionalList();

		Stream<String> stream = stringFunctionalList.initStream();

		assertThat(stream, contains("element1", "element2", "element3"));
	}

	@Test
	public void testRetrievingInitFromOneElementListReturnsOneElementStream() {
		StringFunctionalList stringFunctionalList = new StringFunctionalList(
			null, "test");

		Stream<String> stream = stringFunctionalList.initStream();

		assertThat(stream, contains("test"));
	}

	@Test
	public void testRetrievingLastFromMultiElementListReturnsLastElement() {
		StringFunctionalList stringFunctionalList = _getStringFunctionalList();

		Optional<String> optional = stringFunctionalList.lastOptional();

		assertThat(optional, optionalWithValue(equalTo("element4")));
	}

	@Test
	public void testRetrievingLastFromOneElementListReturnsEmpty() {
		StringFunctionalList stringFunctionalList = new StringFunctionalList(
			null, "test");

		Optional<String> optional = stringFunctionalList.lastOptional();

		assertThat(optional, emptyOptional());
	}

	@Test
	public void testRetrievingMiddleFromMultiElementListReturnsMiddleSublist() {
		StringFunctionalList stringFunctionalList = _getStringFunctionalList();

		Stream<String> stream = stringFunctionalList.middleStream();

		assertThat(stream, contains("element2", "element3"));
	}

	@Test
	public void testRetrievingMiddleFromOneElementListReturnsEmptyStream() {
		StringFunctionalList stringFunctionalList = new StringFunctionalList(
			null, "test");

		Stream<String> stream = stringFunctionalList.middleStream();

		assertThat(stream, empty());
	}

	@Test
	public void testRetrievingTailFromMultiElementListReturnsMiddleSublist() {
		StringFunctionalList stringFunctionalList = _getStringFunctionalList();

		Stream<String> stream = stringFunctionalList.tailStream();

		assertThat(stream, contains("element2", "element3", "element4"));
	}

	@Test
	public void testRetrievingTailFromOneElementListReturnsEmptyStream() {
		StringFunctionalList stringFunctionalList = new StringFunctionalList(
			null, "test");

		Stream<String> stream = stringFunctionalList.tailStream();

		assertThat(stream, empty());
	}

	private StringFunctionalList _getStringFunctionalList() {
		StringFunctionalList stringFunctionalList1 = new StringFunctionalList(
			null, "element1");

		StringFunctionalList stringFunctionalList2 = new StringFunctionalList(
			stringFunctionalList1, "element2");

		StringFunctionalList stringFunctionalList3 = new StringFunctionalList(
			stringFunctionalList2, "element3");

		return new StringFunctionalList(stringFunctionalList3, "element4");
	}

}