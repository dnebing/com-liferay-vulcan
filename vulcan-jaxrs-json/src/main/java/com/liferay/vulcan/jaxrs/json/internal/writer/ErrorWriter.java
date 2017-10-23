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

package com.liferay.vulcan.jaxrs.json.internal.writer;

import com.google.gson.JsonObject;

import com.liferay.vulcan.jaxrs.json.internal.JSONObjectBuilderImpl;
import com.liferay.vulcan.message.json.ErrorMessageMapper;
import com.liferay.vulcan.message.json.JSONObjectBuilder;
import com.liferay.vulcan.result.APIError;

import java.util.Optional;

import javax.ws.rs.core.HttpHeaders;

/**
 * Gives Vulcan the ability to write {@link APIError} by using an {@link
 * ErrorMessageMapper}.
 *
 * @author Alejandro Hernández
 * @review
 */
public class ErrorWriter {

	/**
	 * Writes an {@code APIError} using an {@code ErrorMessageMapper}.
	 *
	 * @param  errorMessageMapper the correct {@code ErrorMessageMapper} for
	 *         this combination of {@code APIError}/{@code HttpHeaders}.
	 * @param  apiError an instance of the apiError.
	 * @param  httpHeaders the HTTP headers of the current request.
	 * @return the apiError written in a JSON string.
	 * @review
	 */
	public static String writeError(
		ErrorMessageMapper errorMessageMapper, APIError apiError,
		HttpHeaders httpHeaders) {

		JSONObjectBuilder jsonObjectBuilder = new JSONObjectBuilderImpl();

		errorMessageMapper.onStart(jsonObjectBuilder, apiError, httpHeaders);

		Optional<String> optional = apiError.getDescription();

		optional.ifPresent(
			description -> errorMessageMapper.mapDescription(
				jsonObjectBuilder, description));

		errorMessageMapper.mapStatusCode(
			jsonObjectBuilder, apiError.getStatusCode());
		errorMessageMapper.mapTitle(jsonObjectBuilder, apiError.getTitle());
		errorMessageMapper.mapType(jsonObjectBuilder, apiError.getType());
		errorMessageMapper.onFinish(jsonObjectBuilder, apiError, httpHeaders);

		JsonObject jsonObject = jsonObjectBuilder.build();

		return jsonObject.toString();
	}

}