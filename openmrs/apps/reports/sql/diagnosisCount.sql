SELECT vt.name as 'Visit Type', diagnosis.name as Diagnosis, count(diagnosis.person_id) as 'Number of patients'
FROM confirmed_diagnosis_view_new diagnosis
INNER JOIN visit v
on v.visit_id=diagnosis.visit_id
  and cast(obs_datetime as DATE) BETWEEN '#startDate#' and '#endDate#'
INNER JOIN visit_type vt ON
                        vt.visit_type_id=v.visit_type_id
GROUP BY diagnosis.name, vt.name
ORDER BY vt.name;
