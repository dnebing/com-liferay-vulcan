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

package com.liferay.vulcan.writer.json;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

/**
 * @author Alejandro Hernández
 */
public enum JsonElementType {
	BOOLEAN,
	OBJECT,
	STRING,
	OTHER;

	public static JsonElementType getJsonElementType(JsonElement jsonElement) {
		if (jsonElement.isJsonObject()) {
			return OBJECT;
		}

		if (jsonElement.isJsonPrimitive()) {
			JsonPrimitive jsonPrimitive = jsonElement.getAsJsonPrimitive();

			if (jsonPrimitive.isBoolean()) {
				return BOOLEAN;
			}

			if (jsonPrimitive.isString()) {
				return STRING;
			}
		}

		return OTHER;
	}
}
