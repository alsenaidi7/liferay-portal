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

/**
 * @author Peter Yoo
 */

package com.liferay.jenkins.results.parser;

import java.io.File;

public class RepositoryFactory {

	public static LocalRepository getLocalRepository(
		File repositoryDirectory, String repositoryName) {

		return new LocalRepository(repositoryDirectory, repositoryName);
	}

	public static RemoteRepository getRemoteRepository(
		GitWorkingDirectory.Remote remote) {

		String hostname = remote.getHostname();

		if (hostname.equalsIgnoreCase("github.com")) {
			return new GitHubRemoteRepository(remote);
		}

		return new RemoteRepository(remote);
	}

	public static RemoteRepository getRemoteRepository(
		String hostname, String repositoryName, String username) {

		if (hostname.equalsIgnoreCase("github.com")) {
			return new GitHubRemoteRepository(repositoryName, username);
		}

		return new RemoteRepository(hostname, repositoryName, username);
	}

}