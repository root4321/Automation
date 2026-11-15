package org.test.support;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.filter.Filter;
import io.restassured.filter.FilterContext;
import io.restassured.http.Header;
import io.restassured.http.Headers;
import io.restassured.response.Response;
import io.restassured.specification.FilterableRequestSpecification;
import io.restassured.specification.FilterableResponseSpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;
import java.util.Set;
import java.util.function.Consumer;
import java.util.regex.Pattern;

/**
 * Logs every API call (request line, headers, body and the response status, headers, body) to the console log
 * and attaches the same text to the running Cucumber scenario so it appears in the reports.
 * Credentials in sensitive headers and query parameters are masked.
 */
public class ApiLoggingFilter implements Filter {
    private static final Logger LOG = LoggerFactory.getLogger(ApiLoggingFilter.class);
    private static final String MASK = "****";
    private static final Set<String> SENSITIVE_HEADERS =
            Set.of("authorization", "proxy-authorization", "cookie", "set-cookie", "x-api-key");
    private static final Pattern SENSITIVE_QUERY_PARAMS =
            Pattern.compile("(?i)([?&](?:key|api_key|apikey|access_token|token|password)=)[^&#]*");
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final Consumer<String> sink;

    public ApiLoggingFilter() {
        this(ApiLoggingFilter::logAndAttach);
    }

    ApiLoggingFilter(Consumer<String> sink) {
        this.sink = sink;
    }

    @Override
    public Response filter(FilterableRequestSpecification request, FilterableResponseSpecification responseSpec,
                           FilterContext context) {
        String requestText = describeRequest(request);
        Response response;
        try {
            response = context.next(request, responseSpec);
        } catch (RuntimeException e) {
            sink.accept(requestText + System.lineSeparator() + "<<< NO RESPONSE: " + e);
            throw e;
        }
        sink.accept(requestText + System.lineSeparator() + describeResponse(response));
        return response;
    }

    private static String describeRequest(FilterableRequestSpecification request) {
        StringBuilder text = new StringBuilder(">>> REQUEST").append(System.lineSeparator());
        text.append(request.getMethod()).append(' ').append(maskQuery(request.getURI())).append(System.lineSeparator());
        text.append("Headers:").append(System.lineSeparator());
        appendHeaders(text, request.getHeaders());
        text.append("Body:").append(System.lineSeparator());
        Object body = request.getBody();
        text.append(body == null ? "<none>" : pretty(body.toString()));
        return text.toString();
    }

    private static String describeResponse(Response response) {
        StringBuilder text = new StringBuilder("<<< RESPONSE ").append(response.getStatusLine())
                .append(" (").append(response.getTime()).append(" ms)").append(System.lineSeparator());
        text.append("Headers:").append(System.lineSeparator());
        appendHeaders(text, response.getHeaders());
        text.append("Body:").append(System.lineSeparator());
        String body = response.asString();
        text.append(body == null || body.isEmpty() ? "<empty>" : pretty(body));
        return text.toString();
    }

    private static void appendHeaders(StringBuilder text, Headers headers) {
        if (headers == null || !headers.iterator().hasNext()) {
            text.append("  <none>").append(System.lineSeparator());
            return;
        }
        for (Header header : headers) {
            String value = SENSITIVE_HEADERS.contains(header.getName().toLowerCase(Locale.ROOT))
                    ? MASK : header.getValue();
            text.append("  ").append(header.getName()).append(": ").append(value).append(System.lineSeparator());
        }
    }

    static String maskQuery(String uri) {
        return SENSITIVE_QUERY_PARAMS.matcher(uri).replaceAll("$1" + MASK);
    }

    private static String pretty(String body) {
        String trimmed = body.trim();
        if (trimmed.startsWith("{") || trimmed.startsWith("[")) {
            try {
                return MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(MAPPER.readTree(trimmed));
            } catch (Exception ignored) {
                // Not valid JSON; log it as received.
            }
        }
        return body;
    }

    private static void logAndAttach(String text) {
        ScenarioContext context = ScenarioContext.current().orElse(null);
        String scenario = context == null ? "-" : context.scenario().getName();
        LOG.info("API call [scenario: {}]{}{}", scenario, System.lineSeparator(), text);
        if (context != null) {
            context.attachText("API call", text);
        }
    }
}
