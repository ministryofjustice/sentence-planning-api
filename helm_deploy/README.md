
### Example deploy command
```
helm --namespace namespace namespace sentence-planning-development --tiller-namespace sentence-planning-development upgrade sentence-planning-api ./sentence-planning-api/ --install --values=values-dev.yaml --values=example-secrets.yaml
```

### Helm init

```
helm init --tiller-namespace sentence-planning-development --uk.gov.digital.justice.service-account tiller --history-max 200
```
```
