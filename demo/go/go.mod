module demo

go 1.21.0

toolchain go1.22.5

require (
	github.com/coffersTech/easy-recon/sdk/go v0.0.0
	github.com/shopspring/decimal v1.4.0
)

require (
	filippo.io/edwards25519 v1.1.0 // indirect
	github.com/go-sql-driver/mysql v1.9.3 // indirect
	github.com/lib/pq v1.11.2 // indirect
	gopkg.in/yaml.v3 v3.0.1 // indirect
)

replace github.com/coffersTech/easy-recon/sdk/go => ../../sdk/go
