#!/bin/bash
set -e  # Stop script on error
exec > /home/ubuntu/setup.log 2>&1  # Redirect all output to a log file

echo "=== Starting user_data script ==="

# Update system and install PostgreSQL client
echo "Updating system..."
apt update -y
echo "Installing PostgreSQL client..."
apt install -y postgresql-client

# Debugging: Print environment variables
echo "DB Endpoint: ${DB_ENDPOINT}"
echo "DB Username: ${DB_USERNAME}"
echo "DB Name: ${DB_NAME}"
echo "DB Password: ${DB_PASSWORD}"
echo "SCRIPT: ${SQL_SCRIPT_CONTENT}"

# Create SQL script file
echo "Creating /home/ubuntu/setup.sql..."
echo "${SQL_SCRIPT_CONTENT}" > /home/ubuntu/setup.sql

# Debugging: Verify the file was created
echo "Checking SQL script content:"
cat /home/ubuntu/setup.sql

# Run the SQL script
echo "Executing SQL script..."
PGPASSWORD="${DB_PASSWORD}" psql -h ${DB_ENDPOINT} -U ${DB_USERNAME} -d ${DB_NAME} -f /home/ubuntu/setup.sql

echo "=== Script execution finished ==="