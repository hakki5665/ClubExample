package com.example.club.integration;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;


public final class WireMockExtensionHolder {

    private WireMockExtensionHolder() {

    }

    public static final WireMockExtension WIREMOCK = WireMockExtension
            .newInstance()
            .options(wireMockConfig().dynamicPort())
            .build();
}