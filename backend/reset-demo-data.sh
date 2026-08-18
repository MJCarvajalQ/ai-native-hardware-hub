#!/bin/bash
# Resets the local demo database back to the original 11-item seed.
# Useful before a live demo, after testing has left the data in a
# different state (deleted items, rented items, etc.).
#
# Usage: from the backend/ directory, run:  ./reset-demo-data.sh
# Then start the backend as usual:          mvn spring-boot:run

set -e
cd "$(dirname "$0")"

echo "Stopping anything running on port 8080..."
lsof -ti:8080 | xargs -r kill -9

echo "Deleting local database file..."
rm -f hardwarehub.db hardwarehub.db-journal

echo "Done. The next 'mvn spring-boot:run' will reseed the full 11-item inventory."
