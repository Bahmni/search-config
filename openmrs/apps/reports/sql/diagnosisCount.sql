
select name as Diagnosis,va.value_reference as 'Visit Type',u.username as doctor,count(cdvn.person_id) as 'Number of patients' from confirmed_diagnosis_view_new cdvn
INNER JOIN encounter en on en.encounter_id=cdvn.encounter_id
INNER JOIN visit_attribute va on va.visit_id=cdvn.visit_id and va.attribute_type_id=1
INNER JOIN users u on u.user_id=en.creator
  WHERE date(cdvn.encounter_datetime) between '#startDate#' and '#endDate#'
GROUP BY name,va.value_reference,u.username
;