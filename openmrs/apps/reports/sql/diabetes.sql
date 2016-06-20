SELECT * from ((SELECT count(person_id) as No_of_Patient_Visit_with_diabetes from confirmed_diagnosis_view_new
where name like '%Diabetes%' and date(visit_date_started) between '#startDate#' and '#endDate#') as No_of_Patient_Visit_with_diabetes,

(SELECT sum(isDiabetesInControl('#startDate#','#endDate#',  pid)) as No_of_Patient_Visit_with_diabetes_incontrol
from (SELECT person_id as pid from confirmed_diagnosis_view_new
where name like '%Diabetes%' and date(visit_date_started) between '#startDate#' and '#endDate#') as x) as No_of_Patient_Visit_with_diabetes_incontrol,

(SELECT concat(round((sum(isDiabetesInControl('#startDate#','#endDate#',  pid))/count(pid))*100,2), ' %') as Percent_of_Patient_Visit_with_diabetes_incontrol
from (SELECT person_id as pid from confirmed_diagnosis_view_new
where name like '%Diabetes%' and date(visit_date_started) between '#startDate#' and '#endDate#') as x) as Percent_of_Patient_Visit_with_diabetes_incontrol,


(SELECT count(DISTINCT person_id) as No_of_Distinct_Patient_with_diabetes from confirmed_diagnosis_view_new
where name like '%Diabetes%' and date(visit_date_started) between '#startDate#' and '#endDate#') as No_of_Distinct_Patient_with_diabetes,

(SELECT sum(isDiabetesInControl('#startDate#','#endDate#',  pid)) as No_of_Distinct_Patient_with_diabetes_incontrol
from (SELECT person_id as pid from confirmed_diagnosis_view_new
where name like '%Diabetes%' and date(visit_date_started) between '#startDate#' and '#endDate#') as x) as No_of_Distinct_Patient_with_diabetes_incontrol,

(SELECT concat(round((sum(isDiabetesInControl('#startDate#','#endDate#',  pid))/count(pid))*100,2), ' %') as Percent_of_Distinct_Patient_with_diabetes_incontrol
from (SELECT person_id as pid from confirmed_diagnosis_view_new
where name like '%Diabetes%' and date(visit_date_started) between '#startDate#' and '#endDate#') as x ) as Percent_of_Distinct_Patient_with_diabetes_incontrol)
