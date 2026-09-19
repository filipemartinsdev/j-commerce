package com.products.config;

import graphql.GraphQLContext;
import graphql.execution.CoercedVariables;
import graphql.language.StringValue;
import graphql.language.Value;
import graphql.schema.*;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.graphql.execution.RuntimeWiringConfigurer;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.Locale;

@Configuration
public class GraphqlConfig {
    @Bean
    public RuntimeWiringConfigurer registerScalarTypes(){
        return wiringBuilder -> wiringBuilder.scalar(
                GraphQLScalarType.newScalar()
                        .name("Instant")
                        .description("ISO-8601 UTC (e.g.: 2026-08-04T13:00:00Z)")
                        .coercing(getInstantCoercing())
                        .build()
        ).scalar(
                GraphQLScalarType.newScalar()
                        .name("URL")
                        .description("RFC 3986 (e.g.: https://youtube.com/alanzoka)")
                        .coercing(getURLCoercing())
                        .build()
        );
    }

    private Coercing<Instant, String> getInstantCoercing(){
        return new Coercing<Instant, String>() {
            @Override
            public String serialize(
                    Object dataFetcherResult,
                    GraphQLContext graphQLContext,
                    Locale locale
            ) throws CoercingSerializeException {
                if (dataFetcherResult instanceof Instant instant) {
                    return instant.toString();
                }
                throw new CoercingSerializeException(
                        "Expected java.time.Instant, but got: " + dataFetcherResult.getClass().getName()
                );
            }

            @Override
            public Instant parseValue(
                    Object input,
                    GraphQLContext graphQLContext,
                    Locale locale
            ) throws CoercingParseValueException {
                if (input instanceof String str) {
                    try {
                        return Instant.parse(str);
                    } catch (DateTimeParseException e) {
                        throw new CoercingParseValueException("Invalid date/time format. Use ISO-8601 UTC.");
                    }
                }
                throw new CoercingParseValueException("Expected a String for type Instant");
            }

            @Override
            public Instant parseLiteral(
                    Value<?> input,
                    CoercedVariables variables,
                    GraphQLContext graphQLContext,
                    Locale locale
            ) throws CoercingParseLiteralException {
                if (input instanceof StringValue stringValue) {
                    try {
                        return Instant.parse(stringValue.getValue());
                    } catch (DateTimeParseException e) {
                        throw new CoercingParseLiteralException("Invalid date/time format in literal.");
                    }
                }
                throw new CoercingParseLiteralException("Expected a StringValue in literal.");
            }

            @Override
            public Value<?> valueToLiteral(
                    Object input,
                    GraphQLContext graphQLContext,
                    Locale locale
            ) {
                return StringValue.newStringValue(serialize(input, graphQLContext, locale)).build();
            }
        };
    }

    private Coercing<URL, String> getURLCoercing(){
        return new Coercing<URL, String>() {
            @Override
            public @Nullable String serialize(
                    @NonNull Object dataFetcherResult,
                    @NonNull GraphQLContext graphQLContext,
                    @NonNull Locale locale
            ) throws CoercingSerializeException {
                if (dataFetcherResult instanceof URL url) {
                    return url.toString();
                }
                throw new CoercingSerializeException(
                        "Expected java.net.URL, but got: " + dataFetcherResult.getClass().getName()
                );
            }

            @Override
            public URL parseValue(
                    @NonNull Object input,
                    @NonNull GraphQLContext graphQLContext,
                    @NonNull Locale locale
            ) throws CoercingParseValueException {
                if (input instanceof String str) {
                    try {
                        return URI.create(str).toURL();
                    } catch (MalformedURLException e) {
                        throw new CoercingParseValueException("Invalid URL format. Follow RFC 3986.");
                    }
                }
                throw new CoercingParseValueException("Expected a String for type URL");
            }

            @Override
            public URL parseLiteral(
                    @NonNull Value<?> input,
                    @NonNull CoercedVariables variables,
                    @NonNull GraphQLContext graphQLContext,
                    @NonNull Locale locale
            ) throws CoercingParseLiteralException {
                if (input instanceof StringValue stringValue) {
                    try {
                        return URI.create(stringValue.getValue()).toURL();
                    } catch (MalformedURLException e) {
                        throw new CoercingParseLiteralException("Invalid URL format in literal.");
                    }
                }
                throw new CoercingParseLiteralException("Expected a StringValue in literal.");
            }

            @Override
            public @NonNull Value<?> valueToLiteral(
                    @NonNull Object input,
                    @NonNull GraphQLContext graphQLContext,
                    @NonNull Locale locale
            ) {
                return StringValue.newStringValue(serialize(input, graphQLContext, locale)).build();
            }
        };
    }
}
