## SEARCH (Gadchiroli Hospotal) specific Bahmni configuration and data. 

Deploy
- under server (apache) www directory
- alias root (search-config) to bahmni_config


*Dev commands:*
* `./scripts/vagrant-link.sh` to link search_config to vagrants /var/www/bahmni_config
* `./scripts/vagrant-database.sh` to run liquibase migrations in vagrant 

 Configurations -
 ==============================================================
 1) Clinical app.json: example -  (Details in comments)

```javascript

 "config" : {
    "otherInvestigationsMap": {
        "Radiology": "Radiology Order",
        "Endoscopy": "Endoscopy Order"
    },
    "conceptSetUI": {   // all configs for conceptSet added here
        "XCodedConcept": {  // name of the concept
            "autocomplete": true,  // if set to true, it will show autocomplete instead of dropdown for coded concept answers.
            "showAbnormalIndicator": true   //If set to true, will show a checkbox for capturing abnormal observation.
        },
        "Text Complaints": {    //name of the concept
            "freeTextAutocomplete": {   //if present, will show a textbox, with autocomplete for concept name.
                "conceptSetName": "VITALS_CONCEPT",  // autocomplete will search for concepts which are membersOf this conceptSet (Optional)
                "codedConceptName": "Complaints"     // autocomplete will search for concepts which are answersTo this codedConcept (Optional)
            }
        }
    }
}

```
