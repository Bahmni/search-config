SELECT * from ((SELECT count(person_id) as No_of_Patient_Visit_with_hypertension from confirmed_diagnosis_view_new
where name like '%Hypertension%' and date(visit_date_started) between '#startDate#' and '#endDate#') as No_of_Patient_Visit_with_hypertension,

(SELECT sum(isHTInControl('#startDate#','#endDate#',  pid)) as No_of_Patient_Visit_with_hypertension_incontrol
from (SELECT person_id as pid from confirmed_diagnosis_view_new
where name like '%Hypertension%' and date(visit_date_started) between '#startDate#' and '#endDate#') as x) as No_of_Patient_Visit_with_hypertension_incontrol,

(SELECT (sum(isHTInControl('#startDate#','#endDate#',  pid))/count(pid))*100 as Percent_of_Patient_Visit_with_hypertension_incontrol
from (SELECT person_id as pid from confirmed_diagnosis_view_new
where name like '%Hypertension%' and date(visit_date_started) between '#startDate#' and '#endDate#') as x) as Percent_of_Patient_Visit_with_hypertension_incontrol,


(SELECT count(DISTINCT person_id) as No_of_Distinct_Patient_with_hypertension from confirmed_diagnosis_view_new
where name like '%Hypertension%' and date(visit_date_started) between '#startDate#' and '#endDate#') as No_of_Distinct_Patient_with_hypertension,

(SELECT sum(isHTInControl('#startDate#','#endDate#',  pid)) as No_of_Distinct_Patient_with_hypertension_incontrol
from (SELECT person_id as pid from confirmed_diagnosis_view_new
where name like '%Hypertension%' and date(visit_date_started) between '#startDate#' and '#endDate#') as x) as No_of_Distinct_Patient_with_hypertension_incontrol,

(SELECT (sum(isHTInControl('#startDate#','#endDate#',  pid))/count(pid))*100 as Percent_of_Distinct_Patient_with_hypertension_incontrol
from (SELECT person_id as pid from confirmed_diagnosis_view_new
where name like '%Hypertension%' and date(visit_date_started) between '#startDate#' and '#endDate#') as x ) as Percent_of_Distinct_Patient_with_hypertension_incontrol)
