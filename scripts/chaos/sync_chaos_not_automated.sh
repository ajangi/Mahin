#!/usr/bin/env bash
# NOT AN AUTOMATED TEST — exits 2 so CI/release gates cannot treat this as PASS.
set -u
echo "Sync chaos is manual only. Follow scripts/chaos/README.md on staging."
echo "This script is intentionally NOT a passing check."
exit 2
