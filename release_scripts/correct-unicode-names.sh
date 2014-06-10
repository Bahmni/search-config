#!/bin/sh

NAME_LOOKUP_FILE=$1

IFS=","
while read f1 f2
do
if [ "${f1}" != "" -a "${f2}" != "" ]
then
fromName=\'$f1\'
toName=\'$f2\'
mysql -uopenmrs-user -ppassword -Dopenmrs <<END_SQL
	update person_name set given_name = $toName where given_name =$fromName;
	update person_name set middle_name = $toName where middle_name =$fromName;
	update person_name set family_name = $toName where family_name =$fromName;
	update person_attribute set value = $toName where value = $fromName;
END_SQL
fi
done < $NAME_LOOKUP_FILE

echo "All set"