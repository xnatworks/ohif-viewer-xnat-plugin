package org.nrg.xnatx.dicomweb.toolkit;

import icr.etherj.Displayable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.PrintStream;

/**
 * @author m.alsad
 *
 * A Spring implementation of the Jersey WebApplicationException
 */
public class WebApplicationException extends RuntimeException
	implements Displayable
{
	private static final long serialVersionUID = 6869762566539253924L;
	private final ResponseEntity<?> response;

	public WebApplicationException()
	{
		this((Throwable) null, HttpStatus.INTERNAL_SERVER_ERROR);
	}

	public WebApplicationException(String message)
	{
		this(message, null, HttpStatus.INTERNAL_SERVER_ERROR);
	}

	public WebApplicationException(ResponseEntity<?> response)
	{
		this.response = response;
	}

	public WebApplicationException(final String message,
		final ResponseEntity<?> response)
	{
		this(message, null, response);
	}

	public WebApplicationException(final String message, final HttpStatus status)
	{
		this(message, null, new ResponseEntity<>(status));
	}

	public WebApplicationException(final int status)
	{
		this((Throwable) null, HttpStatus.valueOf(status));
	}

	public WebApplicationException(final String message, final Throwable cause,
		final HttpStatus status)
	{
		this(message, cause, new ResponseEntity<>(status));
	}

	public WebApplicationException(final String message, final Throwable cause,
		final ResponseEntity<?> response)
	{
		super(message, cause);
		if (response == null)
		{
			this.response = new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
		else
		{
			this.response = response;
		}
	}

	public WebApplicationException(final Throwable cause, final HttpStatus status)
	{
		this(cause, new ResponseEntity<>(status));
	}

	public WebApplicationException(final Throwable cause,
		final ResponseEntity<?> response)
	{
		this(computeExceptionMessage(response), cause, response);
	}

	@Override
	public void display()
	{
		display(System.out, "", false);
	}

	@Override
	public void display(boolean recurse)
	{
		display(System.out, "", recurse);
	}

	@Override
	public void display(String indent)
	{
		display(System.out, indent, false);
	}

	@Override
	public void display(String indent, boolean recurse)
	{
		display(System.out, indent, recurse);
	}

	@Override
	public void display(PrintStream ps)
	{
		display(ps, "", false);
	}

	@Override
	public void display(PrintStream ps, boolean recurse)
	{
		display(ps, "", recurse);
	}

	@Override
	public void display(PrintStream ps, String indent)
	{
		display(ps, indent, false);
	}

	@Override
	public void display(PrintStream ps, String indent, boolean recurse)
	{
		ps.println(indent+getClass().getName());
		String pad = indent+"  * ";
		ps.println(pad+"Message: "+getMessage());
		Throwable cause = getCause();
		if (cause != null)
		{
			ps.println(pad+"Cause: "+cause.getClass().getName());
		}
		StackTraceElement[] stackTrace = getStackTrace();
		for (StackTraceElement element : stackTrace)
		{
			ps.println(indent+"      "+element.toString());
		}
	}

	public ResponseEntity<?> getResponse()
	{
		return response;
	}

	private static String computeExceptionMessage(ResponseEntity<?> response)
	{
		final HttpStatus status;
		if (response != null)
		{
			status = response.getStatusCode();
		}
		else
		{
			status = HttpStatus.INTERNAL_SERVER_ERROR;
		}
		return "HTTP " + status.value() + ' ' + status.getReasonPhrase();
	}
}
