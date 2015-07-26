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

import com.google.api.client.http.javanet.NetHttpTransport;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * @author Zeger Hoogeboom
 */
public class GCSTestHelper
{

	private static Properties getProperties() throws Exception
	{
		Properties properties = new Properties();
		try (InputStream stream = GCSTestHelper.class.getResourceAsStream("/credentials.properties")) {
			properties.load(stream);
		} catch (IOException e) {
			throw new RuntimeException("credentials.properties must be present in classpath", e);
		}
		return properties;
	}

	public static GCSAdapter getAdapter()
	{
		Properties properties;
		try {
			properties = getProperties();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return new GCSAdapter.Builder()
				.setBucket(properties.getProperty("bucket"))
				.setP12Key(new File(properties.getProperty("p12")))
				.setServiceAccountEmail(properties.getProperty("serviceAccountEmail"))
				.setHttpTransport(new NetHttpTransport())
				.build();
	}
}
