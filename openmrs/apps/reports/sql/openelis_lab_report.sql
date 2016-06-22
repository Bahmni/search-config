SELECT
  pi.identity_data                                                     AS "Patient ID",
  s.accession_number                                                   AS "Accession Number",
  concat(coalesce(pr.first_name, ''), ' ', coalesce(pr.last_name, '')) AS "Name",
  s.lastupdated                                                        AS "Collected Date"
FROM sample s
  INNER JOIN sample_human sh ON s.id = sh.samp_id AND s.accession_number IS NOT NULL
                                AND cast(s.lastupdated AS DATE) BETWEEN '#startDate#' AND '#endDate#'
  INNER JOIN patient p ON p.id = sh.patient_id
  INNER JOIN patient_identity pi ON p.id = pi.patient_id and pi.identity_type_id=2
  INNER JOIN person pr ON pr.id = p.person_id
GROUP BY pi.identity_data, s.accession_number, "Collected Date", "Name"
ORDER BY "Collected Date";
