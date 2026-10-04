#!/usr/bin/env bash
# Release shipping guardrails: minified APK + HTTPS API base + no dev hosts in artifact.
set -euo pipefail
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT/android"
./gradlew \
  :core:network:testReleaseUnitTest \
  :core:network:verifyReleaseMahinApiBaseUrlHttps \
  :app:verifyReleaseApkNoEmulatorApiHost \
  --no-daemon --stacktrace
