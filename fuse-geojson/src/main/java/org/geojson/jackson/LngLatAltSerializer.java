package org.geojson.jackson;

/*-
 * #%L
 * fuse-geojson
 * %%
 * Copyright (C) 2016 - 2018 kayhut
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.geojson.LngLatAlt;

import java.io.IOException;

public class LngLatAltSerializer extends JsonSerializer<LngLatAlt> {

	public static final long POW10[] = {1, 10, 100, 1000, 10000, 100000, 1000000, 10000000, 100000000, 1000000000};

	/**
	 * The following must convert double to String in a much more efficient way then Double.toString()
	 *
	 * @See http://stackoverflow.com/questions/10553710/fast-double-to-string-conversion-with-given-precision
	 * @param val
	 * @param precision
	 * @return
	 */
	protected static String fastDoubleToString(double val, int precision) {
		StringBuilder sb = new StringBuilder();
		if (val < 0) {
			sb.append('-');
			val = -val;
		}
		long exp = POW10[precision];
		long lval = (long)(val * exp + 0.5);
		sb.append(lval / exp).append('.');
		long fval = lval % exp;
		for (int p = precision - 1; p > 0 && fval < POW10[p] && fval>0; p--) {
			sb.append('0');
		}
		sb.append(fval);
		int i = sb.length()-1;
		while(sb.charAt(i)=='0' && sb.charAt(i-1)!='.')
		{
			sb.deleteCharAt(i);
			i--;
		}
		return sb.toString();
	}

	@Override
	public void serialize(LngLatAlt value, JsonGenerator jgen, SerializerProvider provider) throws IOException,
			JsonProcessingException {
		jgen.writeStartArray();
		jgen.writeNumber(fastDoubleToString(value.getLongitude(), 9));
		jgen.writeNumber(fastDoubleToString(value.getLatitude(), 9));
		if (value.hasAltitude())
			jgen.writeNumber(value.getAltitude());
		jgen.writeEndArray();
	}
}
