#!/bin/sh

java -cp search-registration-2.0-SNAPSHOT-jar-with-dependencies.jar org.bahmni.implementation.searchconfig.SearchMigrator . $1 localhost superuser Admin123 true true

