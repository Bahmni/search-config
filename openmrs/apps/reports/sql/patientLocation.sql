SELECT * FROM
  (select count(DISTINCT e.patient_id) as 'Total number of patients' from
   visit v inner join visit_type vt on vt.visit_type_id = v.visit_type_id and vt.name='IPD'
   inner join encounter e on v.visit_id = e.visit_id
   inner JOIN obs o on o.encounter_id=e.encounter_id and cast(o.obs_datetime as DATE) BETWEEN '#startDate#' and '#endDate#'
   INNER JOIN concept_view cv on cv.concept_id=o.concept_id and cv.concept_full_name='Patients Category')as Total,
  (select count(DISTINCT e.patient_id) as 'Number of patients from SEARCH tribal area' from
    visit v inner join visit_type vt on vt.visit_type_id = v.visit_type_id and vt.name='IPD'
    inner join encounter e on v.visit_id = e.visit_id
    inner JOIN obs o on o.encounter_id=e.encounter_id
    INNER JOIN concept_view cv on cv.concept_id=o.concept_id and cv.concept_full_name='Patients Category'and cast(o.obs_datetime as DATE) BETWEEN '#startDate#' and '#endDate#'
                                  and o.value_coded  in (select cv.concept_id from concept_view cv where cv.concept_full_name='SEARCH Tribal area')) as search,
  (select count(DISTINCT e.patient_id) as 'Number of patients from other parts of Gadchiroli district'
   from person_address pa inner JOIN
     obs o on o.person_id=pa.person_id and pa.county_district='Gadchiroli' and cast(o.obs_datetime as DATE) BETWEEN '#startDate#' and '#endDate#'
              and o.concept_id in(select cv.concept_id from concept_view cv inner join obs o on o.concept_id=cv.concept_id and cv.concept_full_name='Patients Category')
              and o.value_coded not in (select cv.concept_id from concept_view cv where cv.concept_full_name='SEARCH Tribal area')
     inner join encounter e on e.encounter_id = o.encounter_id
     inner join visit on visit.visit_id = e.visit_id
     INNER JOIN visit_type vt on vt.visit_type_id = visit.visit_type_id and vt.name='IPD') as other_gadchiroli,
  (select count(DISTINCT e.patient_id)as 'Number of patients outside Gadchiroli districts'
   from person_address pa inner JOIN
     obs o on o.person_id=pa.person_id and pa.county_district not LIKE 'Gadchiroli' and cast(o.obs_datetime as DATE) BETWEEN '#startDate#' and '#endDate#'
              and o.concept_id in(select cv.concept_id from concept_view cv inner join obs o on o.concept_id=cv.concept_id and cv.concept_full_name='Patients Category')
              and o.value_coded not in (select cv.concept_id from concept_view cv where cv.concept_full_name='SEARCH Tribal area')
     inner join encounter e on e.encounter_id = o.encounter_id
     inner join visit on visit.visit_id = e.visit_id
     INNER JOIN visit_type vt on vt.visit_type_id = visit.visit_type_id and vt.name='IPD') as outside_gadchiroli;
