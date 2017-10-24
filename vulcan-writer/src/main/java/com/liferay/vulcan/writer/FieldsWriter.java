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

import com.liferay.vulcan.alias.BinaryFunction;
import com.liferay.vulcan.language.Language;
import com.liferay.vulcan.list.FunctionalList;
import com.liferay.vulcan.pagination.SingleModel;
import com.liferay.vulcan.resource.RelatedCollection;
import com.liferay.vulcan.resource.RelatedModel;
import com.liferay.vulcan.resource.Representor;
import com.liferay.vulcan.resource.identifier.Identifier;
import com.liferay.vulcan.response.control.Embedded;
import com.liferay.vulcan.response.control.Fields;
import com.liferay.vulcan.uri.Path;
import com.liferay.vulcan.writer.creator.URLCreator;
import com.liferay.vulcan.writer.internal.list.StringFunctionalList;
import com.liferay.vulcan.writer.request.RequestInfo;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Alejandro Hernández
 */
public class FieldsWriter<T, U extends Identifier> {

	public static <T, V> Optional<SingleModel<V>> getSingleModel(
		RelatedModel<T, V> relatedModel, SingleModel<T> parentSingleModel) {

		Function<T, Optional<V>> modelFunction =
			relatedModel.getModelFunction();

		Optional<V> optional = modelFunction.apply(
			parentSingleModel.getModel());

		return optional.map(
			model -> {
				Class<V> modelClass = relatedModel.getModelClass();

				return new SingleModel<>(model, modelClass);
			});
	}

	public FieldsWriter(
		SingleModel<T> singleModel, RequestInfo requestInfo,
		Representor<T, U> representor, Path path) {

		_singleModel = singleModel;
		_requestInfo = requestInfo;
		_representor = representor;
		_path = path;
	}

	/**
	 * Helper method to write binary resources. It uses a bi consumer so each
	 * {@link javax.ws.rs.ext.MessageBodyWriter} can write each binary
	 * differently.
	 *
	 * @param  biConsumer the consumer that will be called to write each binary.
	 * @review
	 */
	public void writeBinaries(BiConsumer<String, String> biConsumer) {
		Map<String, BinaryFunction<T>> binaryFunctions =
			_representor.getBinaryFunctions();

		Set<String> binaryIds = binaryFunctions.keySet();

		binaryIds.forEach(
			binaryId -> biConsumer.accept(
				binaryId,
				URLCreator.createBinaryURL(
					_requestInfo.getServerURL(), binaryId, _path)));
	}

	/**
	 * Helper method to write a model number fields. It uses a consumer so each
	 * {@link javax.ws.rs.ext.MessageBodyWriter} can write each field
	 * differently.
	 *
	 * @param  biConsumer the consumer that will be called to write each field.
	 * @review
	 */
	public void writeBooleanFields(BiConsumer<String, Boolean> biConsumer) {
		Predicate<String> fieldsPredicate = _getFieldsPredicate();

		Map<String, Function<T, Boolean>> booleanFunctions =
			_representor.getBooleanFunctions();

		Set<Map.Entry<String, Function<T, Boolean>>> entries =
			booleanFunctions.entrySet();

		Stream<Map.Entry<String, Function<T, Boolean>>> stream =
			entries.stream();

		stream.filter(
			entry -> fieldsPredicate.test(entry.getKey())
		).forEach(
			entry -> {
				Function<T, Boolean> fieldFunction = entry.getValue();

				Boolean data = fieldFunction.apply(_singleModel.getModel());

				if (data != null) {
					biConsumer.accept(entry.getKey(), data);
				}
			}
		);
	}

	/**
	 * Helper method to write a model links. It uses a consumer so each {@link
	 * javax.ws.rs.ext.MessageBodyWriter} can write each link differently.
	 *
	 * @param  biConsumer the consumer that will be called to write each link.
	 * @review
	 */
	public void writeLinks(BiConsumer<String, String> biConsumer) {
		Predicate<String> fieldsPredicate = _getFieldsPredicate();

		Map<String, String> stringFunctions = _representor.getLinks();

		Set<Map.Entry<String, String>> entries = stringFunctions.entrySet();

		Stream<Map.Entry<String, String>> stream = entries.stream();

		stream.filter(
			entry -> fieldsPredicate.test(entry.getKey())
		).forEach(
			entry -> {
				String data = entry.getValue();

				if ((data != null) && !data.isEmpty()) {
					biConsumer.accept(entry.getKey(), data);
				}
			}
		);
	}

	/**
	 * Helper method to write a model localized string fields. It uses a
	 * consumer so each {@link javax.ws.rs.ext.MessageBodyWriter} can write each
	 * field differently.
	 *
	 * @param  biConsumer the consumer that will be called to write each field.
	 * @review
	 */
	public void writeLocalizedStringFields(
		BiConsumer<String, String> biConsumer) {

		Predicate<String> fieldsPredicate = _getFieldsPredicate();

		Optional<Language> languageOptional =
			_requestInfo.getLanguageOptional();

		if (!languageOptional.isPresent()) {
			return;
		}

		Language language = languageOptional.get();

		Map<String, BiFunction<T, Language, String>> localizedStringFunctions =
			_representor.getLocalizedStringFunctions();

		Set<Map.Entry<String, BiFunction<T, Language, String>>> entries =
			localizedStringFunctions.entrySet();

		Stream<Map.Entry<String, BiFunction<T, Language, String>>> stream =
			entries.stream();

		stream.filter(
			entry -> fieldsPredicate.test(entry.getKey())
		).forEach(
			entry -> {
				BiFunction<T, Language, String> fieldFunction =
					entry.getValue();

				String data = fieldFunction.apply(
					_singleModel.getModel(), language);

				if ((data != null) && !data.isEmpty()) {
					biConsumer.accept(entry.getKey(), data);
				}
			}
		);
	}

	/**
	 * Helper method to write a model number fields. It uses a consumer so each
	 * {@link javax.ws.rs.ext.MessageBodyWriter} can write each field
	 * differently.
	 *
	 * @param  biConsumer the consumer that will be called to write each field.
	 * @review
	 */
	public void writeNumberFields(BiConsumer<String, Number> biConsumer) {
		Predicate<String> fieldsPredicate = _getFieldsPredicate();

		Map<String, Function<T, Number>> stringFunctions =
			_representor.getNumberFunctions();

		Set<Map.Entry<String, Function<T, Number>>> entries =
			stringFunctions.entrySet();

		Stream<Map.Entry<String, Function<T, Number>>> stream =
			entries.stream();

		stream.filter(
			entry -> fieldsPredicate.test(entry.getKey())
		).forEach(
			entry -> {
				Function<T, Number> fieldFunction = entry.getValue();

				Number data = fieldFunction.apply(_singleModel.getModel());

				if (data != null) {
					biConsumer.accept(entry.getKey(), data);
				}
			}
		);
	}

	/**
	 * Helper method to write a model related collection. It uses a consumer for
	 * writing the URL.
	 *
	 * @param  relatedCollection the instance of the related collection.
	 * @param  parentEmbeddedPathElements list of embedded path elements.
	 * @param  biConsumer the consumer that will be called to write the related
	 *         collection URL.
	 * @review
	 */
	public <V> void writeRelatedCollection(
		RelatedCollection<T, V> relatedCollection, String resourceName,
		FunctionalList<String> parentEmbeddedPathElements,
		BiConsumer<String, FunctionalList<String>> biConsumer) {

		Predicate<String> fieldsPredicate = _getFieldsPredicate();

		String key = relatedCollection.getKey();

		if (!fieldsPredicate.test(key)) {
			return;
		}

		String url = URLCreator.createRelatedCollectionURL(
			_requestInfo.getServerURL(), _path, resourceName);

		FunctionalList<String> embeddedPathElements = new StringFunctionalList(
			parentEmbeddedPathElements, key);

		biConsumer.accept(url, embeddedPathElements);
	}

	/**
	 * Helper method to write a model related models. It uses two consumers (one
	 * for writing the model info, and another for writing its URL) so each
	 * {@link javax.ws.rs.ext.MessageBodyWriter} can write the related model
	 * differently.
	 *
	 * @param  relatedModel the instance of the related model.
	 * @param  parentEmbeddedPathElements list of embedded path elements.
	 * @param  modelBiConsumer the consumer that will be called to write the
	 *         related model info.
	 * @review
	 */
	public <V> void writeRelatedModel(
		RelatedModel<T, V> relatedModel, String url,
		FunctionalList<String> parentEmbeddedPathElements,
		BiConsumer<SingleModel<V>, FunctionalList<String>> modelBiConsumer,
		BiConsumer<String, FunctionalList<String>> linkedURLBiConsumer,
		BiConsumer<String, FunctionalList<String>> embeddedURLBiConsumer) {

		Predicate<String> fieldsPredicate = _getFieldsPredicate();

		String key = relatedModel.getKey();

		if (!fieldsPredicate.test(key)) {
			return;
		}

		Optional<SingleModel<V>> optional = getSingleModel(
			relatedModel, _singleModel);

		if (!optional.isPresent()) {
			return;
		}

		SingleModel<V> singleModel = optional.get();

		Predicate<String> embeddedPredicate = _getEmbeddedPredicate();

		FunctionalList<String> embeddedPathElements = new StringFunctionalList(
			parentEmbeddedPathElements, key);

		Stream<String> stream = Stream.concat(
			Stream.of(embeddedPathElements.head()),
			embeddedPathElements.tailStream());

		String embeddedPath = String.join(
			".", stream.collect(Collectors.toList()));

		boolean embedded = embeddedPredicate.test(embeddedPath);

		if (embedded) {
			embeddedURLBiConsumer.accept(url, embeddedPathElements);
			modelBiConsumer.accept(singleModel, embeddedPathElements);
		}
		else {
			linkedURLBiConsumer.accept(url, embeddedPathElements);
		}
	}

	/**
	 * Helper method to write a model string fields. It uses a consumer so each
	 * {@link javax.ws.rs.ext.MessageBodyWriter} can write each field
	 * differently.
	 *
	 * @param  biConsumer the consumer that will be called to write each field.
	 * @review
	 */
	public void writeStringFields(BiConsumer<String, String> biConsumer) {
		Predicate<String> fieldsPredicate = _getFieldsPredicate();

		Map<String, Function<T, String>> stringFunctions =
			_representor.getStringFunctions();

		Set<Map.Entry<String, Function<T, String>>> entries =
			stringFunctions.entrySet();

		Stream<Map.Entry<String, Function<T, String>>> stream =
			entries.stream();

		stream.filter(
			entry -> fieldsPredicate.test(entry.getKey())
		).forEach(
			entry -> {
				Function<T, String> fieldFunction = entry.getValue();

				String data = fieldFunction.apply(_singleModel.getModel());

				if ((data != null) && !data.isEmpty()) {
					biConsumer.accept(entry.getKey(), data);
				}
			}
		);
	}

	/**
	 * Helper method to write a model types. It uses a consumer so each {@link
	 * javax.ws.rs.ext.MessageBodyWriter} can write the types differently.
	 *
	 * @param  consumer the consumer that will be called to write the types.
	 * @review
	 */
	public void writeTypes(Consumer<List<String>> consumer) {
		consumer.accept(_representor.getTypes());
	}

	private Predicate<String> _getEmbeddedPredicate() {
		Optional<Embedded> optional = _requestInfo.getEmbeddedOptional();

		return optional.map(
			Embedded::getEmbeddedPredicate
		).orElseGet(
			() -> field -> false
		);
	}

	private Predicate<String> _getFieldsPredicate() {
		Optional<Fields> optional = _requestInfo.getFieldsOptional();

		return optional.map(
			fields -> fields.getFieldsPredicate(_representor.getTypes())
		).orElseGet(
			() -> field -> true
		);
	}

	private final Path _path;
	private final Representor<T, U> _representor;
	private final RequestInfo _requestInfo;
	private final SingleModel<T> _singleModel;

}