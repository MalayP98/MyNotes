# VBC Audit Log — Backend & Infrastructure Deep Dive

> **Document Type:** Engineering Reference  
> **Audience:** Backend Engineers, DevOps, On-Call Engineers  
> **Primary Sources:** Or Abramovich (Deep Dive PPT), VBC Transition Doc, Audit Log as a Platform, Release & Deployment Procedures, Iceberg Alerts Index  
> **Repository:** [github.com/Vonage/vbc-audit-log](https://github.com/Vonage/vbc-audit-log)  
> **Current Owning Team:** CPC / ORCA  
> **Language:** TypeScript (monorepo)

---

## Table of Contents

1. [Overview](#1-overview)
2. [Architecture & High-Level Design](#2-architecture--high-level-design)
3. [Core Flows](#3-core-flows)
   - 3.1 [Indexing Flow (Write Path)](#31-indexing-flow-write-path)
   - 3.2 [Public Fetch API (Read Path)](#32-public-fetch-api-read-path)
4. [Business Logic](#4-business-logic)
5. [Database — Elasticsearch](#5-database--elasticsearch)
6. [Error & Burst Handling](#6-error--burst-handling)
7. [BI / Data Warehouse Integration](#7-bi--data-warehouse-integration)
8. [Infrastructure & AWS Services](#8-infrastructure--aws-services)
9. [Authentication & Security](#9-authentication--security)
10. [Codebase Structure](#10-codebase-structure)
11. [SDK — vbc-audit-log-utils](#11-sdk--vbc-audit-log-utils)
12. [DevOps — CI/CD & Deployment](#12-devops--cicd--deployment)
13. [Monitoring & Dashboards](#13-monitoring--dashboards)
14. [Alerting Runbook](#14-alerting-runbook)
15. [Testing Strategy](#15-testing-strategy)
16. [Known Issues & Technical Debt](#16-known-issues--technical-debt)
17. [Useful Links](#17-useful-links)

---

## 1. Overview

The **VBC Audit Log** is a centralized auditing platform within the Vonage Business Communications (VBC) ecosystem. Its primary purpose is to:

- Capture **configuration change events** emitted by various VBC backend services (users, extensions, virtual receptionists, devices, etc.)
- Expose these events through a **public API** to customer admin users, partner admins, and VSUs (Vonage Super Users) via the Admin Portal

The service is designed as a **platform** — other VBC services integrate into it to index their own audit events, while the platform handles storage, authorization, retention, and retrieval uniformly.

Key design principles:

- **Asynchronous by design** — producers fire-and-forget to a queue; events are indexed within seconds under normal conditions
- **Event-centric, not data-centric** — audit records capture what happened, not raw DB diffs
- **Unified API** — all consumers get events in a consistent schema regardless of the source service
- **Authorization-aware** — the fetch API enforces role-based access so users only see what they're allowed to see

---

## 2. Architecture & High-Level Design

The system is split into two independently deployable stacks residing in a single monorepo:

```
┌─────────────────────────────────────────────────────────────────────┐
│                        WRITE PATH (Indexing)                        │
│                                                                     │
│  Producer Service                                                   │
│  (HDAP, iceberg-users, etc.)                                        │
│         │                                                           │
│         ▼                                                           │
│  Private API GW  ◄── HMAC Auth + Authorizer                        │
│  (vbc-audit-log-processing)                                         │
│         │                                                           │
│         ▼                                                           │
│  Indexing Lambda                                                    │
│         │                                                           │
│         ▼                                                           │
│  Amazon Kinesis Data Stream                                         │
│  (retention: 14 days)                                               │
│         │                                                           │
│         ▼                                                           │
│  Processing Lambda  ──► DLQ (on failure)                           │
│         │                                                           │
│         ▼                                                           │
│  Elasticsearch  (audit-log-{stage})                                 │
└─────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────┐
│                        READ PATH (Public API)                       │
│                                                                     │
│  Admin Portal / Client                                              │
│         │                                                           │
│         ▼                                                           │
│  Public API GW                                                      │
│  (vbc-audit-log-api)                                                │
│         │                                                           │
│         ▼                                                           │
│  API Lambda  ──► Authorizer service  ──► HDAP (for partner auth)   │
│         │                                                           │
│         ▼                                                           │
│  Elasticsearch (audit-log-{stage})                                  │
└─────────────────────────────────────────────────────────────────────┘
```

The two stacks share the same Elasticsearch cluster but are otherwise fully decoupled, allowing them to be deployed, scaled, and versioned independently.

---

## 3. Core Flows

### 3.1 Indexing Flow (Write Path)

This is the path by which VBC services write audit events into the platform.

**Step-by-step:**

1. **Producer service** detects a configuration change (e.g., extension updated, user deleted).
2. Producer constructs an audit record conforming to the standard schema (entity type, trigger user, event category, item properties, location).
3. Producer signs the request using **HMAC** (unique key/secret pair per source service) and calls the **private API GW** (`vbc-audit-log-processing`).
4. The **Indexing Lambda** receives the request, validates the signature, and writes the payload onto the **Kinesis Data Stream**.
5. The API call returns immediately — the write is acknowledged as soon as the event hits Kinesis. This makes the operation **asynchronous** from the producer's perspective.
6. The **Processing Lambda** is triggered by the Kinesis stream and consumes events, mapping them to the Elasticsearch index structure.
7. Events are written to **Elasticsearch** (`audit-log-{stage}`) where they become searchable.

> Under normal conditions, an event is fully indexed within **seconds** of the original API call.

### 3.2 Public Fetch API (Read Path)

This path is used by the Admin Portal (and any authorized client) to query audit events.

**Step-by-step:**

1. Client (Admin Portal) calls the **Public API GW** (`vbc-audit-log-api`).
2. The **API Lambda** invokes the **Authorizer service** to validate the caller's role and permissions.
   - For partner/HDAP-based authorization, the lambda also calls HDAP directly.
3. Authorized requests are translated into Elasticsearch queries.
4. Results are returned in a **unified response schema** regardless of the event category or source.
5. Authorization filtering ensures users only see events scoped to their account/role.

---

## 4. Business Logic

### 4.1 Retention

Events are retained for **180 days** in Elasticsearch (the operational store accessible via the public API). This is the window visible to customers in the Admin Portal.

For compliance and BI purposes, a separate long-term path retains events for **3 years** in Snowflake (see §7).

### 4.2 VSU Masking

When a Vonage Super User (VSU) performs an action on a customer account, specific fields in the audit record are masked before being returned to non-VSU users. This prevents customers from seeing internal Vonage operational activity in its raw form.

### 4.3 Authorization

All access to the public API is governed by the **Authorizer service** (owned by the UIAM team). The Audit Log service is fully onboarded to Authorizer, meaning:

- No custom auth logic in the service itself
- Role-based access decisions are centralized
- HDAP is called for partner-scoped authorization decisions (partner accounts need an extra HDAP lookup)

The permission required to write to the indexing API is `create:hdap-audit-log`, scoped to `SUPERUSER` system roles.

### 4.4 Entity & Event Category Model

The platform operates on the concept of **entities** (e.g., extension, user, virtual receptionist) that map 1:1 to **event categories**. Event categories follow the naming convention:

```
{entity_name}_management
```

Example: `ext_management` for the Extension entity.

Each entity must be explicitly declared in both:
- The **indexing stack** — to define the Elasticsearch mapping
- The **API stack** — to define the mapping from internal (DB) names to public (applicative) names

---

## 5. Database — Elasticsearch

### 5.1 Overview

Elasticsearch is the **sole operational datastore** for the Audit Log service. There is no relational DB.

| Attribute | Value |
|---|---|
| Index naming | `audit-log-{stage}` (e.g., `audit-log-prod`, `audit-log-qa7`) |
| Technology | Amazon OpenSearch Service (Elasticsearch-compatible) |
| Retention enforced | 180 days (operational) |

### 5.2 Shards — Primary & Replica

Elasticsearch distributes data across **shards**:

- **Primary shards** — define how many partitions of data are created. This is fixed at index creation time and cannot be changed without a full reindex.
- **Replica shards** — additional copies of primary shards. These provide redundancy and read scalability. Count can be changed at any time.

> Sizing and shard configuration details: [VBC Audit-Log – Sizing and Shards Configuration.docx](https://ericssonab70331551vonagepro-my.sharepoint.com/:w:/r/personal/or_abramovich_vonage_com/_layouts/15/Doc.aspx?sourcedoc=%7B6F8B581A-E297-4087-85B1-E8324B1E349B%7D&file=VBC%20Audit-Log%20-%20Sizing%20and%20Shards%20Configuration.docx)

> DB Schema reference: [DB Scheme (Google Doc)](https://docs.google.com/document/d/1ytY7mu6fbvKgqr2W05pPtNKInOhy36wFLYM_weXcPUI/edit?tab=t.0)

### 5.3 Kinesis Stream — Buffer Layer

Between the Indexing Lambda and Processing Lambda sits an **Amazon Kinesis Data Stream**:

- Acts as a durable, ordered buffer
- Retention period: **14 days** — this is also the replay window for any failures
- Shard count can be adjusted, but only within a factor of 2 per deployment (i.e., 4 shards → max 8 or min 2 in a single deploy)
- For larger changes, shard count must be adjusted manually via the AWS Management Console

---

## 6. Error & Burst Handling

### 6.1 Dead Letter Queue (DLQ)

Each processing lambda has an associated **DLQ**. When an event fails to process (e.g., malformed payload, transient ES error), it is routed to the DLQ.

Alert thresholds:
- **Warning**: ≥ 1 message in DLQ
- **Critical**: ≥ 100 messages in DLQ

Investigation steps:
1. Check metrics for count and timing of DLQ population
2. Inspect logs to determine if it's a single failing account/entity or a broader issue
3. Read DLQ messages directly
4. Cross-check with producer service (e.g., iceberg-users, HDAP) logs
5. Use the **DLQ review tool**: `--env prod --dlq <RELEVANT-DLQ-OPTION | All>`

### 6.2 Poison Pills

A "poison pill" is a malformed event that will always fail processing regardless of retries. The system handles this by:
- Routing to DLQ after exhausting retries
- Using Kinesis 14-day retention as a replay window for legitimate failures

### 6.3 Kinesis Throttling

Two classes of throughput alerts:
- **Write throughput exceeded**: Producers are writing faster than Kinesis shard capacity allows. Resolution: increase shard count.
- **Read throughput exceeded**: The Processing Lambda is reading faster than the shard allows. Resolution: adjust consumer configuration or increase shards.

### 6.4 Iterator Age Alert

If the Kinesis iterator age exceeds the threshold (meaning events are piling up faster than they're being consumed), a warning alert fires:

| Level | Threshold |
|---|---|
| Warning | 2 hours (7,200,000 ms) |
| Critical | 2 days (172,800,000 ms) |

A quick mitigation is to increase the Kinesis retention period to prevent data loss while investigating.

---

## 7. BI / Data Warehouse Integration

In addition to the real-time operational store (Elasticsearch), audit events flow into the Vonage data warehouse for long-term compliance and analytics (e.g., CFIUS requirements).

### 7.1 Data Flow

```
VBC Services
     │
     ▼
Elasticsearch (audit-log-{stage})   ← operational (180 day retention)
     │
     ▼
S3 Bucket: vbc-audit-log-bi-integration-prod   ← BI bridge
     │
     ▼
Snowflake External Stage: SECURE_DB.VBC_AUDIT.VBC_AUDIT_LOG
     │
     ▼
Airflow DAG: ntw_src_cfius_vbc_audit_logs_s3_to_snf
     │
     ▼
Snowflake Table: SECURE_DB.VBC_AUDIT.CFIUS_VBC_AUDIT_LOGS_EVENTS
     │
     ▼
EDW Security View: EDW.SECURITY.V_VBC_AUDIT_CFIUS_VBC_AUDIT_LOGS_EVENTS
     │
     ▼
Downstream consumers (security/fraud teams, compliance)
```

### 7.2 Key Details

| Component | Value |
|---|---|
| S3 BI Bucket | `vbc-audit-log-bi-integration-prod` |
| Snowflake Stage | `SECURE_DB.VBC_AUDIT.VBC_AUDIT_LOG` |
| Snowflake Table | `SECURE_DB.VBC_AUDIT.CFIUS_VBC_AUDIT_LOGS_EVENTS` |
| Airflow DAG | `ntw_src_cfius_vbc_audit_logs_s3_to_snf` |
| EDW View | `EDW.SECURITY.V_VBC_AUDIT_CFIUS_VBC_AUDIT_LOGS_EVENTS` |
| Retention (Snowflake) | **3 years** (enforced via Snowflake purge process) |
| Access Control | Direct access to `SECURE_DB` is restricted; downstream consumers use the EDW view |

---

## 8. Infrastructure & AWS Services

| AWS Service | Role in Audit Log |
|---|---|
| **API Gateway** | Two gateways — `vbc-audit-log-processing` (private/write) and `vbc-audit-log-api` (public/read) |
| **AWS Lambda** | Indexing Lambda, Processing Lambda, API Lambda — all unversioned (new deployment replaces old) |
| **Amazon Kinesis** | Durable event buffer between Indexing and Processing Lambda; 14-day retention |
| **Amazon OpenSearch (ES)** | Primary operational datastore; index `audit-log-{stage}` |
| **Amazon S3** | BI integration bucket; Kafka connector notification bucket |
| **Amazon SQS (DLQ)** | Dead letter queues for failed processing events |
| **AWS Secrets Manager** | Secrets and HMAC keys for producer services |
| **Amazon CloudWatch** | Lambda metrics; integrated with Grafana |

### 8.1 Environments

| Environment | Purpose |
|---|---|
| **DEV / Panther** | Active development; auto-deployed on merge to `develop` |
| **QA7** | Functional QA; all changes must be verified here before PROD |
| **PROD** | Production; manual deployment only |

---

## 9. Authentication & Security

### 9.1 Indexing API (Write)

- All producer services authenticate using **HMAC signatures**
- Each producer has a **unique key/secret pair** — contact the CPC/ORCA team to provision one
- The Audit Log service only verifies the Vonage signature (it does not expose any public endpoint directly)
- Authorization is handled by the **Authorizer service** (UIAM team)
- Secrets are stored in **AWS Secrets Manager**

### 9.2 Public API (Read)

- Calls from the Admin Portal are already authenticated through the VBC API Gateway layer before reaching Audit Log
- The API Lambda calls the **Authorizer service** for role-based access decisions
- For partner admins, an additional call to **HDAP** is made to resolve account-scope authorization
- Users only receive events within their authorized scope

### 9.3 Permission Model

The permission `create:hdap-audit-log` grants write access to the indexing endpoint. It is currently restricted to `SUPERUSER` role (system-to-system only).

---

## 10. Codebase Structure

The repository is a **monorepo** managed with Yarn workspaces.

```
vbc-audit-log/
├── /jenkins               # Shared Jenkins pipeline definitions (CI/CD)
├── /packages/
│   ├── /api               # Public Fetch API — Lambda source code and tests
│   ├── /processing        # Indexing/Processing stack — Lambda source code and tests
│   └── /vbc-audit-log-utils  # NPM SDK package for producers
```

### Key Conventions

- **Branching:** GitFlow (`develop` → `release/X.Y` → `master`)
- **Language:** TypeScript
- **Two independently deployable stacks:** `api` and `processing`
- **Code sharing:** Minimal — packages are intentionally independent to allow separate deployment

### Adding a New Entity

To onboard a new entity (e.g., a new VBC feature area):

1. **Indexing stack** (`/packages/processing`): Define the Elasticsearch field mapping for the new entity
2. **API stack** (`/packages/api`): Define the mapping from internal (ES field) names to public (API response) names
3. **Event category name**: Must follow `snake_case` and format `{entity}_management` (e.g., `ext_management`)
4. **Coordinate with the CPC/ORCA team** to provision an HMAC key/secret for the new producer service
5. Implement the call to the indexing API from your service, signed with the HMAC credentials

---

## 11. SDK — vbc-audit-log-utils

A private NPM package (`@vonage/vbc-audit-log-utils`) is provided to simplify integration for producer services.

- **Package registry:** [npmjs.com/@vonage/vbc-audit-log-utils](https://www.npmjs.com/package/@vonage/vbc-audit-log-utils) (requires NPM login with `@vonage` credentials)
- Handles common logic such as request signing and payload construction
- Currently imported via **absolute path** within the monorepo (known issue — yarn monorepo + deployment conflicts prevent proper package-based import)

### Publishing a New Version

```bash
# 1. Update version in packages/vbc-audit-log-utils/package.json
# 2. Edit README.md if needed
# 3. Verify token
echo $NPM_TOKEN

# 4. Build
cd packages/vbc-audit-log-utils
tsc

# 5. Publish
yarn npm publish
```

> Note: There is currently no CI job for publishing this package — it is a manual process.

---

## 12. DevOps — CI/CD & Deployment

### 12.1 Deployment Architecture

Both stacks are deployed as **unversioned AWS Lambda functions**. A new deployment replaces the existing Lambda in place — no blue/green, no termination step needed.

**Deployment rule:** Always deploy to QA first, verify, then promote to PROD.

**Dependency:** The API stack depends on the Processing stack. When deploying both, always deploy Processing first, then API.

### 12.2 Jenkins Jobs

| Job | Purpose |
|---|---|
| [vbc_audit_log-cut_release_or_hotfix](https://jenkins-master-iceberg-elb.vgcva0.prod.vonagenetworks.net/view/Audit-Log/job/vbc_audit_log-cut_release_or_hotfix/) | Creates a release or hotfix branch |
| [vbc-audit-log-processing-stack-handle-commits](https://jenkins-master-iceberg-elb.vgcva0.prod.vonagenetworks.net/view/Audit-Log/job/vbc-audit-log-processing-stack-handle-commits/) | Builds new release/hotfix branches for processing stack |
| [audit-log-deploy-dev-qa7-processing-stack](https://jenkins-master-iceberg-elb.vgcva0.prod.vonagenetworks.net/view/Audit-Log/job/audit-log-deploy-dev-qa7-processing-stack/) | Deploy Processing stack to QA7 |
| [audit-log-deploy-dev-qa7-api-stack](https://jenkins-master-iceberg-elb.vgcva0.prod.vonagenetworks.net/view/Audit-Log/job/audit-log-deploy-dev-qa7-api-stack/) | Deploy API stack to QA7 |
| [audit-log-deploy-prod-processing-stack](https://jenkins-master-iceberg-elb.vgcva0.prod.vonagenetworks.net/view/Audit-Log/job/audit-log-deploy-prod-processing-stack/) | Deploy Processing stack to PROD |
| [audit-log-deploy-prod-api-stack](https://jenkins-master-iceberg-elb.vgcva0.prod.vonagenetworks.net/view/Audit-Log/job/audit-log-deploy-prod-api-stack/) | Deploy API stack to PROD |
| [vbc_audit_log-promote_to_master](https://jenkins-master-iceberg-elb.vgcva0.prod.vonagenetworks.net/view/Audit-Log/job/vbc_audit_log-promote_to_master/) | Promotes a release branch to master |

### 12.3 Release Flow

```
develop branch
      │
      ▼
  Create git tag (h2o-automation repo)
  Format: vbc_audit_log_<processing|api>_<X.Y.Z>
      │
      ▼
  Jenkins: "Cut release or hotfix" job
  (branch_type: Release, version: X.Y, package_name: processing|api)
      │
      ▼
  Jenkins builds release branch automatically
      │
      ▼
  Prepare release notes (compare tags in GitHub)
      │
      ▼
  Deploy to QA → verify
      │
      ▼
  Deploy to PROD
      │
      ▼
  Promote to master
  Merge fixes back to develop
  Notify #vbc-audit-log-integration Slack channel
```

### 12.4 Hotfix Flow

A hotfix increments only the build number of the currently deployed version:

```
processing-v1.1.0 → processing-v1.1.1
```

Use the same "Cut release or hotfix" job with `branch_type: Hotfix`.

### 12.5 Kinesis Shard Scaling (Manual)

When a shard count change exceeds the 2x factor limit (cannot be done via deployment), perform manually:

1. AWS Console → Kinesis → Data Streams
2. Select the relevant stream
3. Configuration tab → Edit Stream Capacity
4. Set new shard count → Save
5. Wait for status to return to **Active** before deploying

---

## 13. Monitoring & Dashboards

### 13.1 Grafana Dashboards

| Environment | Stack | Dashboard |
|---|---|---|
| QA | Processing | [QA Processing Dashboard](https://qa-metrics-rl9.neo.vocalocity.com/d/vbc-audit-log-processing/vbc-audit-log-processing?orgId=1&refresh=1m) |
| QA | API | [QA API Dashboard](https://qa-metrics-rl9.neo.vocalocity.com/d/vbc-audit-log-api/vbc-audit-log-api?orgId=1&refresh=1m) |
| PROD | Processing | [PROD Processing Dashboard](https://metrics-rl9.neo.vocalocity.com/d/vbc-audit-log-processing/vbc-audit-log-processing?orgId=1&refresh=1m) |
| PROD | API | [PROD API Dashboard](https://metrics-rl9.neo.vocalocity.com/d/vbc-audit-log-api/vbc-audit-log-api?orgId=1&refresh=1m) |

### 13.2 Slack Channels

| Channel | Purpose |
|---|---|
| [#iceberg-alerts](https://vonage.enterprise.slack.com/archives/CG4D0998C) | Critical & warning alerts from all Iceberg services including Audit Log |
| [#vbc-audit-log-warnings](https://vonage.enterprise.slack.com/archives/C01SUK6BHDM) | Audit Log-specific warning-level alerts |
| `#vbc-audit-log-integration` | Deployment notifications and integration coordination |

---

## 14. Alerting Runbook

### Lambda Errors

| Alert | Threshold | Action |
|---|---|---|
| `% errors in lambda <name>` | Warn: >0%, Critical: >20% over 10m | Check Processing/API Grafana dashboard; look at lambda logs in OpenSearch |

> Note: Lambda internal errors (OOM, timeout, AWS infra) are **not** written to standard function logs. Filter by ERROR level will miss these. Cross-check with other lambdas for simultaneous failures.

### API Gateway Errors

The service has **two API GWs**:
- `vbc-audit-log-processing` — private write API
- `vbc-audit-log-api` — public read API

Identify which GW is failing from the alert name, then:
- **Processing GW errors**: Could be Kinesis write failures, auth issues (401 won't alert), Lambda throttling
- **API GW errors**: Could be ES connectivity issues, processing logic errors, HDAP timeout (partner auth)

> A persistent issue with the Processing GW can block ALL producer services from indexing events. Notify relevant teams and consider replaying messages from Kinesis (14-day window).

### Kinesis Stream Alerts

| Alert | Meaning | Action |
|---|---|---|
| Iterator age > 2h (Warning) | Consumer is falling behind | Investigate consumer lambda; increase Kinesis retention as stopgap |
| Iterator age > 2 days (Critical) | Approaching data loss | Immediate investigation; increase retention, scale consumers |
| Write throughput exceeded | Producers exceeding shard capacity | Increase shard count |
| Read throughput exceeded | Consumers exceeding shard read limit | Tune consumer or add shards |
| Get record failures | Records failing to be read from stream | See [AWS Kinesis Troubleshooting Docs](https://docs.aws.amazon.com/streams/latest/dev/troubleshooting-consumers.html) |

### DLQ Alerts

| Threshold | Meaning |
|---|---|
| ≥ 1 message | Isolated failure — investigate during business hours |
| ≥ 100 messages | Broader issue — needs immediate investigation |

Steps: Check metrics timing → Inspect logs for pattern → Examine DLQ messages directly → Run DLQ review tool.

### Elasticsearch Domain Alerts

| Alert | Meaning | Action |
|---|---|---|
| Cluster status **RED** | Primary + replica shards unallocated | Open AWS Support case; use `GET /_cluster/allocation/explain` |
| Cluster status **YELLOW** | New index or insufficient nodes | Check shard allocation; consider AWS support ticket |
| Index writes **blocked** | Storage full or JVM pressure | Free storage or scale instances |
| CPU utilization >60% (Warn) / >80% (Critical) | Instances too small or too few | Upgrade instance type or add nodes |
| Free storage <256 GB (Warn) / <128 GB (Critical) | Approaching full disk | Scale storage |
| JVM memory pressure >75% (Warn) / >80% (Critical) | Memory exhaustion | Scale vertically (up to 64 GiB) or add nodes |
| Automated snapshot failure | No snapshot in 36h | Often caused by red cluster — resolve cluster first |
| Node count < 7 | Cluster nodes failing | Open AWS Support case; see Failed Cluster Nodes guide |

---

## 15. Testing Strategy

| Test Type | Tooling | Notes |
|---|---|---|
| Unit Tests | `ts-jest` | Per-package; covers business logic |
| Component Tests | `testcontainers-node` | Spins up real ES/Kinesis containers locally |
| Integration Tests | Available | Cross-service integration scenarios |
| E2E Tests | Planned / partial | Known gap: does not yet support PROD environment |

---

## 16. Known Issues & Technical Debt

| Issue | Severity | Notes |
|---|---|---|
| **180-day filter timeout** | High | Customer-facing; timeout when fetching events for accounts with high volume (e.g., account 372950 in PROD). Active investigation via [HDAPQA-4257](https://jira.vonage.com/browse/HDAPQA-4257) |
| **ES JVM pressure spikes** | Medium | Spikes observed from June 2026; may require instance type change or shard rebalancing |
| **Connecting to PROD DB** | Medium | Access complexity for on-call engineers |
| **E2E tests for PROD** | Medium | Current E2E tests don't run against PROD; also need to remove hardcoded passwords from repo |
| **msk-to-kinesis replay tool** | Medium | No tooling yet for replaying events from MSK to Kinesis for failure recovery |
| **HDAP priority 2 events in code** | Low | Remove legacy HDAP priority 2 events and load test residue from GH Actions |
| **ES instance cost optimization** | Low | Current instance types may be over-provisioned; ticket open for review |
| **Processing API GW error description** | Low | Errors from the internal API GW are not descriptive; consider moving API GW logic into the indexing Lambda |
| **Generics refactoring in API stack** | Low | Some refactoring needed in public API generics and processing logic |
| **Multi event_category support** | Low | Public API currently only supports a single `event_category` filter per request |
| **vbc-audit-log-utils import** | Low | Package imported via absolute path due to yarn monorepo + deployment issue; proper package import blocked |

---

## 17. Useful Links

| Resource | Link |
|---|---|
| GitHub Repository | [github.com/Vonage/vbc-audit-log](https://github.com/Vonage/vbc-audit-log) |
| Contributing Guide | [CONTRIBUTING.md](https://github.com/Vonage/vbc-audit-log/blob/develop/CONTRIBUTING.md) |
| VBC Transition Doc (Confluence) | [VBC Transition Documentation - VBC Audit Log](https://confluence.vonage.com/display/ESIL/VBC+Transition+Documentation+-+VBC+Audit+Log) |
| Audit Log as a Platform | [confluence.vonage.com/display/ESIL/Audit+Log+as+a+Platform](https://confluence.vonage.com/display/ESIL/Audit+Log+as+a+Platform) |
| Audit Log Index (Confluence) | [confluence.vonage.com/display/ESIL/Audit+Log](https://confluence.vonage.com/display/ESIL/Audit+Log) |
| Release & Deployment Procedures | [Confluence Page](https://confluence.vonage.com/pages/viewpage.action?pageId=149077343) |
| Iceberg Alerts Index (On-Call) | [Confluence Page](https://confluence.vonage.com/pages/viewpage.action?pageId=95522548) |
| Deep Dive PPT (Or Abramovich) | [Deep dive.pptx](https://ericssonab70331551vonagepro-my.sharepoint.com/personal/or_abramovich_vonage_com/_layouts/15/Doc.aspx?sourcedoc=%7B8E4A1296-1DD4-4242-8334-76BC075B0C7B%7D&file=Deep+dive.pptx&action=edit&mobileredirect=true&web=1) |
| Sizing & Shards Config (Word Doc) | [VBC Audit-Log – Sizing and Shards Configuration.docx](https://ericssonab70331551vonagepro-my.sharepoint.com/:w:/r/personal/or_abramovich_vonage_com/_layouts/15/Doc.aspx?sourcedoc=%7B6F8B581A-E297-4087-85B1-E8324B1E349B%7D&file=VBC%20Audit-Log%20-%20Sizing%20and%20Shards%20Configuration.docx) |
| DB Schema (Google Doc) | [DB Scheme](https://docs.google.com/document/d/1ytY7mu6fbvKgqr2W05pPtNKInOhy36wFLYM_weXcPUI/edit?tab=t.0) |
| vbc-audit-log-utils NPM Package | [npmjs.com/@vonage/vbc-audit-log-utils](https://www.npmjs.com/package/@vonage/vbc-audit-log-utils) |
| PROD Processing Dashboard | [Grafana](https://metrics-rl9.neo.vocalocity.com/d/vbc-audit-log-processing/vbc-audit-log-processing?orgId=1&refresh=1m) |
| PROD API Dashboard | [Grafana](https://metrics-rl9.neo.vocalocity.com/d/vbc-audit-log-api/vbc-audit-log-api?orgId=1&refresh=1m) |
| Active Bug (180-day filter) | [HDAPQA-4257](https://jira.vonage.com/browse/HDAPQA-4257) |
| Onboarding Story | [HDAPQA-4258](https://jira.vonage.com/browse/HDAPQA-4258) |
