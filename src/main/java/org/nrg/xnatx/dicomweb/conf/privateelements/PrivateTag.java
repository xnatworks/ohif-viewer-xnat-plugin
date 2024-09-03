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
package org.nrg.xnatx.dicomweb.conf.privateelements;

public class PrivateTag
{
	public static final String PrivateCreator = "ICR XNAT Viewer";

	/**
	 * (7777,xx10) VR=LO VM=1 XNAT Project ID
	 */
	public static final int XNATProjectID = 0x77770010;

	/**
	 * (7777,xx11) VR=LO VM=1 XNAT Subject ID
	 */
	public static final int XNATSubjectID = 0x77770011;

	/**
	 * (7777,xx12) VR=LO VM=1 XNAT Experiment ID
	 */
	public static final int XNATExperimentID = 0x77770012;

	/**
	 * (7777,xx12) VR=LO VM=1 XNAT Scan ID
	 */
	public static final int XNATScanID = 0x77770013;

	/**
	 * (7777,xxFF) VR=UL VM=1-n Data Offsets
	 */
	public static final int DataOffsets = 0x777700FE;

	/**
	 * (7777,xxFF) VR=UL VM=1-n Data Lengths
	 */
	public static final int DataLengths = 0x777700FF;
}
