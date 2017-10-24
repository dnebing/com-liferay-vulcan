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

import static com.liferay.vulcan.writer.creator.URLCreator.createSingleURL;

import com.google.gson.JsonObject;

import com.liferay.vulcan.function.TriFunction;
import com.liferay.vulcan.list.FunctionalList;
import com.liferay.vulcan.message.json.JSONObjectBuilder;
import com.liferay.vulcan.message.json.SingleModelMessageMapper;
import com.liferay.vulcan.pagination.SingleModel;
import com.liferay.vulcan.resource.RelatedCollection;
import com.liferay.vulcan.resource.RelatedModel;
import com.liferay.vulcan.resource.Representor;
import com.liferay.vulcan.resource.identifier.Identifier;
import com.liferay.vulcan.uri.Path;
import com.liferay.vulcan.writer.internal.json.JSONObjectBuilderImpl;
import com.liferay.vulcan.writer.internal.list.StringFunctionalList;
import com.liferay.vulcan.writer.request.RequestInfo;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import java.nio.charset.StandardCharsets;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * @author Alejandro Hernández
 */
public class SingleModelWriter<T> {

	public SingleModelWriter(Builder<T> builder) {
		_singleModelMessageMapper = builder._singleModelMessageMapper;
		_singleModel = builder._singleModel;
		_jsonObjectBuilder = builder._jsonObjectBuilder;
		_requestInfo = builder._requestInfo;
		_representorFunction = builder._representorFunction;
		_pathFunction = builder._pathFunction;
		_resourceNameFunction = builder._resourceNameFunction;
	}

	public void writeTo(OutputStream entityStream) {
		OutputStreamWriter outputStreamWriter = new OutputStreamWriter(
			entityStream, StandardCharsets.UTF_8);

		PrintWriter printWriter = new PrintWriter(outputStreamWriter, true);

		Class<T> modelClass = _singleModel.getModelClass();

		Optional<Representor<T, Identifier>> representorOptional =
			_getRepresentorOptional(modelClass);

		if (!representorOptional.isPresent()) {
			return;
		}

		Optional<Path> pathOptional = _getPathOptional(_singleModel);

		if (!pathOptional.isPresent()) {
			return;
		}

		Representor<T, Identifier> representor = representorOptional.get();

		Path path = pathOptional.get();

		FieldsWriter<T, Identifier> fieldsWriter = new FieldsWriter<>(
			_singleModel, _requestInfo, representor, path);

		T model = _singleModel.getModel();

		_singleModelMessageMapper.onStart(
			_jsonObjectBuilder, model, modelClass,
			_requestInfo.getHttpHeaders());

		fieldsWriter.writeBooleanFields(
			(field, value) -> _singleModelMessageMapper.mapBooleanField(
				_jsonObjectBuilder, field, value));

		fieldsWriter.writeLocalizedStringFields(
			(field, value) -> _singleModelMessageMapper.mapStringField(
				_jsonObjectBuilder, field, value));

		fieldsWriter.writeNumberFields(
			(field, value) -> _singleModelMessageMapper.mapNumberField(
				_jsonObjectBuilder, field, value));

		fieldsWriter.writeStringFields(
			(field, value) -> _singleModelMessageMapper.mapStringField(
				_jsonObjectBuilder, field, value));

		fieldsWriter.writeLinks(
			(fieldName, link) -> _singleModelMessageMapper.mapLink(
				_jsonObjectBuilder, fieldName, link));

		fieldsWriter.writeTypes(
			types -> _singleModelMessageMapper.mapTypes(
				_jsonObjectBuilder, types));

		fieldsWriter.writeBinaries(
			(field, value) -> _singleModelMessageMapper.mapStringField(
				_jsonObjectBuilder, field, value));

		String url = createSingleURL(_requestInfo.getServerURL(), path);

		_singleModelMessageMapper.mapSelfURL(_jsonObjectBuilder, url);

		List<RelatedModel<T, ?>> embeddedRelatedModels =
			representor.getEmbeddedRelatedModels();

		embeddedRelatedModels.forEach(
			embeddedRelatedModel -> _writeEmbeddedRelatedModel(
				fieldsWriter, embeddedRelatedModel, _singleModel, null));

		List<RelatedModel<T, ?>> linkedRelatedModels =
			representor.getLinkedRelatedModels();

		linkedRelatedModels.forEach(
			linkedRelatedModel -> _writeLinkedRelatedModel(
				linkedRelatedModel, _singleModel, null));

		Stream<RelatedCollection<T, ?>> stream =
			representor.getRelatedCollections();

		stream.forEach(
			relatedCollection -> _writeRelatedCollection(
				fieldsWriter, relatedCollection, null));

		_singleModelMessageMapper.onFinish(
			_jsonObjectBuilder, model, modelClass,
			_requestInfo.getHttpHeaders());

		JsonObject jsonObject = _jsonObjectBuilder.build();

		printWriter.println(jsonObject.toString());

		printWriter.close();
	}

	public static class Builder<T> {

		public SingleModelWriter<T> build() {
			return new SingleModelWriter<>(this);
		}

		public Builder<T> jsonObjectBuilder(
			JSONObjectBuilder jsonObjectBuilder) {

			_jsonObjectBuilder = jsonObjectBuilder;

			return this;
		}

		public Builder<T> modelMessageMapper(
			SingleModelMessageMapper<T> singleModelMessageMapper) {

			_singleModelMessageMapper = singleModelMessageMapper;

			return this;
		}

		public Builder<T> pathFunction(
			TriFunction<Identifier,
				Class<? extends Identifier>, Class<?>, Optional<Path>>
					pathFunction) {

			_pathFunction = pathFunction;

			return this;
		}

		public Builder<T> representorFunction(
			Function<Class<?>,
				Optional<? extends Representor<?, ? extends Identifier>>>
					representorFunction) {

			_representorFunction = representorFunction;

			return this;
		}

		public Builder<T> requestInfo(RequestInfo requestInfo) {
			_requestInfo = requestInfo;

			return this;
		}

		public Builder<T> resourceNameFunction(
			Function<Class<?>, Optional<String>> resourceNameFunction) {

			_resourceNameFunction = resourceNameFunction;

			return this;
		}

		public Builder<T> singleModel(SingleModel<T> singleModel) {
			_singleModel = singleModel;

			return this;
		}

		private JSONObjectBuilder _jsonObjectBuilder =
			new JSONObjectBuilderImpl();
		private TriFunction<Identifier,
			Class<? extends Identifier>, Class<?>, Optional<Path>>
				_pathFunction;
		private Function<Class<?>,
			Optional<? extends Representor<?, ? extends Identifier>>>
				_representorFunction;
		private RequestInfo _requestInfo;
		private Function<Class<?>, Optional<String>> _resourceNameFunction;
		private SingleModel<T> _singleModel;
		private SingleModelMessageMapper<T> _singleModelMessageMapper;

	}

	private <V> Optional<Path> _getPathOptional(SingleModel<V> singleModel) {
		Optional<Representor<V, Identifier>> optional = _getRepresentorOptional(
			singleModel.getModelClass());

		return optional.flatMap(
			representor -> {
				Identifier identifier = representor.getIdentifier(
					singleModel.getModel());

				Class<Identifier> identifierClass =
					representor.getIdentifierClass();

				return _pathFunction.apply(
					identifier, identifierClass, singleModel.getModelClass());
			});
	}

	private <V, W extends Identifier> Optional<Representor<V, W>>
		_getRepresentorOptional(Class<V> modelClass) {

		Optional<? extends Representor<?, ? extends Identifier>> optional =
			_representorFunction.apply(modelClass);

		return optional.map(representor -> (Representor<V, W>)representor);
	}

	private <V> void _writeEmbeddedModelFields(
		SingleModel<V> singleModel,
		FunctionalList<String> embeddedPathElements) {

		Class<V> modelClass = singleModel.getModelClass();

		Optional<Representor<V, Identifier>> representorOptional =
			_getRepresentorOptional(modelClass);

		if (!representorOptional.isPresent()) {
			return;
		}

		Optional<Path> pathOptional = _getPathOptional(singleModel);

		if (!pathOptional.isPresent()) {
			return;
		}

		Representor<V, Identifier> representor = representorOptional.get();

		Path path = pathOptional.get();

		FieldsWriter<V, Identifier> fieldsWriter = new FieldsWriter<>(
			singleModel, _requestInfo, representor, path);

		fieldsWriter.writeBooleanFields(
			(field, value) ->
				_singleModelMessageMapper.mapEmbeddedResourceBooleanField(
					_jsonObjectBuilder, embeddedPathElements, field, value));

		fieldsWriter.writeLocalizedStringFields(
			(field, value) ->
				_singleModelMessageMapper.mapEmbeddedResourceStringField(
					_jsonObjectBuilder, embeddedPathElements, field, value));

		fieldsWriter.writeNumberFields(
			(field, value) ->
				_singleModelMessageMapper.mapEmbeddedResourceNumberField(
					_jsonObjectBuilder, embeddedPathElements, field, value));

		fieldsWriter.writeStringFields(
			(field, value) ->
				_singleModelMessageMapper.mapEmbeddedResourceStringField(
					_jsonObjectBuilder, embeddedPathElements, field, value));

		fieldsWriter.writeLinks(
			(fieldName, link) ->
				_singleModelMessageMapper.mapEmbeddedResourceLink(
					_jsonObjectBuilder, embeddedPathElements, fieldName, link));

		fieldsWriter.writeTypes(
			types -> _singleModelMessageMapper.mapEmbeddedResourceTypes(
				_jsonObjectBuilder, embeddedPathElements, types));

		fieldsWriter.writeBinaries(
			(field, value) ->
				_singleModelMessageMapper.mapEmbeddedResourceStringField(
					_jsonObjectBuilder, embeddedPathElements, field, value));

		List<RelatedModel<V, ?>> embeddedRelatedModels =
			representor.getEmbeddedRelatedModels();

		embeddedRelatedModels.forEach(
			relatedModel -> _writeEmbeddedRelatedModel(
				fieldsWriter, relatedModel, singleModel, embeddedPathElements));

		List<RelatedModel<V, ?>> linkedRelatedModels =
			representor.getLinkedRelatedModels();

		linkedRelatedModels.forEach(
			linkedRelatedModel -> _writeLinkedRelatedModel(
				linkedRelatedModel, singleModel, embeddedPathElements));

		Stream<RelatedCollection<V, ?>> stream =
			representor.getRelatedCollections();

		stream.forEach(
			relatedCollection -> _writeRelatedCollection(
				fieldsWriter, relatedCollection, embeddedPathElements));
	}

	private <V, W extends Identifier, X> void _writeEmbeddedRelatedModel(
		FieldsWriter<V, W> fieldsWriter, RelatedModel<V, X> relatedModel,
		SingleModel<V> parentSingleModel,
		FunctionalList<String> parentEmbeddedPathElements) {

		Optional<SingleModel<X>> optional = FieldsWriter.getSingleModel(
			relatedModel, parentSingleModel);

		optional.flatMap(
			this::_getPathOptional
		).map(
			path -> createSingleURL(_requestInfo.getServerURL(), path)
		).ifPresent(
			url -> fieldsWriter.writeRelatedModel(
				relatedModel, url, parentEmbeddedPathElements,
				this::_writeEmbeddedModelFields,
				(resourceURL, embeddedPathElements) ->
					_singleModelMessageMapper.mapEmbeddedResourceURL(
						_jsonObjectBuilder, embeddedPathElements, resourceURL),
				(resourceURL, embeddedPathElements) ->
					_singleModelMessageMapper.mapLinkedResourceURL(
						_jsonObjectBuilder, embeddedPathElements, resourceURL))
		);
	}

	private <V, X> void _writeLinkedRelatedModel(
		RelatedModel<V, X> relatedModel, SingleModel<V> parentSingleModel,
		FunctionalList<String> parentEmbeddedPathElements) {

		Optional<SingleModel<X>> optional = FieldsWriter.getSingleModel(
			relatedModel, parentSingleModel);

		optional.flatMap(
			this::_getPathOptional
		).map(
			path -> createSingleURL(_requestInfo.getServerURL(), path)
		).ifPresent(
			url -> {
				FunctionalList<String> embeddedPathElements =
					new StringFunctionalList(
						parentEmbeddedPathElements, relatedModel.getKey());

				_singleModelMessageMapper.mapLinkedResourceURL(
					_jsonObjectBuilder, embeddedPathElements, url);
			}
		);
	}

	private <V, W extends Identifier, X> void _writeRelatedCollection(
		FieldsWriter<V, W> fieldsWriter,
		RelatedCollection<V, X> relatedCollection,
		FunctionalList<String> parentEmbeddedPathElements) {

		Optional<String> optional = _resourceNameFunction.apply(
			relatedCollection.getModelClass());

		optional.ifPresent(
			name -> fieldsWriter.writeRelatedCollection(
				relatedCollection, name, parentEmbeddedPathElements,
				(url, embeddedPathElements) ->
					_singleModelMessageMapper.mapLinkedResourceURL(
						_jsonObjectBuilder, embeddedPathElements, url)));
	}

	private final JSONObjectBuilder _jsonObjectBuilder;
	private final TriFunction<Identifier,
		Class<? extends Identifier>, Class<?>, Optional<Path>> _pathFunction;
	private final Function<Class<?>,
		Optional<? extends Representor<?, ? extends Identifier>>>
			_representorFunction;
	private final RequestInfo _requestInfo;
	private final Function<Class<?>, Optional<String>> _resourceNameFunction;
	private final SingleModel<T> _singleModel;
	private final SingleModelMessageMapper<T> _singleModelMessageMapper;

}