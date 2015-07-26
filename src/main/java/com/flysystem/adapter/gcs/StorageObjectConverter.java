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
import com.flysystem.core.Visibility;
import com.flysystem.core.exception.FileNotFoundException;
import com.google.api.services.storage.model.ObjectAccessControl;
import com.google.api.services.storage.model.StorageObject;
import com.google.common.base.Converter;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Zeger Hoogeboom
 */
public class StorageObjectConverter extends Converter<StorageObject, FileMetadata>
{

	@Override
	protected FileMetadata doForward(StorageObject storageObject) throws FileNotFoundException
	{
		return new FileMetadata(storageObject.getName(),
				storageObject.getSize().longValue(),
				getVisibility(storageObject),
				storageObject.getContentType(),
				storageObject.getUpdated().getValue(),
				"file");
	}

	@Override
	protected StorageObject doBackward(FileMetadata fileMetadata)
	{
		return null;
	}

	private Visibility getVisibility(StorageObject object)
	{
		Visibility visibility = Visibility.PRIVATE;
		if (object.getAcl() != null) { //todo is always null =/!
			for (ObjectAccessControl objectAccessControl : object.getAcl()) {
				if (objectAccessControl.getEntity().equals("allUsers"))
					visibility = Visibility.PUBLIC;
			}
		}
		return visibility;
	}

	public static List<FileMetadata> doConvert(List<StorageObject> files)
	{
		StorageObjectConverter fileMetadataConverter = new StorageObjectConverter();
		List<FileMetadata> convertedFiles = new ArrayList<>();
		for (StorageObject file : files) {
			FileMetadata converted = fileMetadataConverter.convert(file);
			convertedFiles.add(converted);
		}
		return convertedFiles;
	}
}
