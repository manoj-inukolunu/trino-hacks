/*
 * Copyright 2004-present Facebook. All Rights Reserved.
 */
package com.facebook.presto.server;

import com.facebook.presto.execution.PageBuffer;

public interface QueryDriverProvider
{
    QueryDriver create(PageBuffer outputBuffer);
}
