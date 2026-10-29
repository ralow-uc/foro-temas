#!/usr/bin/env bash
# ---------------------------------------------------------------------------
#  Ejecuta las pruebas automatizadas de foro-temas.
#
#  Corren sobre H2 en modo Oracle, sin tocar la base real ni necesitar red.
#
#  Uso:  ./scripts/test.sh
# ---------------------------------------------------------------------------
set -euo pipefail
DIR="$(cd "$(dirname "$0")" && pwd)"
export JAVA_HOME="${JAVA_HOME:-$(/usr/libexec/java_home -v 21)}"

cd "$DIR/.."
./mvnw test
