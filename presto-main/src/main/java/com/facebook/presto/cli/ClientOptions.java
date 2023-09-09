/*
 * Copyright 2004-present Facebook. All Rights Reserved.
 */
package com.facebook.presto.cli;

import io.airlift.command.Option;

import java.net.URI;

public class ClientOptions
{
    @Option(name = "--server", title = "server")
    public URI server = URI.create("http://localhost:8080");

    @Option(name = "--user", title = "user")
    public String user = System.getProperty("user.name");

    @Option(name = "--catalog", title = "catalog")
    public String catalog;

    @Option(name = "--schema", title = "schema")
    public String schema;

    @Option(name = "--debug", title = "debug")
    public boolean debug;

    public ClientSession toClientSession()
    {
        return new ClientSession(server, user, catalog, schema, debug);
    }
}
