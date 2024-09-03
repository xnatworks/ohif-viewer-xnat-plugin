/********************************************************************
 * Copyright (c) 2023, Institute of Cancer Research
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * (1) Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 * (2) Redistributions in binary form must reproduce the above
 *     copyright notice, this list of conditions and the following
 *     disclaimer in the documentation and/or other materials provided
 *     with the distribution.
 *
 * (3) Neither the name of the Institute of Cancer Research nor the
 *     names of its contributors may be used to endorse or promote
 *     products derived from this software without specific prior
 *     written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *********************************************************************/
package org.nrg.xnatx.dicomweb.service.inputcreator;

import icr.etherj.AbstractPathScan;
import lombok.extern.slf4j.Slf4j;
import org.dcm4che2.data.Tag;
import org.dcm4che3.data.Attributes;
import org.nrg.xnatx.dicomweb.toolkit.IcrDicomFileReader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author m.alsad
 */
@Slf4j
public class DicomwebPathScan extends AbstractPathScan<Attributes>
{
	@Override
	public Attributes scanFile(Path file) throws IOException
	{
		if (Files.isDirectory(file) || !Files.isReadable(file))
		{
			log.warn("Not a file or cannot be read: {}", file);
			return null;
		}

		Attributes attrs = null;
		try
		{
			IcrDicomFileReader reader = new IcrDicomFileReader(
				file.toFile());
			attrs = reader.read();
		}
		catch (IOException exIO)
		{
			log.warn("Cannot scan file: " + file, exIO);
			throw exIO;
		}

		// Ignore null SOPClassUID
		if (attrs != null)
		{
			String cuid = attrs.getString(Tag.SOPClassUID);
			if (cuid == null)
			{
				return null;
			}
		}

		return attrs;
	}
}
