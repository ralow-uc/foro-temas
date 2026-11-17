# Cómo contribuir

Los microservicios del foro siguen **Trunk Based Development (TBD)**: un
repositorio por microservicio y una sola rama de larga vida, `main`.

## Reglas

1. **`main` siempre está desplegable.** Todo lo que llega a `main` pasa las
   pruebas automatizadas y construye la imagen Docker (ver `.github/workflows/ci.yml`).
2. **Ramas de vida corta** que salen de `main` y vuelven en uno o dos días:
   - `feature/<descripcion>` para funcionalidades nuevas
   - `fix/<descripcion>` para correcciones
   - `docs/<descripcion>` para documentación
3. **Integración con pull request** y revisión de código antes del merge. El
   merge se hace con `--no-ff` para que quede visible en el historial qué
   commits formaron cada funcionalidad.
4. **Commits pequeños** y con mensajes en español que digan *qué* cambia y
   *por qué*.
5. **Versiones con etiquetas semánticas** (`vMAYOR.MENOR.PARCHE`) sobre `main`.
   No hay ramas `release/` ni `develop`: la versión es una etiqueta del tronco.

## Flujo

```bash
git switch main && git pull
git switch -c feature/mi-cambio
# ... commits pequeños ...
./mvnw test
git push -u origin feature/mi-cambio      # abrir pull request hacia main
# tras la revisión y la CI en verde:
git switch main && git merge --no-ff feature/mi-cambio
git branch -d feature/mi-cambio
git tag -a v1.1.0 -m "Descripción de la versión"
```

## Por qué TBD y no GitFlow

Con un repositorio por microservicio, cada servicio se despliega por separado.
GitFlow agrega ramas `develop`, `release/` y `hotfix/` que en este contexto solo
alargan el tiempo entre escribir un cambio y desplegarlo. TBD mantiene merges
pequeños y frecuentes, menos conflictos y una CI que valida cada integración.
