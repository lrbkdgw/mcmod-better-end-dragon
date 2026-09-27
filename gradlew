#!/usr/bin/env sh
set -eu
ROOT="$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)"
GRADLE_VERSION=8.5
CACHE="${GRADLE_USER_HOME:-$HOME/.gradle}/wrapper/dists/gradle-$GRADLE_VERSION-bin"
DIST=$(find "$CACHE" -type f -path '*/gradle/bin/gradle' 2>/dev/null | head -1 || true)
if [ -z "$DIST" ]; then
  TMP="${TMPDIR:-/tmp}/gradle-$GRADLE_VERSION"
  if [ ! -x "$TMP/bin/gradle" ]; then
    mkdir -p "$TMP"
    curl -fsSL "https://services.gradle.org/distributions/gradle-$GRADLE_VERSION-bin.zip" -o "$TMP.zip"
    unzip -q -o "$TMP.zip" -d "${TMP}.unpack"
    mv "${TMP}.unpack/gradle-$GRADLE_VERSION" "$TMP"
  fi
  DIST="$TMP/bin/gradle"
fi
exec "$DIST" "$@"
