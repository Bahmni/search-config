SELECT * FROM
  (
  (
SELECT count(*) as 'Total number of patients' from visit v
  INNER JOIN visit_attribute va on va.visit_id=v.visit_id and va.attribute_type_id=1 and va.value_reference='IPD'
WHERE date(v.date_started) between '#startDate#' and '#endDate#')  as Total,

   (SELECT count(*) as 'Number of patients from SEARCH tribal area' from visit v
  INNER JOIN person_address pa on pa.person_id=v.patient_id and
                                  pa.address3='Dhanora' and pa.county_district='Gadchiroli'
                                  and pa.city_village
                                      in ('Bhaapada','Bhendikanhar','Bhimpur','Darachi','Fulbodi','Gathanyeli','Gattepayali',
                                          'Ghodezari','Girola','Gota','Haranda','Horekasa','kachakal','kanhartola','keligattha',
                                          'Kharakadi','Khutgaon','Khutgaon Tola','Kokadkasa','Kondawahi','Kovantola','Kupaner','Kusumtola',
                                          'Kuthegaon','Latzora','Mahawada','Malanda','Marakegaon','Mendha Lekha','Mendha Tola','Munjalgondi',
                                          'Parasawadi','Pathrgota','Pavani','Pustola','Rekhatola','Rengatola','Rondawahi','Sakhera Tola',
                                          'Sinsur','Tavitola','Tudmel','Udegaon','Ushirpar','Vadgaon','Vaghbhumi','Yedampayali',
                                          'Yedampayali Khurd','Yedasgondi','Yerandi','Zari')
  INNER JOIN visit_attribute va on va.visit_id=v.visit_id and va.attribute_type_id=1 and va.value_reference='IPD'
WHERE date(v.date_started) between  '#startDate#' and '#endDate#' ) as search,


(SELECT count(*) as 'Number of patients from other parts of Gadchiroli district' from visit v
  INNER JOIN person_address pa on pa.person_id=v.patient_id
                                  and pa.county_district='Gadchiroli'
                                  and ((pa.city_village not
                                      in ('Bhaapada','Bhendikanhar','Bhimpur','Darachi','Fulbodi','Gathanyeli','Gattepayali',
                                          'Ghodezari','Girola','Gota','Haranda','Horekasa','kachakal','kanhartola','keligattha',
                                          'Kharakadi','Khutgaon','Khutgaon Tola','Kokadkasa','Kondawahi','Kovantola','Kupaner','Kusumtola',
                                          'Kuthegaon','Latzora','Mahawada','Malanda','Marakegaon','Mendha Lekha','Mendha Tola','Munjalgondi',
                                          'Parasawadi','Pathrgota','Pavani','Pustola','Rekhatola','Rengatola','Rondawahi','Sakhera Tola',
                                          'Sinsur','Tavitola','Tudmel','Udegaon','Ushirpar','Vadgaon','Vaghbhumi','Yedampayali',
                                          'Yedampayali Khurd','Yedasgondi','Yerandi','Zari')
                                  and pa.address3 = 'Dhanora'

                                  ) or pa.address3 != 'Dhanora')
  INNER JOIN visit_attribute va on va.visit_id=v.visit_id and va.attribute_type_id=1 and va.value_reference='IPD'
WHERE date(v.date_started) between '#startDate#' and '#endDate#')
 as other_gadchiroli,
  (SELECT count(*) as 'No of Patients outside Gadchiroli District'from visit v
  INNER JOIN person_address pa on pa.person_id=v.patient_id and pa.county_district !='Gadchiroli'
  INNER JOIN visit_attribute va on va.visit_id=v.visit_id and va.attribute_type_id=1 and va.value_reference='IPD'
WHERE date(v.date_started) between '#startDate#' and '#endDate#') as outside_gadchiroli);
