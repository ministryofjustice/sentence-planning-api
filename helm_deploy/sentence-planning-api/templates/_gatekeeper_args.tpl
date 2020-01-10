{{/* vim: set filetype=mustache: */}}
{{/*
Args for keycloak gatekeeper service
*/}}
{{- define "gatekeeper.args" }}
args:
    - --client-id={{ .Values.secrets.KEYCLOAK_CLIENT_ID }}
    - --client-secret={{ .Values.secrets.KEYCLOAK_CLIENT_SECRET }}
    - --discovery-url={{ .Values.env.KEYCLOAK_REALM }}
    - --listen=:8081
    - --enable-default-deny
    - --enable-logging=true
    - --enable-json-logging=true
    - --enable-token-header
    - --upstream-url={{ .Values.env.KEYCLOAK_UPSTREAM_URL }}
    - --upstream-response-header-timeout=60s
    - --upstream-expect-continue-timeout=60s
    - --upstream-keepalive-timeout=60s
    - --server-read-timeout=60s
    - --server-write-timeout=60s
    - --no-redirects=true
    - --cors-origins='*'
    - --resources=uri=/health|white-listed=true
    - --resources=uri=/info|white-listed=true
    - --resources=uri=/*
    - --http-only-cookie=true
    - --add-claims=name
    - --add-claims=given_name
    - --add-claims=family_name
    - --verbose

{{- end -}}
