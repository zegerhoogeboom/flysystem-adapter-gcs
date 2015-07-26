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

import com.flysystem.core.FileMetadata;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static junit.framework.TestCase.assertEquals;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * @author Zeger Hoogeboom
 */
public class GCSAdapterTestRealData
{
	GCSAdapter adapter;
	String path;


	@Before
	public void setUp() throws Exception
	{
		path = "test.txt";
		adapter = GCSTestHelper.getAdapter();
	}

	@Test
	public void read()
	{
		String read = adapter.read(path);
		assertEquals("test!", read);
	}

	@Test
	public void write()
	{
		boolean write = adapter.write("temp.txt", "temp");
		assertTrue(write);
	}


	@Test
	public void canWriteReadAndDeleteFile()
	{
		assertFalse(adapter.has("temp.txt"));
		adapter.write("temp.txt", "temp");
		assertTrue(adapter.has("temp.txt"));
		adapter.delete("temp.txt");
		assertFalse(adapter.has("temp.txt"));
	}

	@Test
	public void copy()
	{
		assertFalse(adapter.has("newexample.txt"));
		adapter.copy(path, "newexample.txt");
		assertTrue(adapter.has("newexample.txt"));
		adapter.delete("newexample.txt");
	}

	@Test
	public void listContents()
	{
		List<FileMetadata> files = adapter.listContents("", true);
		assertThat(files, hasItem(new FileMetadata(path)));
	}

	@Test
	public void getSize()
	{
		adapter.write("temp.txt", "test");
		long size = adapter.getSize("temp.txt");
		assertEquals(size, 4);
		adapter.delete("temp.txt");
	}

}
