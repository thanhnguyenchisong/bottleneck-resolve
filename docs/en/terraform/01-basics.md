## Part 1: Terraform basics

### What Terraform is
- IaC (infrastructure as code) tool that plans desired state and applies changes via providers.
- Declarative: you describe resources; Terraform figures out create/update/delete.
- Tracks state to know what exists; plans are diff-like previews before apply.

### Install
- macOS: `brew install terraform`
- Linux: `curl -fsSL https://apt.releases.hashicorp.com/gpg | sudo apt-key add - && sudo apt-add-repository ...` or download from releases.hashicorp.com.
- Verify: `terraform version`

### Core commands (muscle memory)
```bash
terraform init              # download providers/modules, set up backend
terraform fmt               # format code
terraform validate          # syntax + provider validation
terraform plan              # preview changes
terraform apply             # apply with confirmation
terraform destroy           # remove managed resources
```

### Files and structure
- `main.tf` (entry), `variables.tf`, `outputs.tf`, `providers.tf` (common split).
- `terraform.tfvars` or `*.auto.tfvars` for values (auto-loaded).
- `.terraform.lock.hcl` locks provider versions; commit it.
- Use one working directory per stack/env (or use workspaces with care).

### Minimal example (local file)
```hcl
terraform {
  required_providers {
    local = { source = "hashicorp/local", version = "~> 2.4" }
  }
}

provider "local" {}

resource "local_file" "example" {
  filename = "hello.txt"
  content  = "hello"
}
```
Commands:
```bash
terraform init
terraform plan
terraform apply
terraform destroy
```

### Provider and version pinning
- Use version constraints in `required_providers`.
- Pin Terraform in `required_version` to avoid accidental upgrades.

### State caution (even in dev)
- State is authoritative; never delete `.tfstate` unless you want to lose tracking.
- For teams, use remote backends with locking (S3+DynDB, GCS, etc.)—covered in Part 2.

Proceed to Part 2 for workflow, backends, and state management.
