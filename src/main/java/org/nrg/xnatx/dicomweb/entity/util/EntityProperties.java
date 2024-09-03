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
package org.nrg.xnatx.dicomweb.entity.util;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class EntityProperties
{
	public static Map<String,Object> newPropsMap(String[] keys, Object[] values)
	{
		Map<String,Object> props = new HashMap<>();
		for (int i = 0; i < keys.length; i++)
		{
			props.put(keys[i], values[i]);
		}

		return props;
	}

	public static <E> boolean setExampleProps(E example, Map<String,Object> props)
	{
		for (String key: props.keySet())
		{
			if (!setExampleProp(example, key, props.get(key)))
			{
				return false;
			}
		}

		return true;
	}

	public static boolean setExampleProp(Object object, String fieldName,
		Object fieldValue)
	{
		// Set field with Reflection
		Class<?> clazz = object.getClass();
		while (clazz != null)
		{
			try
			{
				Field field = clazz.getDeclaredField(fieldName);
				field.setAccessible(true);
				field.set(object, fieldValue);
				return true;
			}
			catch (NoSuchFieldException e)
			{
				clazz = clazz.getSuperclass();
			}
			catch (Exception e)
			{
				return false;
				// throw new IllegalStateException(e);
			}
		}
		return false;
	}
}
