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

import com.google.api.client.googleapis.media.MediaHttpDownloader;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.storage.Storage;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import static junit.framework.TestCase.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

/**
 * @author Zeger Hoogeboom
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({MediaHttpDownloader.class, Storage.Objects.Get.class, Storage.class, HttpRequestFactory.class, JacksonFactory.class, IOUtils.class})
public class GCSAdapterTest
{
	GCSAdapter adapter;
	File p12;
	Storage mockedClient;
	String bucketName;
	String path;
	HttpTransport mockedTransport;
	Storage.Objects objectsMock;

	@Before
	public void setUp() throws Exception
	{
		bucketName = "test";
		path = "file.txt";
		p12 = new File(System.getProperty("user.dir")+"/src/test/files/key.p12");

		mockStatic(IOUtils.class);
		setupMockedClient();
		adapter = new GCSAdapter.Builder()
				.setClient(mockedClient)
				.setBucket("ornate-shine-613")
				.build();
	}

	private void setupMockedClient()
	{
		mockedClient = mock(Storage.class);
		objectsMock = mock(Storage.Objects.class);
		mockedTransport = mock(HttpTransport.class);

		when(mockedClient.objects()).thenReturn(objectsMock);
		when(mockedClient.getJsonFactory()).thenReturn(mock(JacksonFactory.class));

		HttpRequestFactory mockedRequestFactory = mock(HttpRequestFactory.class); //setClient() uses this to retrieve the http transport
		when(mockedRequestFactory.getTransport()).thenReturn(mockedTransport);
		when(mockedClient.getRequestFactory()).thenReturn(mockedRequestFactory);
	}

	@Test
	public void read() throws IOException
	{
		Storage.Objects.Get getMock = mock(Storage.Objects.Get.class);
		MediaHttpDownloader httpMock = mock(MediaHttpDownloader.class);
		InputStream streamMock = mock(InputStream.class);

		when(getMock.getMediaHttpDownloader()).thenReturn(httpMock);
		when(getMock.executeMediaAsInputStream()).thenReturn(streamMock);
		when(objectsMock.get(anyString(), anyString())).thenReturn(getMock);
		when(httpMock.setDirectDownloadEnabled(true)).thenReturn(httpMock);
		when(IOUtils.toString(Matchers.<InputStream>any(), Matchers.<String>any())).thenReturn("contents");

		String read = adapter.read(path);
		assertEquals("contents", read);
	}


}
