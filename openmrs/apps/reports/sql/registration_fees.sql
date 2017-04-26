select pi.identifier, (case when date(p.date_created) between '#startDate#' and '#endDate#' then 'New' else 'Old' end) as Patient_Type,
  Locale_Name.name, Locale_Name.mobile,
  pa.city_village, pa.county_district as "District", pa.address3 as "Tehsil", p.gender,
                                    ((case WHEN ans.name='-10' then -10 when ans.name='10' then 10 else 0 end )) as "Registration_Fees"
From visit v
  inner join person p on v.patient_id = p.person_id
  inner join visit_type vt on vt.visit_type_id = v.visit_type_id
  inner join person_address pa on p.person_id = pa.person_id
  inner join patient_identifier pi on pi.patient_id = p.person_id
  Left Outer join (
                    select pa.person_id, concat(given_name,' ',middle_name, ' ',family_name) as name,
                                         group_concat(case when pat.name = 'Mobile' then pa.value else '' end separator '') as mobile
                    from person_attribute as pa
                      inner join person_attribute_type pat on pa.person_attribute_type_id = pat.person_attribute_type_id
                      inner JOIN person_name pn on pn.person_id=pa.person_id and pn.preferred=1
                    group by pa.person_id) as Locale_Name on p.person_id = Locale_Name.person_id
  left outer join encounter e on e.visit_id = v.visit_id
  left outer join obs o on o.encounter_id = e.encounter_id and o.voided = 0

  left outer join concept_name as cn on cn.concept_id = o.concept_id AND cn.name = 'Registration Fee'
                                        AND cn.concept_name_type = 'FULLY_SPECIFIED' AND cn.voided = 0
                                        and o.obs_id= (select max(obs_id) from obs oo where oo.encounter_id = e.encounter_id and oo.concept_id=cn.concept_id and oo.voided = 0)
  INNER join concept_name as ans on o.value_coded=ans.concept_id and ans.concept_name_type='SHORT'
where date(v.date_started) between '#startDate#' and '#endDate#' AND date(e.encounter_datetime) between '#startDate#' and '#endDate#'
and (o.concept_id=cn.concept_id or o.concept_id is null)
And vt.name != 'IPD'
and v.patient_id !=8;
