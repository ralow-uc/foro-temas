#!/usr/bin/env bash
# ---------------------------------------------------------------------------
#  Ejecuta un script .sql contra la base Oracle Cloud.
#
#  En este equipo no hay sqlplus ni sqlcl instalados, asi que el script se
#  ejecuta por JDBC con EjecutarSql.java y el driver ojdbc11 que Maven ya
#  descargo. La alternativa manual es pegar el script en Database Actions.
#
#  Uso:
#    ./scripts/sql.sh admin scripts/00_crear_esquema_foro.sql
#    ./scripts/sql.sh foro  scripts/01_crear_tablas.sql
#
#  El primer argumento elige con que usuario conectarse:
#    admin -> usuario ADMIN, contrasena del Llavero (fullstack3-admin)
#    foro  -> usuario FORO,  contrasena del Llavero (foro-online-db)
# ---------------------------------------------------------------------------
set -euo pipefail

if [ $# -lt 2 ]; then
  echo "Uso: $0 {admin|foro} <archivo.sql> [--continuar-con-errores]" >&2
  exit 2
fi

QUIEN="$1"
ARCHIVO="$2"
EXTRA="${3:-}"

WALLET_DIR="${TNS_ADMIN_DIR:-$HOME/.oracle/wallets/FULLSTACK3}"
if [ ! -f "$WALLET_DIR/tnsnames.ora" ]; then
  echo "ERROR: no encuentro el Wallet en $WALLET_DIR" >&2
  exit 1
fi

TNS_ALIAS="${TNS_ALIAS:-fullstack3_tp}"
JDBC_URL="jdbc:oracle:thin:@${TNS_ALIAS}?TNS_ADMIN=${WALLET_DIR}"

case "$QUIEN" in
  admin) USUARIO="ADMIN"; LLAVE="fullstack3-admin" ;;
  foro)  USUARIO="FORO";  LLAVE="foro-online-db" ;;
  *) echo "ERROR: el primer argumento debe ser 'admin' o 'foro'." >&2; exit 2 ;;
esac

if ! CLAVE="$(security find-generic-password -a "$USER" -s "$LLAVE" -w 2>/dev/null)"; then
  echo "ERROR: no hay contrasena en el Llavero con el nombre '$LLAVE'." >&2
  echo "Guardala con:" >&2
  echo "  read -s -p 'Password: ' P && security add-generic-password -a \"\$USER\" -s $LLAVE -w \"\$P\" -U && unset P" >&2
  exit 1
fi

OJDBC="$(find "$HOME/.m2/repository/com/oracle/database/jdbc/ojdbc11" -name "ojdbc11-*.jar" | sort | tail -1)"
PKI="$(find "$HOME/.m2/repository/com/oracle/database/security/oraclepki" -name "oraclepki-*.jar" | sort | tail -1)"
if [ -z "$OJDBC" ]; then
  echo "ERROR: no encuentro ojdbc11 en el repositorio local de Maven." >&2
  echo "Ejecuta primero: (cd foro-usuarios && ./mvnw dependency:go-offline)" >&2
  exit 1
fi

export JAVA_HOME="${JAVA_HOME:-$(/usr/libexec/java_home -v 21)}"
DIR_SCRIPTS="$(cd "$(dirname "$0")" && pwd)"

exec "$JAVA_HOME/bin/java" --class-path "$OJDBC:$PKI" \
  "$DIR_SCRIPTS/EjecutarSql.java" \
  "$JDBC_URL" "$USUARIO" "$CLAVE" "$ARCHIVO" $EXTRA
