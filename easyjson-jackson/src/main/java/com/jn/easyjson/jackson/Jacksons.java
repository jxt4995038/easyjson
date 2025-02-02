/*
 * Copyright 2019 the original author or authors.
 *
 * Licensed under the Apache, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at  http://www.gnu.org/licenses/lgpl-3.0.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jn.easyjson.jackson;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.core.json.JsonReadContext;
import com.fasterxml.jackson.core.json.JsonWriteContext;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.jn.easyjson.core.codec.dialect.DialectIdentify;
import com.jn.easyjson.core.codec.dialect.PropertyCodecConfiguration;
import com.jn.easyjson.jackson.ext.EasyJsonObjectMapper;
import com.jn.langx.annotation.NonNull;
import com.jn.langx.util.Objs;
import com.jn.langx.util.collection.Pipeline;
import com.jn.langx.util.reflect.Reflects;

import java.lang.reflect.Type;
import java.text.DateFormat;
import java.util.Locale;
import java.util.ServiceLoader;
import java.util.TimeZone;

public class Jacksons {
    /**
     * @since 3.2.3
     */
    private static final Version CURRENT_VERSION;

    private static final String JACKSON_CORE_PACKAGE_NAME = "com.fasterxml.jackson.core";

    public static boolean isJacksonJavaType(Type type) {
        return type instanceof JavaType;
    }

    public static JavaType toJavaType(Type type) {
        return (JavaType) type;
    }

    public static boolean getBooleanAttr(DeserializationContext ctx, String key) {
        if (ctx == null || key == null) {
            return false;
        }
        return getBoolean(ctx.getAttribute(key));
    }

    public static boolean getBooleanAttr(SerializerProvider sp, String key) {
        if (sp == null || key == null) {
            return false;
        }
        return getBoolean(sp.getAttribute(key));
    }

    public static boolean getBoolean(Object obj) {
        if (obj == null) {
            return false;
        }
        return obj.toString().toLowerCase().equals("true");
    }

    public static TimeZone getTimeZone(DeserializationContext ctx) {
        return Objs.useValueIfNull((TimeZone) ctx.getAttribute(JacksonConstants.SERIALIZE_TIMEZONE), TimeZone.getDefault());
    }

    public static Locale getLocale(DeserializationContext ctx) {
        return Objs.useValueIfNull((Locale) ctx.getAttribute(JacksonConstants.SERIALIZE_LOCALE), Locale.getDefault());
    }

    public static DateFormat getDateFormatAttr(DeserializationContext ctx, String key) {
        if (ctx == null || key == null) {
            return null;
        }
        return asDateFormat(ctx.getAttribute(key));
    }

    public static DateFormat getDateFormatAttr(SerializerProvider sp, String key) {
        if (sp == null || key == null) {
            return null;
        }
        return asDateFormat(sp.getAttribute(key));
    }

    private static DateFormat asDateFormat(Object object) {
        if (object == null) {
            return null;
        }
        if (object instanceof DateFormat) {
            return (DateFormat) object;
        }
        return null;
    }


    public static String getStringAttr(DeserializationContext ctx, String key) {
        if (ctx == null || key == null) {
            return null;
        }
        return asString(ctx.getAttribute(key));
    }

    public static String getStringAttr(SerializerProvider sp, String key) {
        if (sp == null || key == null) {
            return null;
        }
        return asString(sp.getAttribute(key));
    }

    private static String asString(Object object) {
        if (object == null) {
            return null;
        }
        if (object instanceof String) {
            return (String) object;
        }
        return null;
    }

    public static final DialectIdentify JACKSON = new DialectIdentify();

    static {
        JACKSON.setId("jackson");
        JACKSON.setLibUrl(Reflects.getCodeLocation(ObjectMapper.class).toString());
    }

    public static PropertyCodecConfiguration getPropertyCodecConfiguration(JsonGenerator gen) {
        ObjectCodec objectCodec = gen.getCodec();
        if (!(objectCodec instanceof EasyJsonObjectMapper)) {
            return null;
        }
        JsonStreamContext streamContext = gen.getOutputContext();
        if (streamContext instanceof JsonWriteContext) {
            JsonWriteContext writeContext = (JsonWriteContext) streamContext;
            Object container = writeContext.getCurrentValue();
            String propertyName = writeContext.getCurrentName();
            EasyJsonObjectMapper objectMapper = (EasyJsonObjectMapper) objectCodec;
            JacksonJSONBuilder jsonBuilder = objectMapper.getJsonBuilder();
            DialectIdentify proxyDialectIdentify = jsonBuilder.proxyDialectIdentify();
            return PropertyCodecConfiguration.getPropertyCodecConfiguration(proxyDialectIdentify, container, propertyName);
        }
        return null;
    }

    public static PropertyCodecConfiguration getPropertyCodecConfiguration(@NonNull JsonParser p) {
        ObjectCodec objectCodec = p.getCodec();
        if (!(objectCodec instanceof EasyJsonObjectMapper)) {
            return null;
        }
        JsonStreamContext parsingContext = p.getParsingContext();
        if (parsingContext instanceof JsonReadContext) {
            JsonReadContext readContext = (JsonReadContext) parsingContext;
            Object container = readContext.getCurrentValue();
            String propertyName = readContext.getCurrentName();
            EasyJsonObjectMapper objectMapper = (EasyJsonObjectMapper) objectCodec;
            JacksonJSONBuilder jsonBuilder = objectMapper.getJsonBuilder();
            DialectIdentify proxyDialectIdentify = jsonBuilder.proxyDialectIdentify();
            return PropertyCodecConfiguration.getPropertyCodecConfiguration(proxyDialectIdentify, container, propertyName);
        }
        return null;
    }

    static {
        CURRENT_VERSION = guessCurrentVersion();
    }

    /**
     * @since 3.2.3
     */
    private static Version guessCurrentVersion() {
        JsonFactory factory = Pipeline.of(ServiceLoader.load(JsonFactory.class)).filter(e -> JACKSON_CORE_PACKAGE_NAME.equals(e.getClass().getPackage().getName())).findFirst();
        Version template = factory.version();

        Version version = new Version(template.getMajorVersion(), template.getMinorVersion(), template.getPatchLevel(), null, null, null);
        return version;

    }

    /**
     * @since 3.2.3
     */
    public static Version getCurrentVersion() {
        return CURRENT_VERSION;
    }
}
