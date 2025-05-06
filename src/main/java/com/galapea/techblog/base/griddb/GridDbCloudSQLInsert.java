package com.galapea.techblog.base.griddb;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GridDbCloudSQLInsert(@JsonProperty("stmt") String statement) {
}
