#!/usr/bin/env bash
# Verifies release Android artifacts do not ship the emulator API base URL.
set -euo pipefail
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT/android"
./gradlew :core:network:testReleaseUnitTest :app:verifyReleaseApkNoEmulatorApiHost --no-daemon --stacktrace
