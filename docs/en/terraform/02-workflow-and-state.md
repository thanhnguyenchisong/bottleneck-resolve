## Part 2: Workflow and state

### Standard workflow
1) `terraform init` — download providers, set backend.
2) `terraform fmt` — format.
3) `terraform validate` — basic checks.
4) `terraform plan -out=plan.tfplan` — preview and save plan.
5) `terraform apply plan.tfplan` — apply the exact plan.
6) `terraform destroy` — clean up (when intentional).

### Backends (remote state + locking)
- Use remote state for teams to avoid local .tfstate drift.
- Common: S3 + DynamoDB (locking), GCS (with locking), Azure Storage (with locking).
- Example S3 backend (in `terraform` block):
```hcl
terraform {
  backend "s3" {
    bucket         = "my-tf-state-bucket"
    key            = "perf-app/terraform.tfstate"
    region         = "us-east-1"
    dynamodb_table = "my-tf-locks"
    encrypt        = true
  }
}
```
- Initialize (one-time per workspace): `terraform init`

### Workspaces
- Light-weight state partitioning (dev/stage/prod) sharing same config:
```bash
terraform workspace new dev
terraform workspace select dev
terraform workspace list
```
- Use cautiously; many teams prefer separate directories/stacks instead of workspaces for clearer isolation.

### State safety
- Never edit state manually; use `terraform state` commands if needed (move/import).
- Locking prevents concurrent applies; ensure backend supports it.
- Back up state buckets and restrict access (IAM).

### Imports and existing resources
- Bring existing infra under management:
```bash
terraform import aws_s3_bucket.logs my-logs-bucket
```
- After import, add matching resource blocks; run `plan` to confirm no drift.

### Drift detection
- Regular `terraform plan` to spot changes made outside Terraform.
- In CI, run plan nightly and alert on unexpected diffs.

### Destroy caution
- Use `-target` only for surgical changes; prefer full plans to avoid surprises.
- For shared infra, restrict `destroy` via IAM or CI policy.

Proceed to Part 3 for modules and reuse.
