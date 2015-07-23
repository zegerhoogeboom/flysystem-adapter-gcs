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
import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.Handler;
import java.util.logging.Logger;
import java.util.logging.StreamHandler;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertThat;

/**
 * todo test the setClient();
 * @author Zeger Hoogeboom
 */
public class GCSAdapterBuilderTest
{
	GCSAdapter adapter;
	File p12;

	@Before
	public void setUp() throws Exception
	{
		p12 = new File(System.getProperty("user.dir")+"/src/test/files/key.p12");
	}

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	private static Logger log = Logger.getLogger(GCSAdapter.class.getName()); // matches the logger in the affected class
	private static OutputStream logCapturingStream;
	private static StreamHandler customLogHandler;

	@Before
	public void attachLogCapturer() //for java.util.Logger log capturing
	{
		logCapturingStream = new ByteArrayOutputStream();
		Handler[] handlers = log.getParent().getHandlers();
		customLogHandler = new StreamHandler(logCapturingStream, handlers[0].getFormatter());
		log.addHandler(customLogHandler);
	}

	public String getTestCapturedLog() throws IOException
	{
		customLogHandler.flush();
		return logCapturingStream.toString();
	}


	@Test
	public void buildNoBucketname() {
		thrown.expect(GCSConnectionException.class);
		thrown.expectMessage("Bucket name has to be provided.");
		adapter = new GCSAdapter.Builder().build();
	}

	@Test
	public void buildNoServiceAccount() {
		thrown.expect(GCSConnectionException.class);
		thrown.expectMessage("Service account email has to be provided.");
		adapter = new GCSAdapter.Builder()
				.setBucket("")
				.build();
	}

	@Test
	public void buildNoP12Key() {
		thrown.expect(GCSConnectionException.class);
		thrown.expectMessage("A .p12 key has to be provided.");
		adapter = new GCSAdapter.Builder()
				.setBucket("")
				.setServiceAccountEmail("")
				.build();
	}

	@Test
	public void buildNoApplicationName() throws IOException
	{
		adapter = new GCSAdapter.Builder()
				.setBucket("")
				.setServiceAccountEmail("")
				.setP12Key(p12)
				.build();
		assertThat(getTestCapturedLog(), CoreMatchers.containsString("You didn't set your Application name. GCS will log warning messages because of this! Suggested format is \"MyCompany-ProductName/1.0\"."));
	}

	@Test
	public void buildWithAllOptions() throws IOException
	{
		adapter = new GCSAdapter.Builder()
				.setBucket("bucket")
				.setServiceAccountEmail("clientId")
				.setP12Key(p12)
				.setApplicationName("applicationName")
				.build();
		assertEquals(getTestCapturedLog(), "");
	}
}
