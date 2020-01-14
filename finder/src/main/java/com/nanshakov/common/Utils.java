package com.nanshakov.common;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.http.client.fluent.Request;

import java.io.IOException;
import java.io.Serializable;
import java.util.regex.Pattern;

import lombok.experimental.UtilityClass;

@UtilityClass
public class Utils {

    public byte[] copyUrlToByteArray(final String urlStr)
            throws IOException {
        return copyUrlToByteArray(urlStr, 5000, 5000);
    }

    public String getExtension(String urlStr) {
        String[] split = urlStr.split(Pattern.quote("."));
        return "." + split[split.length - 1];
    }

    public byte[] copyUrlToByteArray(
            final String urlStr,
            final int connectionTimeout, final int readTimeout)
            throws IOException {
        return Request.Get(urlStr)
                .connectTimeout(connectionTimeout)
                .socketTimeout(readTimeout)
                .execute()
                .returnContent()
                .asBytes();
    }

    public String calculateHashSha1(Object o) {
        return DigestUtils.sha1Hex(DigestUtils.sha1(SerializationUtils.serialize((Serializable) o)));
    }

    public String calculateHashSha256(Object o) {
        return DigestUtils.sha256Hex(DigestUtils.sha256(SerializationUtils.serialize((Serializable) o)));
    }
}
