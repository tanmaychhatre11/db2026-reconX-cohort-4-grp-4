# C4 Level 1 - Context Diagram: ReconX

```mermaid
C4Context
    title System Context - ReconX Trade Reconciliation Platform

    Person(ops, "Ops Analyst", "Investigates and resolves trade mismatches day-to-day")
    Person(manager, "Ops Manager", "Reviews reconciliation KPIs and approves break disposals")
    Person(developer, "Platform Engineer", "Deploys and monitors the ReconX platform")
    Person(auditor, "Compliance Auditor", "Reads audit trails and regulatory reports")

    System(reconx, "ReconX", "Enterprise trade reconciliation platform. Ingests trade feeds, detects mismatches, surfaces breaks, and emits audit events.")

    System_Ext(oms, "Order Management System (OMS)", "Source of internal trade bookings")
    System_Ext(sftp, "Counterparty SFTP Feed", "Delivers external trade confirmations from counterparties and custodians")
    System_Ext(bloomberg, "Bloomberg Data Service", "Provides real-time and end-of-day reference and pricing data")
    System_Ext(email, "Email Notification Gateway", "Delivers break alerts and reconciliation summary reports")
    System_Ext(sso, "Identity Provider (SSO)", "Authenticates users and issues JWT access tokens")
    System_Ext(grafana, "Grafana Observability Stack", "Collects Prometheus metrics and visualises platform health dashboards")

    Rel(ops, reconx, "Queries breaks, updates disposals", "HTTPS")
    Rel(manager, reconx, "Approves breaks, views KPI dashboards", "HTTPS")
    Rel(developer, reconx, "Deploys, configures and monitors", "HTTPS")
    Rel(auditor, reconx, "Downloads audit logs and reports", "HTTPS")

    Rel(oms, reconx, "Pushes trade booking events", "Kafka")
    Rel(sftp, reconx, "Delivers counterparty confirmation files", "SFTP")
    Rel(bloomberg, reconx, "Streams reference and pricing data", "HTTPS")
    Rel(reconx, email, "Sends break alerts and summary reports", "SMTP")
    Rel(reconx, sso, "Validates JWT tokens", "HTTPS")
    Rel(reconx, grafana, "Exposes metrics endpoint", "HTTP")

    UpdateLayoutConfig($c4ShapeInRow="4", $c4BoundaryInRow="1")
```
