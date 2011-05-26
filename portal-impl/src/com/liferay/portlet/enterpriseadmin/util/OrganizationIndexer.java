/**
 * Copyright (c) 2000-2011 Liferay, Inc. All rights reserved.
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

package com.liferay.portlet.enterpriseadmin.util;

import com.liferay.portal.kernel.search.BaseIndexer;
import com.liferay.portal.kernel.search.BooleanClauseOccur;
import com.liferay.portal.kernel.search.BooleanQuery;
import com.liferay.portal.kernel.search.BooleanQueryFactoryUtil;
import com.liferay.portal.kernel.search.Document;
import com.liferay.portal.kernel.search.DocumentImpl;
import com.liferay.portal.kernel.search.Field;
import com.liferay.portal.kernel.search.SearchContext;
import com.liferay.portal.kernel.search.SearchEngineUtil;
import com.liferay.portal.kernel.search.Summary;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.model.Organization;
import com.liferay.portal.service.OrganizationLocalServiceUtil;
import com.liferay.portal.util.PortletKeys;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.portlet.PortletURL;

/**
 * @author Raymond Augé
 * @author Zsigmond Rab
 * @author Hugo Huijser
 */
public class OrganizationIndexer extends BaseIndexer {

	public static final String[] CLASS_NAMES = {Organization.class.getName()};

	public static final String PORTLET_ID =
		PortletKeys.ENTERPRISE_ADMIN_ORGANIZATIONS;

	public OrganizationIndexer() {
		setStagingAware(false);
	}

	public String[] getClassNames() {
		return CLASS_NAMES;
	}

	public void postProcessContextQuery(
			BooleanQuery contextQuery, SearchContext searchContext)
		throws Exception {

		LinkedHashMap<String, Object> params =
			(LinkedHashMap<String, Object>)searchContext.getAttribute("params");

		if (params != null) {
			Long[][] leftAndRightOrganizationIds = (Long[][])params.get(
				"organizationsTree");

			if (leftAndRightOrganizationIds != null) {
				BooleanQuery organizationsTreeQuery =
					BooleanQueryFactoryUtil.create();

				if (leftAndRightOrganizationIds.length == 0) {
					organizationsTreeQuery.addRequiredTerm(
						Field.ORGANIZATION_ID, -1);
				}
				else if (leftAndRightOrganizationIds.length > 0) {
					for (Long[] leftAndRightOrganizationId :
							leftAndRightOrganizationIds) {

						organizationsTreeQuery.addNumericRangeTerm(
							"leftOrganizationId", leftAndRightOrganizationId[0],
							leftAndRightOrganizationId[1]);
					}
				}

				contextQuery.add(
					organizationsTreeQuery, BooleanClauseOccur.MUST);
			}
		}
	}

	public void postProcessSearchQuery(
			BooleanQuery searchQuery, SearchContext searchContext)
		throws Exception {

		addSearchTerm(searchQuery, searchContext, "city", true);
		addSearchTerm(searchQuery, searchContext, "country", true);
		addSearchTerm(searchQuery, searchContext, "name", true);
		addSearchTerm(
			searchQuery, searchContext, "parentOrganizationId", false);
		addSearchTerm(searchQuery, searchContext, "region", true);
		addSearchTerm(searchQuery, searchContext, "street", true);
		addSearchTerm(searchQuery, searchContext, "type", true);
		addSearchTerm(searchQuery, searchContext, "zip", true);

		LinkedHashMap<String, Object> params =
			(LinkedHashMap<String, Object>)searchContext.getAttribute("params");

		if (params != null) {
			String expandoAttributes = (String)params.get("expandoAttributes");

			if (Validator.isNotNull(expandoAttributes)) {
				addSearchExpando(searchQuery, searchContext, expandoAttributes);
			}
		}
	}

	protected void doDelete(Object obj) throws Exception {
		Organization organization = (Organization)obj;

		Document document = new DocumentImpl();

		document.addUID(PORTLET_ID, organization.getOrganizationId());

		SearchEngineUtil.deleteDocument(
			organization.getCompanyId(), document.get(Field.UID));
	}

	protected Document doGetDocument(Object obj) throws Exception {
		Organization organization = (Organization)obj;

		Document document = getBaseModelDocument(PORTLET_ID, organization);

		document.addKeyword(Field.COMPANY_ID, organization.getCompanyId());
		document.addText(Field.NAME, organization.getName());
		document.addKeyword(
			Field.ORGANIZATION_ID, organization.getOrganizationId());
		document.addKeyword(Field.TYPE, organization.getType());

		document.addNumber(
			"leftOrganizationId", organization.getLeftOrganizationId());
		document.addKeyword(
			"parentOrganizationId", organization.getParentOrganizationId());
		document.addNumber(
			"rightOrganizationId", organization.getRightOrganizationId());

		populateAddresses(
			document, organization.getAddresses(), organization.getRegionId(),
			organization.getCountryId());

		return document;
	}

	protected String doGetSortField(String orderByCol) {
		if (orderByCol.equals("name")) {
			return "name";
		}
		else if (orderByCol.equals("type")) {
			return "type";
		}
		else {
			return orderByCol;
		}
	}

	protected Summary doGetSummary(
		Document document, Locale locale, String snippet,
		PortletURL portletURL) {

		String title = document.get("name");

		String content = null;

		String organizationId = document.get(Field.ORGANIZATION_ID);

		portletURL.setParameter(
			"struts_action", "/enterprise_admin/edit_organization");
		portletURL.setParameter("organizationId", organizationId);

		return new Summary(title, content, portletURL);
	}

	protected void doReindex(Object obj) throws Exception {
		if (obj instanceof List<?>) {
			List<Organization> organizations = (List<Organization>)obj;

			for (Organization organization : organizations) {
				doReindex(organization);
			}
		}
		else if (obj instanceof Long) {
			long organizationId = (Long)obj;

			Organization organization =
				OrganizationLocalServiceUtil.getOrganization(organizationId);

			doReindex(organization);
		}
		else if (obj instanceof long[]) {
			long[] organizationIds = (long[])obj;

			Map<Long, Collection<Document>> documentsMap =
				new HashMap<Long, Collection<Document>>();

			for (long organizationId : organizationIds) {
				Organization organization =
					OrganizationLocalServiceUtil.getOrganization(
						organizationId);

				Document document = getDocument(organization);

				long companyId = organization.getCompanyId();

				Collection<Document> documents = documentsMap.get(companyId);

				if (documents == null) {
					documents = new ArrayList<Document>();

					documentsMap.put(companyId, documents);
				}

				documents.add(document);
			}

			for (Map.Entry<Long, Collection<Document>> entry :
					documentsMap.entrySet()) {

				long companyId = entry.getKey();
				Collection<Document> documents = entry.getValue();

				SearchEngineUtil.updateDocuments(companyId, documents);
			}
		}
		else if (obj instanceof Organization) {
			Organization organization = (Organization)obj;

			Document document = getDocument(organization);

			SearchEngineUtil.updateDocument(
				organization.getCompanyId(), document);
		}
	}

	protected void doReindex(String className, long classPK) throws Exception {
		Organization organization =
			OrganizationLocalServiceUtil.getOrganization(classPK);

		doReindex(organization);
	}

	protected void doReindex(String[] ids) throws Exception {
		long companyId = GetterUtil.getLong(ids[0]);

		reindexOrganizations(companyId);
	}

	protected String getPortletId(SearchContext searchContext) {
		return PORTLET_ID;
	}

	protected void reindexOrganizations(long companyId) throws Exception {
		int count = OrganizationLocalServiceUtil.getOrganizationsCount();

		int pages = count / OrganizationIndexer.DEFAULT_INTERVAL;

		for (int i = 0; i <= pages; i++) {
			int start = (i * OrganizationIndexer.DEFAULT_INTERVAL);
			int end = start + OrganizationIndexer.DEFAULT_INTERVAL;

			reindexOrganizations(companyId, start, end);
		}
	}

	protected void reindexOrganizations(long companyId, int start, int end)
		throws Exception {

		List<Organization> organizations =
			OrganizationLocalServiceUtil.getOrganizations(start, end);

		if (organizations.isEmpty()) {
			return;
		}

		Collection<Document> documents = new ArrayList<Document>();

		for (Organization organization : organizations) {
			Document document = getDocument(organization);

			documents.add(document);
		}

		SearchEngineUtil.updateDocuments(companyId, documents);
	}

}