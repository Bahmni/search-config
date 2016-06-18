Bahmni.ConceptSet.FormConditions.rules = {
    'Diastolic Data' : function (formName, formFieldValues) {
        var systolic = formFieldValues['Systolic'];
        var diastolic = formFieldValues['Diastolic'];
        if (systolic || diastolic) {
            return {
                enable: ["Posture"]
            }
        } else {
            return {
                disable: ["Posture"]
            }
        }
    },
    'Chest Findings' : function (formName, formFieldValues) {
        var clear = formFieldValues["Chest Findings"];
        if (clear=="Unclear") {
            return {
                enable: ["If Unclear"]
            }
        } else {
            return {
                disable: ["If Unclear"]
            }
        }
    },
    'Heart Sounds' : function (formName, formFieldValues) {
        var normal = formFieldValues["Heart Sounds"];
        if (normal=="Abnormal") {
            return {
                enable: ["If Abnormal"]
            }
        } else {
            return {
                disable: ["If Abnormal"]
            }
        }
    },
    'Systolic Data' : function (formName, formFieldValues) {
        var systolic = formFieldValues['Systolic'];
        var diastolic = formFieldValues['Diastolic'];
        if (systolic || diastolic) {
            return {
                enable: ["Posture"]
            }
        } else {
            return {
                disable: ["Posture"]
            }
        }
    },
       'Liver/Spleen' : function (formName, formFieldValues) {
        var palpable = formFieldValues["Liver/Spleen"];
        if (palpable == "Palpable") {
            return {
                enable: ["If Palpable"]
            }
        } else {
            return {
                disable: ["If Palpable"]
            }
        }
    },
    'Patient Can Undergo Surgery at SEARCH with Due Risk' : function (formName, formFieldValues) {
        var surgery = formFieldValues["Patient Can Undergo Surgery at SEARCH with Due Risk"];
        if (surgery == false) {
            return {
                enable: ["If, No"]
            }
        } else {
            return {
                disable: ["If, No"]
            }
        }
    },
};