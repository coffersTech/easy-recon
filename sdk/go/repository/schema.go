package repository

import (
	"database/sql"
	"fmt"

	"github.com/coffersTech/easy-recon/sdk/go/dialect"
)

// InitTables initializes database tables using dialect
func InitTables(db *sql.DB, dialect dialect.ReconDatabaseDialect) error {
	statements := dialect.GetCreateTableSQL()
	for _, stmt := range statements {
		_, err := db.Exec(stmt)
		if err != nil {
			return fmt.Errorf("failed to execute sql: %s, error: %v", stmt, err)
		}
	}
	return nil
}
