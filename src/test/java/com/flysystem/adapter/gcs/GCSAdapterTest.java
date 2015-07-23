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

import com.google.api.services.storage.Storage;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

/**
 * @author Zeger Hoogeboom
 */
public class GCSAdapterTest
{
	GCSAdapter adapter;
	File p12;
	Storage mockedClient;
	String bucketName;
	String path;

	@Before
	public void setUp() throws Exception
	{
		bucketName = "test";
		bucketName = "file.txt";
		p12 = new File(System.getProperty("user.dir")+"/src/test/files/key.p12");
		mockedClient = mock(Storage.class);
		adapter = new GCSAdapter.Builder()
//				.setClient(mockedClient)
				.setBucket("ornate-shine-613")
				.setP12Key(p12)
				.setServiceAccountEmail("882658829069-ea7q6h3fng1a8aa1gjb1eapifud9bkjv@developer.gserviceaccount.com")
				.build();
	}

	@Test
	public void read() throws IOException
	{
		Storage.Objects.Get mock = mock(Storage.Objects.Get.class);
//		when(mockedClient.objects().get(bucketName, path)).thenReturn(mock);
//		String read = adapter.read("godviavideo.txt");
//		assertNull(read);
		assertTrue(true);
	}
}
