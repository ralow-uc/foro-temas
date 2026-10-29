#!/usr/bin/env bash
# ---------------------------------------------------------------------------
#  Arranca foro-temas (puerto 8082) contra el esquema FORO de Oracle Cloud.
#
#  Uso:  ./scripts/run.sh
#
#  El otro microservicio del foro (foro-usuarios) corre desde su propio repositorio.
#  Para la demostracion completa hacen falta los dos arriba, porque este
#  servicio valida los tokens que el otro emite.
# ---------------------------------------------------------------------------
set -euo pipefail
DIR="$(cd "$(dirname "$0")" && pwd)"
source "$DIR/entorno-oracle.sh"

echo "foro-temas -> ${ORACLE_USER}@${TNS_ALIAS:-fullstack3_tp}  (puerto 8082)"
cd "$DIR/.."
exec ./mvnw -q spring-boot:run -Dspring-boot.run.profiles=oracle
