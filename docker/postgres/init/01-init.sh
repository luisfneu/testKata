#!/bin/bash
set -e

echo "Creating database and user for Dungeon Solver..."

# Create additional extensions if needed
psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
    -- Enable extensions that might be useful for performance
    CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
    CREATE EXTENSION IF NOT EXISTS "pgcrypto";
    
    -- Grant permissions
    GRANT ALL PRIVILEGES ON DATABASE $POSTGRES_DB TO $POSTGRES_USER;
    
    -- Create indexes for better performance (will be created by Liquibase too, but good to have)
    -- Liquibase will handle the actual table creation
EOSQL

echo "Database initialization completed!"
