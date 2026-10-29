#!/usr/bin/env bash
# ---------------------------------------------------------------------------
#  Prepara las variables de entorno que los dos microservicios necesitan para
#  conectarse al esquema FORO de Oracle Autonomous Database.
#
#  No se ejecuta directamente: lo cargan run-usuarios.sh y run-foro.sh con
#  "source". Las credenciales se leen del Llavero de macOS y nunca se escriben
#  en el repositorio.
#
#  Para guardar la contrasena del esquema una sola vez:
#    read -s -p "Password FORO: " P && \
#      security add-generic-password -a "$USER" -s foro-online-db -w "$P" -U && \
#      unset P && echo " guardada en el Llavero"
# ---------------------------------------------------------------------------

# Carpeta del Wallet descomprimido, fuera del repositorio.
WALLET_DIR="${TNS_ADMIN_DIR:-$HOME/.oracle/wallets/FULLSTACK3}"

if [ ! -f "$WALLET_DIR/tnsnames.ora" ]; then
  echo "ERROR: no encuentro el Wallet en:" >&2
  echo "  $WALLET_DIR" >&2
  echo "Descomprime alli el Wallet de la base o exporta TNS_ADMIN_DIR." >&2
  exit 1
fi

# IMPORTANTE: la ruta del Wallet NO puede contener espacios. El driver JDBC de
# Oracle los elimina al parsear el parametro TNS_ADMIN de la URL y la conexion
# falla con ORA-12263. Por eso el Wallet vive en ~/.oracle/wallets y no junto
# al proyecto, cuya ruta tiene espacios.

TNS_ALIAS="${TNS_ALIAS:-fullstack3_tp}"

export ORACLE_JDBC_URL="jdbc:oracle:thin:@${TNS_ALIAS}?TNS_ADMIN=${WALLET_DIR}"
export ORACLE_USER="${ORACLE_USER:-FORO}"

if ! ORACLE_PASSWORD="$(security find-generic-password -a "$USER" -s foro-online-db -w 2>/dev/null)"; then
  echo "ERROR: no hay contrasena en el Llavero con el nombre 'foro-online-db'." >&2
  echo "Guardala con el comando que aparece en la cabecera de este script." >&2
  exit 1
fi
export ORACLE_PASSWORD

# Secreto con el que foro-usuarios firma el JWT y foro-temas lo valida. Tiene que ser
# el mismo en los dos servicios; si no existe todavia, se genera y se guarda.
if ! FORO_JWT_SECRETO="$(security find-generic-password -a "$USER" -s foro-jwt-secreto -w 2>/dev/null)"; then
  FORO_JWT_SECRETO="$(LC_ALL=C tr -dc 'A-Za-z0-9' < /dev/urandom | head -c 64)"
  security add-generic-password -a "$USER" -s foro-jwt-secreto -w "$FORO_JWT_SECRETO" -U
  echo "Secreto JWT generado y guardado en el Llavero como 'foro-jwt-secreto'."
fi
export FORO_JWT_SECRETO

export JAVA_HOME="${JAVA_HOME:-$(/usr/libexec/java_home -v 21)}"
