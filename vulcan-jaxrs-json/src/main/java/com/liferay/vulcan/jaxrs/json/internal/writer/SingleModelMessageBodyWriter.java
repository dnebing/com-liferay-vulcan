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

import static org.osgi.service.component.annotations.ReferenceCardinality.AT_LEAST_ONE;
import static org.osgi.service.component.annotations.ReferencePolicyOption.GREEDY;

import com.liferay.vulcan.error.VulcanDeveloperError.MustHaveMessageMapper;
import com.liferay.vulcan.error.VulcanDeveloperError.MustHaveProvider;
import com.liferay.vulcan.language.Language;
import com.liferay.vulcan.message.json.SingleModelMessageMapper;
import com.liferay.vulcan.pagination.SingleModel;
import com.liferay.vulcan.response.control.Embedded;
import com.liferay.vulcan.response.control.Fields;
import com.liferay.vulcan.result.Try;
import com.liferay.vulcan.result.Try.Success;
import com.liferay.vulcan.url.ServerURL;
import com.liferay.vulcan.wiring.osgi.manager.CollectionResourceManager;
import com.liferay.vulcan.wiring.osgi.manager.PathIdentifierMapperManager;
import com.liferay.vulcan.wiring.osgi.manager.ProviderManager;
import com.liferay.vulcan.wiring.osgi.util.GenericUtil;
import com.liferay.vulcan.writer.SingleModelWriter;
import com.liferay.vulcan.writer.request.RequestInfo;

import java.io.IOException;
import java.io.OutputStream;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import javax.servlet.http.HttpServletRequest;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * Gives Vulcan the ability to write single models. For that end it uses the
 * right {@link SingleModelMessageMapper} in accordance with the media type.
 *
 * @author Alejandro Hernández
 * @author Carlos Sierra Andrés
 * @author Jorge Ferrer
 * @review
 */
@Component(
	immediate = true, property = "liferay.vulcan.message.body.writer=true"
)
@Provider
public class SingleModelMessageBodyWriter<T>
	implements MessageBodyWriter<Success<SingleModel<T>>> {

	@Override
	public long getSize(
		Success<SingleModel<T>> success, Class<?> clazz, Type genericType,
		Annotation[] annotations, MediaType mediaType) {

		return -1;
	}

	@Override
	public boolean isWriteable(
		Class<?> clazz, Type genericType, Annotation[] annotations,
		MediaType mediaType) {

		Try<Class<Object>> classTry =
			GenericUtil.getFirstGenericTypeArgumentTry(genericType);

		return classTry.filter(
			SingleModel.class::equals
		).isSuccess();
	}

	@Override
	public void writeTo(
			Success<SingleModel<T>> success, Class<?> clazz, Type genericType,
			Annotation[] annotations, MediaType mediaType,
			MultivaluedMap<String, Object> httpHeaders,
			OutputStream entityStream)
		throws IOException, WebApplicationException {

		SingleModel<T> singleModel = success.getValue();

		SingleModelMessageMapper<T> singleModelMessageMapper =
			getSingleModelMessageMapper(mediaType, singleModel);

		ServerURL serverURL = getServerURL();

		RequestInfo.Builder requestInfoBuilder = new RequestInfo.Builder();

		RequestInfo requestInfo = requestInfoBuilder.httpHeaders(
			this.httpHeaders
		).serverURL(
			serverURL
		).embedded(
			_provide(Embedded.class)
		).fields(
			_provide(Fields.class)
		).language(
			_provide(Language.class)
		).build();

		SingleModelWriter.Builder<T> singleModelWriterBuilder =
			new SingleModelWriter.Builder<>();

		SingleModelWriter<T> singleModelWriter =
			singleModelWriterBuilder.modelMessageMapper(
				singleModelMessageMapper
			).singleModel(
				singleModel
			).requestInfo(
				requestInfo
			).representorFunction(
				_collectionResourceManager::getRepresentorOptional
			).resourceNameFunction(
				aClass -> _collectionResourceManager.getNameOptional(
					aClass.getName())
			).pathFunction(
				_pathIdentifierMapperManager::map
			).build();

		singleModelWriter.writeTo(entityStream);
	}

	/**
	 * Returns the server URL, or throws a {@link MustHaveProvider} developer
	 * error.
	 *
	 * @return the server URL.
	 */
	protected ServerURL getServerURL() {
		Optional<ServerURL> optional = providerManager.provideOptional(
			ServerURL.class, httpServletRequest);

		return optional.orElseThrow(
			() -> new MustHaveProvider(ServerURL.class));
	}

	/**
	 * Returns the right {@link SingleModelMessageMapper} for the provided
	 * {@link MediaType} that supports writing the provided {@link SingleModel}.
	 *
	 * @param  mediaType the request media type
	 * @param  singleModel the single model to write
	 * @return the {@code SingleModelMessageMapper} that writes the {@code
	 *         SingleModel} in the media type
	 */
	protected SingleModelMessageMapper<T> getSingleModelMessageMapper(
		MediaType mediaType, SingleModel<T> singleModel) {

		Stream<SingleModelMessageMapper<T>> stream =
			singleModelMessageMappers.stream();

		String mediaTypeString = mediaType.toString();

		return stream.filter(
			messageMapper ->
				mediaTypeString.equals(messageMapper.getMediaType()) &&
				messageMapper.supports(singleModel, httpHeaders)
		).findFirst(
		).orElseThrow(
			() -> new MustHaveMessageMapper(
				mediaTypeString, singleModel.getModelClass())
		);
	}

	@Context
	protected HttpHeaders httpHeaders;

	@Context
	protected HttpServletRequest httpServletRequest;

	@Reference
	protected ProviderManager providerManager;

	@Reference(cardinality = AT_LEAST_ONE, policyOption = GREEDY)
	protected List<SingleModelMessageMapper<T>> singleModelMessageMappers;

	private <S> S _provide(Class<S> clazz) {
		return providerManager.provideOrNull(clazz, httpServletRequest);
	}

	@Reference
	private CollectionResourceManager _collectionResourceManager;

	@Reference
	private PathIdentifierMapperManager _pathIdentifierMapperManager;

}