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

package com.liferay.talend.runtime.apio.jsonld;

/**
 * Hydra Vocabulary for Hypermedia-Driven Web APIs,
 * see <a href="https://www.w3.org/ns/hydra/core">Hydra Core Vocabulary</a>
 *
 *
 * @author Zoltán Takács
 */
public interface HydraConstants {

	public static final String CLASS = hydra("Class");

	public static final String COLLECTION = hydra("Collection");

	public static final String HYDRA = "hydra";

	public static final String OPERATION = hydra("Operation");

	public static final String PARTIAL_COLLECTION_VIEW = hydra(
		"PartialCollectionView");

	public static final String SUPPORTED_PROPERTY = hydra("SupportedProperty");

	/**
	 * Prefixes a type with the Hydra qualifier
	 *
	 * @param  type the type to prefix
	 * @return the type prefixed with the Hydra qualifier
	 * @review
	 */
	public static String hydra(String type) {
		return HYDRA + ":" + type;
	}

}