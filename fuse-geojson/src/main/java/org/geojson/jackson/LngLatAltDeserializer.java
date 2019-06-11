package org.geojson.jackson;

/*-
 * #%L
 * fuse-geojson
 * %%
 * Copyright (C) 2016 - 2018 yangdb   ------ www.yangdb.org ------
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

import java.io.IOException;

import org.geojson.LngLatAlt;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

public class LngLatAltDeserializer extends JsonDeserializer<LngLatAlt> {

	@Override
	public LngLatAlt deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException,
			JsonProcessingException {
		if (jp.isExpectedStartArrayToken()) {
			return deserializeArray(jp, ctxt);
		}
		throw ctxt.mappingException(LngLatAlt.class);
	}

	protected LngLatAlt deserializeArray(JsonParser jp, DeserializationContext ctxt) throws IOException,
			JsonProcessingException {
		LngLatAlt node = new LngLatAlt();
		node.setLongitude(extractDouble(jp, ctxt, false));
		node.setLatitude(extractDouble(jp, ctxt, false));
		node.setAltitude(extractDouble(jp, ctxt, true));
		if (jp.hasCurrentToken() && jp.getCurrentToken() != JsonToken.END_ARRAY)
			jp.nextToken();
		return node;
	}

	private double extractDouble(JsonParser jp, DeserializationContext ctxt, boolean optional)
			throws JsonParseException, IOException {
		JsonToken token = jp.nextToken();
		if (token == null) {
			if (optional)
				return Double.NaN;
			else
				throw ctxt.mappingException("Unexpected end-of-input when binding data into LngLatAlt");
		}
		else {
			switch (token) {
				case END_ARRAY:
					if (optional)
						return Double.NaN;
					else
						throw ctxt.mappingException("Unexpected end-of-input when binding data into LngLatAlt");
				case VALUE_NUMBER_FLOAT:
					return jp.getDoubleValue();
				case VALUE_NUMBER_INT:
					return jp.getLongValue();
				default:
					throw ctxt.mappingException("Unexpected token (" + token.name()
							+ ") when binding data into LngLatAlt ");
			}
		}
	}
}
