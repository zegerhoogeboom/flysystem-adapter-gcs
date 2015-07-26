/*
 * Copyright (c) 2013-2015 Frank de Jonge
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is furnished
 * to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.flysystem.adapter.gcs;

import com.flysystem.adapter.gcs.exception.GCSConnectionException;
import com.flysystem.core.Config;
import com.flysystem.core.FileMetadata;
import com.flysystem.core.Visibility;
import com.flysystem.core.adapter.AbstractAdapter;
import com.flysystem.core.exception.FileExistsException;
import com.flysystem.core.exception.FileNotFoundException;
import com.flysystem.core.exception.FlysystemGenericException;
import com.flysystem.core.util.PathUtil;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.InputStreamContent;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.storage.Storage;
import com.google.api.services.storage.StorageScopes;
import com.google.api.services.storage.model.Bucket;
import com.google.api.services.storage.model.StorageObject;
import org.apache.commons.io.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

/**
 * @author Zeger Hoogeboom
 */
public class GCSAdapter extends AbstractAdapter
{
	private final static Logger logger = Logger.getLogger(GCSAdapter.class.getName());
	private JsonFactory jsonFactory;
	private HttpTransport httpTransport;

	private String applicationName;
	private String bucketName;
	private String serviceAccountEmail;
	private File p12Key;
	private boolean AUTH_LOCAL_WEBSERVER = true;

	private Storage client;

	private GCSAdapter(){}

	public static class Builder {
		GCSAdapter adapter;

		public Builder()
		{
			adapter = new GCSAdapter();
		}

		public Builder setJsonFactory(JsonFactory factory) {
			adapter.jsonFactory = factory;
			return this;
		}
		public Builder setHttpTransport(HttpTransport httpTransport) {
			adapter.httpTransport = httpTransport;
			return this;
		}
		public Builder setApplicationName(String applicationName) {
			adapter.applicationName = applicationName;
			return this;
		}
		public Builder setBucket(String bucket) {
			adapter.bucketName = bucket;
			return this;
		}
		public Builder setServiceAccountEmail(String serviceAccountEmail) {
			adapter.serviceAccountEmail = serviceAccountEmail;
			return this;
		}

		/**
		 * The expected path of your .p12 key is ${rootOfProject}/keyName.
	     * ${rootOfProject} is retrieved with the variable System.getProperty("user.dir").
		 * In case you want your .p12 key to reside elsewhere, please see the method {@code setP12Key(File key)}
		 * @param keyName The name of your key. e.g. "mykey.p12".
		 * @return GCSAdapter.Builder
		 */
		public Builder setP12Key(String keyName)
		{
			adapter.p12Key = new File(String.format("%s/%s",System.getProperty("user.dir"), keyName));
			return this;
		}

		public Builder setP12Key(File key)
		{
			adapter.p12Key = key;
			return this;
		}

		/**
		 * This method should not be used in regular use cases.
		 * Setting the client yourself is useful when for example you want to use a different authentication mechanism than service accounts.
		 * When the client is set, no other properties are required to be set any longer.
		 * @param client
		 * @return
		 */
		public Builder setClient(Storage client) {
			adapter.client = client;
			adapter.applicationName = client.getApplicationName();
			adapter.jsonFactory = client.getJsonFactory();
			adapter.httpTransport = client.getRequestFactory().getTransport();
			return this;
		}

		private void withDefaults()
		{
			if (adapter.httpTransport == null) {
				try {
					setHttpTransport(GoogleNetHttpTransport.newTrustedTransport());
				} catch (GeneralSecurityException | IOException e) {
					throw new FlysystemGenericException(e);
				}
			}
			if (adapter.jsonFactory == null) setJsonFactory(JacksonFactory.getDefaultInstance());

			if (adapter.client == null) {
				try {
					adapter.client = new Storage.Builder(adapter.httpTransport, adapter.jsonFactory, adapter.authorize())
							.setApplicationName(adapter.applicationName).build();
				} catch (IOException e) {
					throw new FlysystemGenericException(e);
				}
			}
		}

		public GCSAdapter build()
		{
			if (adapter.bucketName == null && adapter.client == null) throw new GCSConnectionException("Bucket name has to be provided.");
			if (adapter.serviceAccountEmail == null && adapter.client == null) throw new GCSConnectionException("Service account email has to be provided.");
			if (adapter.p12Key == null && adapter.client == null) throw new GCSConnectionException("A .p12 key has to be provided.");
			if (adapter.applicationName == null && adapter.client == null) logger.warning("You didn't set your Application name. GCS will log warning messages because of this! Suggested format is \"MyCompany-ProductName/1.0\".");
			withDefaults();
			return adapter;
		}
	}

	private Credential authorize() throws IOException
	{
		try {
			return new GoogleCredential.Builder().setTransport(httpTransport)
					.setJsonFactory(jsonFactory)
					.setServiceAccountId(serviceAccountEmail)
					.setServiceAccountScopes(Collections.singleton(StorageScopes.DEVSTORAGE_FULL_CONTROL))
					.setServiceAccountPrivateKeyFromP12File(p12Key)
					.build();
		} catch (GeneralSecurityException e) {
			throw new FlysystemGenericException(e);
		}
	}

	public boolean has(String path)
	{
		return false;
	}

	public String read(String path) throws FileNotFoundException
	{
		try {
			Storage.Objects.Get execute = client.objects().get(bucketName, path);
			execute.getMediaHttpDownloader().setDirectDownloadEnabled(true);
			InputStream stream = execute.executeMediaAsInputStream();
			return IOUtils.toString(stream, "UTF-8");
		} catch (IOException e) {
			throw new FlysystemGenericException(e);
		}
	}

	private String readInputStream(InputStream stream)
	{
	   return null;
	}

	public List<FileMetadata> listContents(String directory, boolean recursive)
	{
		return null;
	}

	public FileMetadata getMetadata(String path)
	{
		return null;
	}

	public Long getSize(String path)
	{
		return getObject(path).getSize().longValue();
	}

	public String getMimetype(String path)
	{
		return getObject(path).getContentType();
	}

	public Long getTimestamp(String path)
	{
		return getObject(path).getUpdated().getValue();
	}

	public Visibility getVisibility(String path)
	{
		return null;
	}

	public boolean write(String path, String contents, Config config)
	{
		return false;
	}

	public boolean write(String path, String contents)
	{
		try {
			StorageObject object = new StorageObject();
			object.setContentType(PathUtil.guessMimeType(path));
			InputStreamContent mediaContent = new InputStreamContent("application/octet-stream", new ByteArrayInputStream(contents.getBytes()));
			mediaContent.setLength(contents.length());
			client.objects().insert(bucketName, object, mediaContent);
			return true;
		} catch (IOException e) {
			throw new FlysystemGenericException(e);
		}
	}

	public boolean update(String path, String contents)
	{
		return false;
	}

	public boolean update(String path, String contents, Config config)
	{
		return false;
	}

	public boolean rename(String from, String to) throws FileExistsException, FileNotFoundException
	{
		return false;
	}

	public boolean copy(String path, String newpath)
	{
//		client.objects().copy(bucketName, path, bucketName, newpath).execute();
		return true;
	}

	public boolean delete(String path)
	{
		try {
			client.objects().delete(bucketName, path);
		} catch (IOException e) {
			throw new FlysystemGenericException(e);
		}
		return true;
	}

	public boolean deleteDir(String dirname)
	{
		return false;
	}

	public boolean createDir(String dirname, Config config)
	{
		return false;
	}

	public boolean createDir(String dirname)
	{
		return false;
	}

	public boolean setVisibility(String path, Visibility visibility)
	{
		return false;
	}

	private Bucket getBucket()
	{
		try {
			Bucket execute = client.buckets().get(bucketName).execute();
			return execute;
		} catch (IOException e) {
			throw new FlysystemGenericException(e);
		}
	}

	private StorageObject getObject(String path)
	{
		try {
			return client.objects().get(bucketName, path).execute();
		} catch (IOException e) {
			throw new FlysystemGenericException(e);
		}
	}
}
