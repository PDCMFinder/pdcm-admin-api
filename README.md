# PDCM Admin API

The main purpose of the PDCM Admin API is to provide the support for the mappings process that is required for PDCM Finder.

PDCM Finder, 

## The project

This is a Java project that uses spring boot to expose a set of endpoints that power up the UI for PDCM Admin.




## Important pieces
### JSON mapping rules
A set of JSON files contain the Mapping Rules. A rule defines a relationship between a set of values (keys/values) that represent data that comes from the providers data (for instance Treatments and Diagnosis) and an ontology term.

Those JSON files are a input for the ETL process that loads the providers data into PDCM Finder.

### H2 Database

An embedded database that contains the representation of the JSON mapping rules (mapped terms) plus the terms that are still unmapped in the system (unmapped terms).

Having those values in a database makes it easy to query over the data and make calculations with it.



## Setup development environment

1. Fork this repo using Github's user interface.
2. Clone your fork

```
git clone https://github.com/<YOUR_USERNAME>/pdcm-admin-api
cd pdcm-admin-api
```

3. Add `upstream` remote:

```
git remote add upstream https://github.com/PDCMFinder/pdcm-admin-api.git
```

## Configuration
Configure in rest/src/main/resources/application.properties the data folder and the database location.

## Acknowledgements

PDCM Admin  API is freely available under an Apache 2 license. Work is supported by NCI U24CA253539 and the European Molecular Biology Laboratory.