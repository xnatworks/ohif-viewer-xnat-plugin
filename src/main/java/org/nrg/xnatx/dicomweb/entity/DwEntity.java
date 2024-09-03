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
package org.nrg.xnatx.dicomweb.entity;

import org.dcm4che3.data.Attributes;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntity;
import org.nrg.xnatx.dicomweb.conf.DicomwebDeviceConfiguration;
import org.nrg.xnatx.dicomweb.toolkit.DicomwebUtils;

import javax.persistence.*;
import java.io.IOException;

@MappedSuperclass
public abstract class DwEntity extends AbstractHibernateEntity
{
	private byte[] encodedAttributes;
	private String revision;
	@Transient
	private Attributes cachedAttributes;

	@Override
	public boolean equals(Object o)
	{
		if (this == o)
		{
			return true;
		}
		if (o == null || getClass() != o.getClass())
		{
			return false;
		}
		final AbstractHibernateEntity that = (AbstractHibernateEntity) o;
		return getId() == that.getId();
	}

	@Transient
	public Attributes getAttributes() throws IOException
	{
		if (cachedAttributes == null)
		{
			cachedAttributes = DicomwebUtils.decodeAttributes(encodedAttributes);
		}

		return cachedAttributes;
	}

	@Transient
	protected void setAttributes(Attributes attrs) throws IOException
	{
		cachedAttributes = new Attributes(attrs);
		encodedAttributes = DicomwebUtils.encodeAttributes(cachedAttributes);

		revision = DicomwebDeviceConfiguration.DICOMWEB_DATA_REVISION;
	}

	@Transient
	public abstract Attributes getQueryAttributes();

	@Basic(optional = false)
	public String getRevision()
	{
		return revision;
	}

	public void setRevision(String revision)
	{
		this.revision = revision;
	}

	public abstract void setData(Attributes attrs) throws IOException;

//	public abstract Attributes toAttributes() throws IOException;

	@Basic(optional = false)
	@Column(name = "attrs")
	private byte[] getEncodedAttributes()
	{
		return encodedAttributes;
	}

	private void setEncodedAttributes(byte[] encodedAttributes)
	{
		this.encodedAttributes = encodedAttributes;
	}
}
