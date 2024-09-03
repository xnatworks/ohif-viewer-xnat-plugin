/*********************************************************************
 * Copyright (c) 2018, Institute of Cancer Research
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
package org.nrg.xnatx.plugin;

import icr.etherj.AbstractExceptionCode;

/**
 *
 * @author jamesd
 */
public final class PluginCode extends AbstractExceptionCode
{
	public static final PluginCode Success = new PluginCode("Success", "00000");
	// 01 IO
	public static final PluginCode IO = new PluginCode("IO", "01001");
	public static final PluginCode FileNotFound = new PluginCode(
		"File not found", "01002");
	// 02 XML
	public static final PluginCode XML = new PluginCode("XML", "02001");
	public static final PluginCode ParserConfiguration = new PluginCode(
		"Parser configuration", "02002");
	public static final PluginCode SAX = new PluginCode("SAX", "02003");
	// 03 - HTTP
	public static final PluginCode HttpInvalid = new PluginCode(
		"Invalid HTTP response", "03001");
	public static final PluginCode HttpNotImplementedYet = new PluginCode(
		"HTTP not implemented yet", "03002");
	public static final PluginCode HttpBadRequest = new PluginCode(
		"HTTP 400 bad request", "03400");
	public static final PluginCode HttpUnauthorised = new PluginCode(
		"HTTP 401 unauthorised", "03401");
	public static final PluginCode HttpForbidden = new PluginCode(
		"HTTP 403 forbidden", "03403");
	public static final PluginCode HttpNotFound = new PluginCode(
		"HTTP 404 not found", "03404");
	public static final PluginCode HttpMethodNotAllowed = new PluginCode(
		"HTTP 405 method not allowed", "03405");
	public static final PluginCode HttpMethodNotAcceptable = new PluginCode(
		"HTTP 406 method not Acceptable", "03406");
	public static final PluginCode HttpConflict = new PluginCode(
		"HTTP 409 conflict", "03409");
	public static final PluginCode HttpUnprocessableEntity = new PluginCode(
		"HTTP 422 unprocessable entity", "03422");
	public static final PluginCode HttpInternalError = new PluginCode(
		"HTTP 500 internal error", "03500");
	// In the DICOM context, this code shall be used for cases such as SOP Class Not Supported
	public static final PluginCode DICOMWebNotSupported = new PluginCode(
		"501 Not Implemented", "03501");
	// 04 General
	public static final PluginCode IllegalArgument = new PluginCode(
		"Illegal argument", "04001");
	public static final PluginCode IllegalState = new PluginCode("Illegal state",
		"04002");
	// 05 - Other
	public static final PluginCode Unsupported = new PluginCode("Unsupported",
		"05001");
	// 06 - XNAT
	public static final PluginCode XNAT = new PluginCode("XNAT", "06001");
	public static final PluginCode ConfigService = new PluginCode(
		"ConfigService", "06002");
	// 07 JSON
	public static final PluginCode JSON = new PluginCode("JSON", "07001");

	@Override
	public String toString()
	{
		return getMessage();
	}

	private PluginCode(String message, String code)
	{
		super(message, code);
	}
	
}
