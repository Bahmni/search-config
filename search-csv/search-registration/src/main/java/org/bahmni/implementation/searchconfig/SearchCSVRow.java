package org.bahmni.implementation.searchconfig;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.bahmni.csv.CSVEntity;
import org.bahmni.csv.CSVHeader;

public class SearchCSVRow extends CSVEntity {
    @CSVHeader(name = "Visit Date")
    public String visit_date;

    @CSVHeader(name = "ANC no")
    public String anc_no;

    @CSVHeader(name = "New case no")
    public String newCaseNo;

    @CSVHeader(name = "Old Case no")
    public String oldCaseNo;

    @CSVHeader(name = "Prefix")
    public String prefix;

    @CSVHeader(name = "First Name")
    public String firstName;

    @CSVHeader(name = "Middle Name")
    public String middleName;

    @CSVHeader(name = "Last Name")
    public String lastName;

    @CSVHeader(name = "Village")
    public String village;

    @CSVHeader(name = "Tehsil")
    public String tehsil;

    @CSVHeader(name = "Mobile no")
    public String mobileNumber;

    @CSVHeader(name = "Age")
    public String age;

    @CSVHeader(name = "Gender")
    public String gender;

    @CSVHeader(name = "Fees")
    public String fees;

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
