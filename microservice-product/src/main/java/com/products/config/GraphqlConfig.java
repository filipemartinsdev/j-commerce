package com.products.config;

import graphql.GraphQLContext;
import graphql.execution.CoercedVariables;
import graphql.language.StringValue;
import graphql.language.Value;
import graphql.schema.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.graphql.execution.RuntimeWiringConfigurer;

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
                        .coercing(new Coercing<Instant, String>() {
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
                        })
                        .build());
    }

}
