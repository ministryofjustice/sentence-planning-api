{{/* vim: set filetype=mustache: */}}
{{/*
Environment variables for web and worker containers
*/}}
{{- define "deployment.envs" }}
env:
  - name: SERVER_PORT
    value: "{{ .Values.image.port }}"

  - name: SPRING_PROFILES_ACTIVE
    value: "postgres,logstash"

  - name: JAVA_OPTS
    value: "{{ .Values.env.JAVA_OPTS }}"

  - name: OAUTH_ROOT_URL
    value: "{{ .Values.env.OAUTH_ROOT_URL }}"

  - name: ASSESSMENT_API_URI_ROOT
    value: "{{ .Values.env.ASSESSMENT_API_URI_ROOT }}"

  - name: JWT_PUBLIC_KEY
    value: "{{ .Values.env.JWT_PUBLIC_KEY }}"

  - name: DATABASE_USERNAME
    valueFrom:
      secretKeyRef:
        name: sentence-planning-rds-instance-output
        key: database_username

  - name: DATABASE_PASSWORD
    valueFrom:
      secretKeyRef:
        name: sentence-planning-rds-instance-output
        key: database_password

  - name: DATABASE_NAME
    valueFrom:
      secretKeyRef:
        name: sentence-planning-rds-instance-output
        key: database_name

  - name: DATABASE_ENDPOINT
    valueFrom:
      secretKeyRef:
        name: sentence-planning-rds-instance-output
        key: rds_instance_endpoint

  - name: SENTENCEPLAN_API_CLIENT_ID
    valueFrom:
      secretKeyRef:
        name: sentence-planning-api
        key: API_CLIENT_CREDENTIALS_ID

  - name: SENTENCEPLAN_API_CLIENT_SECRET
    valueFrom:
      secretKeyRef:
        name: sentence-planning-api
        key: API_CLIENT_CREDENTIALS_SECRET

{{- end -}}
