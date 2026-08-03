#!/bin/sh
set -e

APP_USER=${APP_USER:-fellowlodge}
UPLOAD_DIR=${UPLOAD_DIR:-/data/uploads}

# If the container is running as root (Render default), prepare the persistent
# upload volume and drop to the unprivileged app user before starting the JVM.
if [ "$(id -u)" = "0" ]; then
    mkdir -p "${UPLOAD_DIR}"
    chown -R "${APP_USER}:${APP_USER}" "${UPLOAD_DIR}"
    exec su -s /bin/sh "${APP_USER}" -- \
        java -XX:MaxRAMPercentage=75.0 -XX:+ExitOnOutOfMemoryError -jar /opt/fellowlodge/app.jar
fi

# Already running as an unprivileged user (e.g. local docker run with -u).
exec java -XX:MaxRAMPercentage=75.0 -XX:+ExitOnOutOfMemoryError -jar /opt/fellowlodge/app.jar
