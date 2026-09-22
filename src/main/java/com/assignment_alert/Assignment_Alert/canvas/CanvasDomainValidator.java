package com.assignment_alert.Assignment_Alert.canvas;

import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.assignment_alert.Assignment_Alert.exceptions.RequestValidationException;

@Component
public class CanvasDomainValidator {

    private static final String GENERIC_ERROR =
            "That doesn't look like a valid Canvas address. Use your school's Canvas URL, for example canvas.yourschool.edu.";

    @Value("${app.canvas.allowed-domain-suffixes:}")
    private List<String> allowedSuffixes = List.of();

    public String normalize(String rawDomain) {
        if (rawDomain == null || rawDomain.isBlank()) {
            throw new RequestValidationException(GENERIC_ERROR);
        }

        String candidate = rawDomain.trim();
        while (candidate.endsWith("/")) {
            candidate = candidate.substring(0, candidate.length() - 1);
        }

        if (!candidate.contains("://")) {
            candidate = "https://" + candidate;
        }

        URI uri;
        try {
            uri = new URI(candidate);
        } catch (URISyntaxException e) {
            throw new RequestValidationException(GENERIC_ERROR);
        }

        if (!"https".equalsIgnoreCase(uri.getScheme())) {
            throw new RequestValidationException("The Canvas address must use https.");
        }
        if (uri.getUserInfo() != null) {
            throw new RequestValidationException(GENERIC_ERROR);
        }
        if (uri.getPort() != -1 && uri.getPort() != 443) {
            throw new RequestValidationException(GENERIC_ERROR);
        }
        if (uri.getPath() != null && !uri.getPath().isEmpty()) {
            throw new RequestValidationException(GENERIC_ERROR);
        }
        if (uri.getQuery() != null || uri.getFragment() != null) {
            throw new RequestValidationException(GENERIC_ERROR);
        }

        String host = uri.getHost();
        if (host == null || host.isBlank()) {
            throw new RequestValidationException(GENERIC_ERROR);
        }
        host = host.toLowerCase(Locale.ROOT);

        requirePublicHost(host);
        requireAllowedSuffix(host);

        return "https://" + host;
    }

    private void requirePublicHost(String host) {
        InetAddress[] addresses;
        try {
            addresses = InetAddress.getAllByName(host);
        } catch (UnknownHostException e) {
            throw new RequestValidationException(GENERIC_ERROR);
        }

        for (InetAddress address : addresses) {
            if (isPrivate(address)) {
                throw new RequestValidationException("That Canvas address points to a private network and can't be used.");
            }
        }
    }

    private boolean isPrivate(InetAddress address) {
        if (address.isAnyLocalAddress()
                || address.isLoopbackAddress()
                || address.isLinkLocalAddress()
                || address.isSiteLocalAddress()
                || address.isMulticastAddress()) {
            return true;
        }

        byte[] octets = address.getAddress();
        if (octets.length == 4) {
            int first = octets[0] & 0xFF;
            int second = octets[1] & 0xFF;
            if (first == 0) {
                return true;
            }
            if (first == 100 && second >= 64 && second <= 127) {
                return true;
            }
            if (first == 192 && second == 0) {
                return true;
            }
            if (first == 198 && (second == 18 || second == 19)) {
                return true;
            }
        } else if (octets.length == 16 && (octets[0] & 0xFE) == 0xFC) {
            return true;
        }

        return false;
    }

    private void requireAllowedSuffix(String host) {
        if (allowedSuffixes == null || allowedSuffixes.isEmpty()) {
            return;
        }

        for (String suffix : allowedSuffixes) {
            String allowed = suffix.trim().toLowerCase(Locale.ROOT);
            if (!allowed.isEmpty() && (host.equals(allowed) || host.endsWith("." + allowed))) {
                return;
            }
        }

        throw new RequestValidationException("That Canvas domain isn't allowed on this server.");
    }
}
