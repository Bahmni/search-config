#!/bin/sh
set -x -i
FILE=$2
NAME_LOOKUP_FILE=$1
IFS=","
while read fromName toName
do
from=\'$fromName\' 
to=\'$toName\' 
	
mysql -uopenmrs-user -ppassword -Dopenmrs <<END_SQL
  	update person_name set given_name = $to where given_name =$from;
	update person_name set middle_name = $to where middle_name =$from;
	update person_name set family_name = $to where family_name =$from;
	update person_attribute set value = $to where value =$from;
	update person_address set address3 = $to where address3 = $from;
	update person_address set county_district = $to where county_district = $from;
	update person_address set state_province = $to where state_province = $from;
	update person_address set city_village = $to where city_village = $from;
END_SQL

done < $NAME_LOOKUP_FILE
echo "All set"