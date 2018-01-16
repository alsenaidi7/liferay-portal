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

package com.liferay.fragment.entry.processor.portlet;

import com.liferay.fragment.exception.FragmentEntryContentException;
import com.liferay.fragment.processor.FragmentEntryProcessor;
import com.liferay.fragment.processor.PortletRegistry;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.language.LanguageUtil;
import com.liferay.portal.kernel.service.PortletLocalServiceUtil;
import com.liferay.portal.kernel.util.ResourceBundleUtil;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.kernel.util.StringUtil;

import java.util.ResourceBundle;

import javax.portlet.Portlet;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Pavel Savinov
 */
@Component(
	immediate = true,
	property = {"fragment.entry.processor.priority:Integer=3"},
	service = FragmentEntryProcessor.class
)
public class PortletFragmentEntryProcessor implements FragmentEntryProcessor {

	@Override
	public String processFragmentEntryHTML(String html, JSONObject jsonObject)
		throws PortalException {

		Document document = Jsoup.parseBodyFragment(html);

		for (Element element : document.select("*")) {
			String tagName = element.tagName();

			if (!StringUtil.startsWith(tagName, "lfr-app-")) {
				continue;
			}

			String alias = StringUtil.replace(
				tagName, "lfr-app-", StringPool.BLANK);

			Portlet portlet = _portletRegistry.getPortlet(alias);

			if (portlet == null) {
				throw new FragmentEntryContentException(
					LanguageUtil.format(
						_resourceBundle,
						"there-is-no-portlet-available-for-alias-x", alias));
			}

			Element runtimeHTMLTag = new Element("@liferay_portlet.runtime");

			runtimeHTMLTag.attr("portletName", portlet);

			element.replaceWith(runtimeHTMLTag);
		}

		Element bodyElement = document.body();

		return bodyElement.html();
	}

	@Override
	public void validateFragmentEntryHTML(String html) throws PortalException {
		Document document = Jsoup.parseBodyFragment(html);

		for (Element element : document.select("*")) {
			String htmlTagName = element.tagName();

			if (!StringUtil.startsWith(htmlTagName, "lfr-app-")) {
				continue;
			}

			String alias = StringUtil.replace(
				htmlTagName, "lfr-app-", StringPool.BLANK);

			Portlet portlet = _portletRegistry.getPortlet(alias);

			if (portlet == null) {
				throw new FragmentEntryContentException(
					LanguageUtil.format(
						_resourceBundle,
						"there-is-no-portlet-available-for-alias-x", alias));
			}
		}
	}

	@Reference
	private PortletRegistry _portletRegistry;

	private final ResourceBundle _resourceBundle = ResourceBundleUtil.getBundle(
		"content.Language", getClass());

}