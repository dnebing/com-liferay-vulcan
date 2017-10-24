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


import static com.liferay.vulcan.writer.json.IsJsonText.jsonText;
import static com.liferay.vulcan.writer.json.JsonMatchers.aJsonObject;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import com.liferay.vulcan.message.json.ErrorMessageMapper;
import com.liferay.vulcan.message.json.JSONObjectBuilder;
import com.liferay.vulcan.result.APIError;

import java.io.IOException;

import java.util.Objects;
import java.util.Optional;

import javax.ws.rs.core.HttpHeaders;

import com.liferay.vulcan.writer.json.Conditions;
import org.junit.Test;

import org.mockito.Mockito;

/**
 * @author Alejandro Hernández
 */
public class ErrorWriterTest {

	@Test
	public void test() throws IOException {
		ErrorMessageMapper errorMessageMapper = new TestErrorMessageMapper();

		APIError apiError = Mockito.mock(APIError.class);

		Mockito.when(
			apiError.getDescription()
		).thenReturn(
			Optional.of("a description")
		);

		Mockito.when(
			apiError.getStatusCode()
		).thenReturn(
			404
		);

		Mockito.when(
			apiError.getTitle()
		).thenReturn(
			"a title"
		);

		Mockito.when(
			apiError.getType()
		).thenReturn(
			"a type"
		);

		HttpHeaders httpHeaders = Mockito.mock(HttpHeaders.class);

		Mockito.when(
			httpHeaders.getHeaderString("start")
		).thenReturn(
			"true"
		);

		Mockito.when(
			httpHeaders.getHeaderString("end")
		).thenReturn(
			"true"
		);

		String error = ErrorWriter.writeError(
			errorMessageMapper, apiError, httpHeaders);

		final Conditions.Builder builder = new Conditions.Builder();

		Conditions conditions = builder.where(
			"description", is(jsonText(equalTo("a description")))
		).where(
			"title", is(jsonText(equalTo("a title")))
		).build();

		assertThat(error, is(aJsonObject(conditions)));
	}

	private class TestErrorMessageMapper implements ErrorMessageMapper {

		@Override
		public String getMediaType() {
			return "mediaType";
		}

		@Override
		public void mapDescription(
			JSONObjectBuilder jsonObjectBuilder, String description) {

			jsonObjectBuilder.field(
				"description"
			).stringValue(
				description
			);
		}

		@Override
		public void mapStatusCode(
			JSONObjectBuilder jsonObjectBuilder, Integer statusCode) {

			jsonObjectBuilder.field(
				"status"
			).numberValue(
				statusCode
			);
		}

		@Override
		public void mapTitle(
			JSONObjectBuilder jsonObjectBuilder, String title) {

			jsonObjectBuilder.field(
				"title"
			).stringValue(
				title
			);
		}

		@Override
		public void mapType(JSONObjectBuilder jsonObjectBuilder, String type) {
			jsonObjectBuilder.field(
				"type"
			).stringValue(
				type
			);
		}

		@Override
		public void onFinish(
			JSONObjectBuilder jsonObjectBuilder, APIError apiError,
			HttpHeaders httpHeaders) {

			if (Objects.equals(httpHeaders.getHeaderString("end"), "true")) {
				jsonObjectBuilder.field(
					"end"
				).booleanValue(
					true
				);
			}
		}

		@Override
		public void onStart(
			JSONObjectBuilder jsonObjectBuilder, APIError apiError,
			HttpHeaders httpHeaders) {

			if (Objects.equals(httpHeaders.getHeaderString("start"), "true")) {
				jsonObjectBuilder.field(
					"start"
				).booleanValue(
					true
				);
			}
		}

	}

}