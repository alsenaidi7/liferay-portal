/**
 * Copyright (c) 2000-2009 Liferay, Inc. All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.liferay.portal.dao.jdbc.util;

import com.liferay.portal.kernel.jndi.JNDIUtil;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.PropertiesUtil;
import com.liferay.portal.kernel.util.SortedProperties;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.util.PropsUtil;
import com.liferay.portal.util.PropsValues;

import com.mchange.v2.c3p0.ComboPooledDataSource;

import java.util.Enumeration;
import java.util.Properties;

import javax.naming.InitialContext;

import javax.sql.DataSource;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.dbcp.BasicDataSourceFactory;

import org.springframework.beans.factory.config.AbstractFactoryBean;

import uk.org.primrose.pool.datasource.GenericDataSourceFactory;

/**
 * <a href="DataSourceFactoryBean.java.html"><b><i>View Source</i></b></a>
 *
 * @author Brian Wing Shun Chan
 */
public class DataSourceFactoryBean extends AbstractFactoryBean {

	public Object createInstance() throws Exception {
		Properties properties = _properties;

		if (properties == null) {
			properties = PropsUtil.getProperties(_propertyPrefix, true);
		}
		else {
			properties = PropertiesUtil.getProperties(
				properties, _propertyPrefix, true);
		}

		String jndiName = properties.getProperty("jndi.name");

		if (Validator.isNotNull(jndiName)) {
			try {
				return JNDIUtil.lookup(new InitialContext(), jndiName);
			}
			catch (Exception e) {
				_log.error("Unable to lookup " + jndiName, e);
			}
		}

		DataSource dataSource = null;

		String liferayPoolProvider =
			PropsValues.JDBC_DEFAULT_LIFERAY_POOL_PROVIDER;

		if (liferayPoolProvider.equals("c3po")) {
			dataSource = createDataSourceC3PO(properties);
		}
		else if (liferayPoolProvider.equals("dbcp")) {
			dataSource = createDataSourceDBCP(properties);
		}
		else {
			dataSource = createDataSourcePrimrose(properties);
		}

		if (_log.isDebugEnabled()) {
			_log.debug(
				"Creating data source " + dataSource.getClass().getName());

			SortedProperties sortedProperties = new SortedProperties(
				properties);

			_log.debug("Properties for prefix " + _propertyPrefix);

			sortedProperties.list(System.out);
		}

		return dataSource;
	}

	public Class<?> getObjectType() {
		return DataSource.class;
	}

	public void setProperties(Properties properties) {
		_properties = properties;
	}

	public void setPropertyPrefix(String propertyPrefix) {
		_propertyPrefix = propertyPrefix;
	}

	public void setPropertyPrefixLookup(String propertyPrefixLookup) {
		_propertyPrefix = PropsUtil.get(propertyPrefixLookup);
	}

	protected DataSource createDataSourceC3PO(Properties properties)
		throws Exception {

		DataSource dataSource = new ComboPooledDataSource();

		Enumeration<String> enu =
			(Enumeration<String>)properties.propertyNames();

		while (enu.hasMoreElements()) {
			String key = enu.nextElement();

			String value = properties.getProperty(key);

			// Map org.apache.commons.dbcp.BasicDataSource to C3PO

			if (key.equalsIgnoreCase("driverClassName")) {
				key = "driverClass";
			}
			else if (key.equalsIgnoreCase("url")) {
				key = "jdbcUrl";
			}
			else if (key.equalsIgnoreCase("username")) {
				key = "user";
			}

			BeanUtils.setProperty(dataSource, key, value);
		}

		return dataSource;
	}

	protected DataSource createDataSourceDBCP(Properties properties)
		throws Exception {

		return BasicDataSourceFactory.createDataSource(properties);
	}

	protected DataSource createDataSourcePrimrose(Properties properties)
		throws Exception {

		properties.setProperty("poolName", _propertyPrefix);

		Enumeration<String> enu =
			(Enumeration<String>)properties.propertyNames();

		while (enu.hasMoreElements()) {
			String key = enu.nextElement();

			String value = properties.getProperty(key);

			// Map org.apache.commons.dbcp.BasicDataSource to Primrose

			if (key.equalsIgnoreCase("driverClassName")) {
				key = "driverClass";
			}
			else if (key.equalsIgnoreCase("url")) {
				key = "driverURL";
			}
			else if (key.equalsIgnoreCase("username")) {
				key = "user";
			}

			properties.setProperty(key, value);
		}

		GenericDataSourceFactory genericDataSourceFactory =
			new GenericDataSourceFactory();

		return genericDataSourceFactory.loadPool(_propertyPrefix, properties);
	}

	private static Log _log =
		LogFactoryUtil.getLog(DataSourceFactoryBean.class);

	private Properties _properties;
	private String _propertyPrefix;

}