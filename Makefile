.PHONY: help tokens-check backend-test backend-lint android-test android-lint android-assemble admin-install admin-test admin-build openapi-lint verify

help:
	@echo "Mahin M0 targets:"
	@echo "  make tokens-check"
	@echo "  make backend-test"
	@echo "  make android-test"
	@echo "  make admin-build"
	@echo "  make verify"

tokens-check:
	python3 scripts/check_design_tokens.py

backend-test:
	cd backend && ./gradlew test --stacktrace

backend-lint:
	cd backend && ./gradlew ktlintCheck detekt --stacktrace

android-test:
	cd android && ./gradlew test --stacktrace

android-lint:
	cd android && ./gradlew lintDebug ktlintCheck detekt --stacktrace

android-assemble:
	cd android && ./gradlew assembleDebug --stacktrace

admin-install:
	cd admin && npm ci

admin-test:
	cd admin && npm test

admin-build:
	cd admin && npm run build

openapi-lint:
	npx --yes @redocly/cli@1.34.2 lint openapi/openapi.yaml --config redocly.yaml

verify: tokens-check backend-lint backend-test android-lint android-test android-assemble admin-install admin-test admin-build openapi-lint
	@echo "M0 verify completed"
