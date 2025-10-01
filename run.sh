#!/bin/sh
export $(grep -v '^#' envvars | xargs) && java -jar target/interview-tech-challenge-jar-with-dependencies.jar
