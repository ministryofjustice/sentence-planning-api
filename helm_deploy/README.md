
### Example deploy command
```
helm --namespace sentence-planning-development --tiller-namespace sentence-planning-development upgrade sentence-planning-api ./sentence-planning-api/ --install --values=values-dev.yaml --values=example-secrets.yaml
```

### Rolling back a release
Find the revision number for the deployment you want to roll back:
```
helm --tiller-namespace sentence-planning-development history sentence-planning-api -o yaml
```
(note, each revision has a description which has the app version and circleci build URL)

Rollback
```
helm --tiller-namespace sentence-planning-development rollback sentence-planning-api [INSERT REVISION NUMBER HERE] --wait
```

### Helm init

```
helm init --tiller-namespace sentence-planning-development --uk.gov.digital.justice.service-account tiller --history-max 200
```
```
