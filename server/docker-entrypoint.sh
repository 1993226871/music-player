#!/bin/sh
echo "Starting Music Player Server with production profile..."
java -Dspring.profiles.active=prod -jar app.jar