# Security Policy

## Supported versions

Releases are date-stamped (`yyyy.MM.dd.HH.mm`). Only the **latest release** receives fixes; upgrade to it before reporting a problem. The shaded jar is rebuilt and re-checked for every release, and dependency advisories are tracked through GitHub Dependabot.

## Reporting a vulnerability

Please do **not** open a public issue for a security problem.

Use GitHub's private vulnerability reporting for this repository (*Security → Report a vulnerability*). Include the KETE release, the Keycloak version, the destination(s) involved and, if possible, a minimal configuration that reproduces the issue. You will get an acknowledgement within a week, and a fix or a mitigation is published as a regular release.

## Scope notes

- KETE forwards Keycloak events, which contain personal data (usernames, IP addresses, admin representations). Protect the destination transport (`tls.*` options) and the destination itself accordingly.
- Secrets are read from environment variables and never logged; `toString()` output of configuration objects omits them.
- Hostname verification (`tls.verify-hostname`) is off by default for backward compatibility; enable it for any destination reached over an untrusted network.
